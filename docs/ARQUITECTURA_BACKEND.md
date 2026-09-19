# Arquitectura del Backend Evox

Documento de referencia para entender cómo está construido y cómo funciona el backend de la
plataforma de venta de productos electrónicos.

- **Stack:** Spring Boot 3.3.4 · Java 17 · PostgreSQL (Supabase) · JWT (jjwt 0.11.5) · Lombok
- **Artefacto Maven:** `com.evox:evox-backend` (paquete raíz `com.evox.backend`)
- **Puerto del servidor:** `8081`

---

## 1. Estructura por capas

El código sigue una arquitectura en capas simple:

```
com.evox.backend/
├── model/       → Entidades JPA (reflejan las tablas de la base de datos)
├── repository/  → Acceso a datos (interfaces JpaRepository)
├── dto/         → Moldes JSON que entran (request) y salen (response) de la API
├── service/     → Lógica de negocio (reglas, validaciones, permisos)
├── controller/  → Endpoints REST (lo que expone cada URL)
├── security/    → Autenticación JWT y control de roles
├── exception/   → Manejo centralizado de errores
└── EvoxBackendApplication.java → Clase de arranque (@SpringBootApplication)
```

El flujo de una petición es siempre el mismo:

```
HTTP → Controller → Service → Repository → Base de datos (Supabase/PostgreSQL)
                        ↑
                    (valida reglas de negocio y lanza ApiException cuando falla)
```

---

## 2. Modelo de datos (entidades)

Todas las entidades usan Lombok `@Data`, **ID `UUID`** autogenerado por Hibernate
(`@UuidGenerator`), timestamps en `OffsetDateTime` (zona horaria ISO-8601) y `@PrePersist`
para autocompletar las fechas.

| Entidad | Tabla | Qué representa | Campos clave |
|---|---|---|---|
| `Perfil` | `perfiles` | Usuario de la plataforma | `correo` (único, login), `nombreCompleto`, `password` (hash BCrypt), `rol` |
| `Categoria` | `categorias` | Categoría del catálogo | `nombre` (único), `descripcion`, `orden`, `activo` |
| `Producto` | `productos` | Producto electrónico | `nombre`, `precio` (BigDecimal 12,2), `stock`, `imagen`, `video`, `videoDuration`, `videoStartTime`, `categoria` |
| `Carrito` | `carritos` | **1 fila = 1 línea de carrito** (producto + cantidad + usuario) | `cantidad`, `precioUnitario` (precio congelado), `fecha` |
| `Pedido` | `pedidos` | Pedido confirmado | `estado`, `total`, `nota`, `items` (JSONB) |
| `Comentario` | `comentarios` | Reseña + calificación de un producto | `puntuacion` (1–5), `contenido` |

### Relaciones

```
Perfil   1 ─── * Pedido / Carrito / Comentario   (usuario_id)
Producto 1 ─── * Carrito / Comentario            (producto_id)
Categoria 1 ─── * Producto                        (categoria_id, opcional)
Pedido: sus ítems NO son una tabla → se guardan como JSONB (clase ItemPedidoJson)
```

