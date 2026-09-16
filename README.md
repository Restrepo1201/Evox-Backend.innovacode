# Evox Backend - Plataforma de Venta de Productos Electrónicos

Backend en **Spring Boot 3 + Java 17 + PostgreSQL (Supabase)**, construido siguiendo:
- El **Documento de Contratos de API REST** (rutas, JSON de entrada/salida, códigos HTTP).
- El **diagrama entidad-relación** (perfil, producto, comentario/calificación, carrito, pedidos).
- El **diagrama de casos de uso** (roles: cliente y administrador).

El código está organizado en capas simples, fáciles de seguir:

```
model/       -> Entidades JPA (las tablas de la base de datos)
repository/  -> Acceso a datos (JpaRepository de Spring)
dto/         -> Los "moldes" JSON que entran y salen de la API
service/     -> La lógica de negocio (reglas, validaciones)
controller/  -> Los endpoints REST (lo que expone la URL)
security/    -> Login con JWT y control de roles
exception/   -> Manejo centralizado de errores
```

## 1. Requisitos previos
- Java 17
- Maven (o IntelliJ, que trae Maven integrado)
- Un proyecto en **Supabase** (PostgreSQL) con las tablas creadas

## 2. Configuración

Crea un archivo **`.env` en la raíz del proyecto** (ya está ignorado por `.gitignore`,
nunca lo subas al repositorio):

```bash
# Supabase Configuration
SUPABASE_URL=https://<ref>.supabase.co

# Conexion JDBC de la base PostgreSQL de Supabase
SUPABASE_DB_HOST=db.<ref>.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=<password_real_de_supabase>

# JWT Secret (clave de 32+ caracteres)
JWT_SECRET_KEY=<clave_segura_de_al_menos_32_caracteres>
```

`src/main/resources/application.properties` apunta a esas variables:

```properties
spring.config.import=optional:file:.env
spring.datasource.url=jdbc:postgresql://${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT}/${SUPABASE_DB_NAME}
spring.datasource.username=${SUPABASE_DB_USER}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
app.jwt.secret=${JWT_SECRET_KEY}
app.jwt.expiracion-ms=86400000
```

> El valor de `SUPABASE_DB_PASSWORD` es el **password de la base de datos** (Settings → Database → Connection string), no la API key.

## 3. Tablas y entidades

El backend espera estas tablas en Supabase (Hibernate puede crearlas/completarlas con
`ddl-auto=update`, pero si ya las tienes con su DDL, se usa tu esquema):

| Tabla        | Entidad    | Notas                                                     |
|--------------|------------|-----------------------------------------------------------|
| `perfiles`   | `Perfil`   | id UUID, correo, nombre_completo, rol, password*          |
| `categorias` | `Categoria`| id UUID, nombre, descripcion, orden, activo               |
| `productos`  | `Producto` | id UUID, nombre, descripcion, precio, stock, imagen, `categoria_id` → categorias |
| `carritos`   | `Carrito`  | 1 fila = 1 producto con su cantidad y `precio_unitario`   |
| `pedidos`    | `Pedido`   | items guardados como **JSONB**                            |
| `comentarios`| `Comentario`| puntuacion + contenido                                    |

**Ojo:** `perfiles` no incluye `password` en el DDL original del esquema. El login del
backend usa BCrypt, así que la columna debe existir. Si arrancas con `ddl-auto=update`,
Hibernate la agrega automáticamente; si prefieres crearla a mano:

```sql
ALTER TABLE perfiles ADD COLUMN password TEXT;
```

Los `id` son `UUID` generados por la aplicación (`@UuidGenerator`), así que el valor
`DEFAULT auth.uid()` o `gen_random_uuid()` de Supabase no interfiere.

## 4. Ejecutar el proyecto
```bash
mvn spring-boot:run
```
El servidor queda disponible en `http://localhost:8080`.

## 5. Cómo probar rápido (con Postman o curl)

1. **Registrarse**
   `POST /api/v1/auth/register`
   ```json
   { "nombre": "Luna", "apellido": "Barreto", "correo": "luna@email.com", "password": "123456" }
   ```
   Su nombre completo se guarda como `Luna Barreto` en `perfiles.nombre_completo`.

2. **Iniciar sesión** (te devuelve el token)
   `POST /api/v1/auth/login`
   ```json
   { "correo": "luna@email.com", "password": "123456" }
   ```
   Copia el `token` de la respuesta.

3. **Usar el token** en cada petición protegida, en el header:
   ```
   Authorization: Bearer <el_token_copiado>
   ```

4. **Ver el catálogo** (público, no necesita token)
   `GET /api/v1/productos`

5. **Crear un producto** (necesita `categoriaId` existente y rol ADMINISTRADOR).
   Para tener un administrador, regístrate como cliente y luego, directamente en la base
   de datos, cambia su `rol` de `CLIENTE` a `ADMINISTRADOR`:
   ```sql
   UPDATE perfiles SET rol = 'ADMINISTRADOR' WHERE correo = 'admin@email.com';
   ```
   Vuelve a iniciar sesión con ese usuario para obtener un token con el rol actualizado.

## 6. Endpoints implementados (igual que el contrato de API)

| Módulo      | Método | Ruta                                         | Acceso                  |
|-------------|--------|-----------------------------------------------|--------------------------|
| Auth        | POST   | /api/v1/auth/register                        | Público                  |
| Auth        | POST   | /api/v1/auth/login                           | Público                  |
| Productos   | GET    | /api/v1/productos                            | Público                  |
| Productos   | GET    | /api/v1/productos/{id}                       | Público                  |
| Productos   | POST   | /api/v1/productos                            | ADMINISTRADOR            |
| Productos   | PUT    | /api/v1/productos/{id}                       | ADMINISTRADOR            |
| Productos   | DELETE | /api/v1/productos/{id}                       | ADMINISTRADOR            |
| Carrito     | GET    | /api/v1/carrito                              | Cliente autenticado      |
| Carrito     | POST   | /api/v1/carrito/items                        | Cliente autenticado      |
| Carrito     | PUT    | /api/v1/carrito/items/{productoId}           | Cliente autenticado      |
| Carrito     | DELETE | /api/v1/carrito/items/{productoId}           | Cliente autenticado      |
| Pedidos     | POST   | /api/v1/pedidos                              | Cliente autenticado      |
| Pedidos     | GET    | /api/v1/pedidos                              | Cliente autenticado      |
| Pedidos     | GET    | /api/v1/pedidos/{id}                         | Dueño o ADMINISTRADOR    |
| Comentarios | POST   | /api/v1/productos/{id}/comentarios           | Cliente autenticado      |
| Comentarios | GET    | /api/v1/productos/{id}/comentarios           | Público                  |

Cambios respecto al esquema anterior (MySQL):
- Todos los `{id}` son **UUID** (producto, pedido, productoId del carrito).
- `POST /api/v1/pedidos` recibe solo `{ "nota": "..." }` (el pedido se arma con el carrito actual).
- La fecha de pedidos/comentarios es `ISO-8601 con zona horaria` (ej. `2026-09-16T16:00:00Z`), no solo la fecha.

## 7. Decisiones tomadas para mantenerlo simple
- **Lombok** (`@Data`) genera los getters/setters automáticamente para no llenar las
  entidades y DTOs de código repetido. Si usas IntelliJ o VS Code, instala el plugin de
  Lombok para que el editor lo entienda.
- **Comentario + Calificación** se guardan en una sola tabla (`comentarios`), porque el
  contrato de API los pide juntos en un mismo endpoint (`puntuacion` + `contenido`).
- **Carrito plano**: cada fila de `carritos` es un producto del carrito del cliente, con
  el `precio_unitario` del momento en que se agregó.
- **JWT** es "stateless": el servidor no guarda sesiones, cada petición trae su propio
  token en el header `Authorization`.
- El primer administrador se crea manualmente en la base de datos (no hay un endpoint
  público para crear admins, por seguridad).

## 8. Próximos pasos sugeridos
- Agregar `lista_deseos` (aparece en el ER pero no en el contrato de API todavía).
- Escribir la colección de Postman para probar cada endpoint (el documento maestro lo pide).
- Añadir tests unitarios de los servicios.