### Enumeraciones
- `Rol`: `CLIENTE`, `ADMINISTRADOR`.
- `EstadoPedido`: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`.

### Detalle importante del Pedido
Los ítems del pedido viven dentro de la misma fila como **JSONB**, no en una tabla aparte.
Cada ítem (`ItemPedidoJson`) guarda un *snapshot* del producto en el momento de la compra:
`productoId`, `nombre`, `cantidad`, `precio`. Así el pedido no cambia aunque el catálogo
cambie después.

---

## 3. Autenticación y seguridad (JWT)

- **Modelo 100% stateless**: el servidor no guarda sesiones; cada petición viaja con su
  token en el header `Authorization: Bearer <token>`.
- **Token HS256**, validez **24 h** (`app.jwt.expiracion-ms=86400000`).
- El **subject del token es el correo**; lleva un claim `rol` con el nombre del rol.

### Componentes (`security/`)
| Archivo | Rol |
|---|---|
| `SecurityConfig` | Cadena de filtros: CORS, **CSRF off**, `STATELESS`, registro del `JwtAuthFilter`, y las reglas de acceso por ruta. Define `BCryptPasswordEncoder` y CORS para `localhost:*` y `127.0.0.1:*`. |
| `JwtUtil` | Genera y valida tokens (`generarToken`, `obtenerCorreo`, `obtenerRol`). Lee el secreto de `app.jwt.secret`. |
| `JwtAuthFilter` | Filtro `OncePerRequestFilter`: lee el token, lo valida y construye la autoridad `ROLE_CLIENTE` / `ROLE_ADMINISTRADOR` en el `SecurityContext`. Si el token es inválido, solo limpia el contexto (la autorización rechaza después). |
| `UsuarioActual` | Helper que resuelve el `Perfil` completo del usuario autenticado a partir del correo (principal del contexto). Lo usan los servicios de carrito/pedido/comentario. |

### Reglas de acceso por ruta
- **Público (sin token):** `POST/GET /api/v1/auth/**`, `GET /api/v1/productos/**`, `GET /api/v1/categorias/**`.
- **Solo ADMINISTRADOR:** CRUD de productos (POST/PUT/DELETE `/api/v1/productos/**`) y `GET /api/v1/perfiles`.
- **Autenticado (cualquier rol):** carrito, pedidos y creación de comentarios. Lo demás cae en `anyRequest().authenticated()`.

> Adicionalmente, el `PedidoService` aplica una regla de permiso a nivel de negocio:
> un pedido en detalle solo lo puede ver el **dueño** o un **ADMINISTRADOR**.

---

## 4. Manejo de errores (`exception/`)

- `ApiException`: excepción de negocio con `HttpStatus` + mensaje. Los servicios la lanzan
  para errores 400/401/403/404/409/...
- `GlobalExceptionHandler` (`@RestControllerAdvice`), con 3 casos:
  - `ApiException` → su propio status + mensaje.
  - `MethodArgumentNotValidException` (fallos de `@Valid`) → **400** con el primer error `"campo: mensaje"`.
  - Cualquier otra excepción → **500** "Error interno del servidor" (sin exponer detalles internos).

Formato JSON de error uniforme en toda la API:
```json
{ "codigo": 404, "mensaje": "Producto no encontrado" }
```

---

## 5. Lógica de negocio por módulo (`service/`)

### PerfilService (auth y usuarios)
- `registrar`: valida correo no duplicado (**409** si existe), concatena `nombre + apellido`
  en `nombreCompleto`, hashea la contraseña con **BCrypt** y asigna siempre `Rol.CLIENTE`.
- `iniciarSesion`: valida credenciales (401 si falla) y devuelve el JWT + datos básicos del usuario.
- `listarUsuarios`: catálogo de usuarios para el panel admin (solo accesible por rol, definido en SecurityConfig).

### ProductoService (catálogo)
- `listar`: paginado con filtros por **categoría** y **búsqueda por nombre** (case-insensitive),
  combinables. `pagina` mínimo 1, `limite` default 10.
- `buscarEntidad(id)`: método interno compartido con Carrito/Pedido/Comentario como única
  fuente de validación de 404 de producto.
- Crear/actualizar valida que la `categoriaId` exista si viene (400 si no).
- **No valida stock ni precio al crear** (eso se valida en carrito y pedido).

### CarritoService (línea por línea)
- **Cada fila de `carritos` = un producto del carrito de un usuario.** El carrito de un
  usuario es el conjunto de todas sus filas.
- `agregarProducto`: si ya existe la línea, **suma la cantidad**; si es nueva, crea la fila
  congelando el `precioUnitario` con el precio actual del producto.
- `actualizarCantidad` / `eliminarProducto`: operan sobre la línea; 404 si el producto no está en el carrito.
- **Regla de stock:** si `cantidadDeseada > producto.getStock()` → **409 "Stock insuficiente"**
  (se valida contra el stock actual; todavía no se descuenta nada).

### PedidoService (checkout) — `@Transactional`
Convierte el **carrito actual** en un pedido en una sola transacción:
1. Carrito vacío → **400 "El carrito esta vacio"**.
2. Crea el `Pedido` con estado `PENDIENTE` y la `nota` del request.
3. Recorre las líneas del carrito: si `cantidad > stock` → **409** y la transacción **revierte todo**.
4. Si todo pasa: **descuenta stock**, arma los ítems JSONB (snapshot), acumula el `total`.
5. Guarda el pedido y **vacía el carrito**.

Detalle de permisos: `obtenerDetalle` devuelve 404 si no existe, y **403** si quien lo pide
no es el dueño ni admin. **No existe endpoint para cambiar el estado del pedido** en la API.

### ComentarioService
- `crear`: valida que el producto exista (404) y guarda la reseña con el usuario autenticado.
- `listarDeProducto`: comentarios ordenados por fecha descendente (sin paginación).

---

## 6. Endpoints de la API

| Módulo | Método | Ruta | Acceso |
|---|---|---|---|
| Auth | POST | `/api/v1/auth/register` | Público |
| Auth | POST | `/api/v1/auth/login` | Público |
| Productos | GET | `/api/v1/productos` (params: `categoria`, `buscar`, `pagina`, `limite`) | Público |
| Productos | GET | `/api/v1/productos/{id}` | Público |
| Productos | POST | `/api/v1/productos` | ADMIN |
| Productos | PUT | `/api/v1/productos/{id}` | ADMIN |
| Productos | DELETE | `/api/v1/productos/{id}` | ADMIN |
| Categorías | GET | `/api/v1/categorias` | Público |
| Perfiles | GET | `/api/v1/perfiles` | ADMIN |
| Carrito | GET | `/api/v1/carrito` | Autenticado |
| Carrito | POST | `/api/v1/carrito/items` | Autenticado |
| Carrito | PUT | `/api/v1/carrito/items/{productoId}` | Autenticado |
| Carrito | DELETE | `/api/v1/carrito/items/{productoId}` | Autenticado |
| Pedidos | POST | `/api/v1/pedidos` (solo `{ "nota": "..." }`) | Autenticado |
| Pedidos | GET | `/api/v1/pedidos` | Autenticado |
| Pedidos | GET | `/api/v1/pedidos/{id}` | Dueño o ADMIN |
| Comentarios | POST | `/api/v1/productos/{productoId}/comentarios` | Autenticado |
| Comentarios | GET | `/api/v1/productos/{productoId}/comentarios` | Público |

---

## 7. DTOs principales (formato JSON)

**Requests:**
- `RegisterRequest`: `nombre`, `apellido`, `correo` (@Email), `password`.
- `LoginRequest`: `correo`, `password`.
- `ProductoRequest`: `nombre`, `descripcion`, `precio`, `stock`, `imagen`, `video`, `videoDuration`, `videoStartTime`, `categoriaId`.
- `ItemCarritoRequest`: `productoId`, `cantidad` (≥1). · `CantidadRequest`: `cantidad`.
- `ComentarioRequest`: `puntuacion` (1–5), `contenido`.
- `PedidoRequest`: `nota` (opcional).

**Responses:**
- `LoginResponse`: `{ token, usuario: { id, nombre, rol } }`.
- `ProductoResponse`: `{ id, nombre, descripcion, precio, stock, imagen, video, videoDuration, videoStartTime, categoria }` → `categoria` es el **nombre** (String), no el objeto.
- `CatalogoResponse`: `{ pagina, total, productos: [ProductoResponse] }`.
- `CarritoResponse`: `{ items: [{ productoId, nombre, precio, cantidad, subtotal }], total }`.
- `PedidoCreadoResponse`: `{ id, estado, total, mensaje }`. · `PedidoDetalleResponse`: `{ id, estado, total, items }`.
- `ComentarioResponse`: `{ id, usuario (nombreCompleto), puntuacion, contenido, fecha }`.
- `MensajeResponse`: `{ mensaje }`.
- Error estándar: `{ codigo, mensaje }`.

Las respuestas multilínea de carrito tienen formas específicas: POST devuelve `{ mensaje, total }`
y el PUT `{ mensaje, subtotal, total }`.

---

## 8. Configuración

`application.properties` importa las variables desde un archivo `.env` en la raíz
(`spring.config.import=optional:file:.env`), que **nunca se sube al repositorio**:

```bash
SUPABASE_URL=https://<ref>.supabase.co
SUPABASE_DB_HOST=db.<ref>.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=<password>
JWT_SECRET_KEY=<clave de 32+ caracteres>
```

Datos de configuración clave:
- `spring.jpa.hibernate.ddl-auto=update` → Hibernate crea/ajusta las tablas automáticamente desde las entidades.
- `app.jwt.secret` y `app.jwt.expiracion-ms` (24 h) para el token.
- `server.port=8081`.

Arranque: `mvn spring-boot:run` (requiere Java 17 y Maven).

---

## 9. Decisiones de diseño a recordar

1. **JWT stateless** — sin sesiones en servidor, el token viaja en cada petición.
2. **El primer administrador se crea manualmente en BD** (no hay endpoint público para crear admins):
   ```sql
   UPDATE perfiles SET rol = 'ADMINISTRADOR' WHERE correo = 'admin@email.com';
   ```
3. **Carrito plano** — una fila por producto-usuario con el `precioUnitario` congelado al agregar.
4. **Ítems de pedido en JSONB** — snapshot de los productos, sin tabla intermedia.
5. **Checkout transaccional** — valida stock, descuenta stock y vacía el carrito en una sola transacción.
6. **Comentario + calificación en una misma tabla/endpoint** (puntuación + contenido juntos).
7. **Errores uniformes** — toda la API responde `{ codigo, mensaje }`.
8. **Sin endpoints** para: cambiar estado del pedido, editar perfil, CRUD de categorías, moderar comentarios ni checkout con pago.

---

## 10. Próximos pasos (pendientes)
- `lista_deseos` (está en el ER pero no en el contrato de API).
- CRUD de estado de pedidos (checkout/PAGADO nunca se asigna vía API).
- Tests unitarios de servicios y colección de Postman.