# Contrato de API - Evox Backend

Referencia para el frontend. **Base URL:** `http://localhost:8080`
Todos los endpoints viven bajo `/api/v1`.

> Entorno: el backend corre **local** y la **sola dependencia externa es la base de datos PostgreSQL en Supabase (online)**. El frontend consume la API en `http://localhost:8080` directamente.

## CORS

El backend permite CORS para orígenes locales (`http://localhost:*` y `http://127.0.0.1:*`):
- Métodos permitidos: `GET, POST, PUT, DELETE, OPTIONS`.
- Headers permitidos: cualquier (`*`), incluido `Authorization`.
- Credenciales: `true`.

No hace falta proxy ni middleware de CORS en el frontend.

## Autenticación

El backend usa **JWT (Bearer)**:
1. `POST /auth/register` o `POST /auth/login` para obtener/renovar el token.
2. En cada petición protegida enviar el header `Authorization: Bearer <token>`.
3. Guardar el token en el cliente (localStorage/sessionStorage) y desloguear en 401.

**Roles:** `CLIENTE` (acciones de compra) y `ADMINISTRADOR` (gestionar productos).

**Formato de error (todos los endpoints):**
```json
{ "codigo": 404, "mensaje": "Producto no encontrado" }
```

| Código | Significado |
|--------|-------------|
| 400 | Validación o dato inválido (el mensaje viene como `campo: motivo`) |
| 401 | Credenciales incorrectas / token inválido o vencido |
| 403 | No tiene acceso al recurso / token ausente en ruta protegida |
| 404 | No encontrado |
| 409 | Conflicto (correo repetido, stock insuficiente) |
| 500 | Error interno |

> Los `{id}` de producto, pedido y carrito son **UUID**.

---

## Resumen de endpoints

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/api/v1/auth/register` | Público |
| POST | `/api/v1/auth/login` | Público |
| GET | `/api/v1/productos` | Público |
| GET | `/api/v1/productos/{id}` | Público |
| POST | `/api/v1/productos` | ADMINISTRADOR |
| PUT | `/api/v1/productos/{id}` | ADMINISTRADOR |
| DELETE | `/api/v1/productos/{id}` | ADMINISTRADOR |
| GET | `/api/v1/carrito` | Token |
| POST | `/api/v1/carrito/items` | Token |
| PUT | `/api/v1/carrito/items/{productoId}` | Token |
| DELETE | `/api/v1/carrito/items/{productoId}` | Token |
| POST | `/api/v1/pedidos` | Token |
| GET | `/api/v1/pedidos` | Token |
| GET | `/api/v1/pedidos/{id}` | Token (dueño o admin) |
| POST | `/api/v1/productos/{productoId}/comentarios` | Token |
| GET | `/api/v1/productos/{productoId}/comentarios` | Público |

---

## Auth

### POST /api/v1/auth/register — Público
Registra un cliente (su nombre completo se compone de nombre + apellido).

**Request:**
```json
{ "nombre": "Luna", "apellido": "Barreto", "correo": "luna@email.com", "password": "123456" }
```

**Response 201:**
```json
{
  "id": "3f4c...-uuid",
  "nombreCompleto": "Luna Barreto",
  "correo": "luna@email.com",
  "rol": "CLIENTE"
}
```
**Errores:** `409` si el correo ya existe. `400` si campos inválidos.

### POST /api/v1/auth/login — Público
**Request:**
```json
{ "correo": "luna@email.com", "password": "123456" }
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": { "id": "3f4c...-uuid", "nombre": "Luna Barreto", "rol": "CLIENTE" }
}
```

---

## Productos

### GET /api/v1/productos — Público
Catálogo paginado con filtros opcionales.

**Query params:** `categoria` (nombre), `buscar` (subcadena del nombre), `pagina` (default 1), `limite` (default 10).

**Response 200:**
```json
{
  "pagina": 1,
  "total": 12,
  "productos": [
    {
      "id": "3f4c...-uuid",
      "nombre": "Audífonos Bluetooth",
      "descripcion": "Inalámbricos, 40h de batería",
      "precio": 1299.99,
      "stock": 10,
      "imagen": "https://.../imagen.jpg",
      "categoria": "Audio"
    }
  ]
}
```
`categoria` y `imagen` pueden ser `null`.

### GET /api/v1/productos/{id} — Público
**Response 200:** el mismo objeto `ProductoResponse` de arriba.
**Errores:** `404` si no existe.

### POST /api/v1/productos — ADMINISTRADOR
**Request:**
```json
{
  "nombre": "Audífonos Bluetooth",
  "descripcion": "Inalámbricos",
  "precio": 1299.99,
  "stock": 10,
  "imagen": "https://.../imagen.jpg",
  "categoriaId": "uuid-de-la-categoria"
}
```
`imagen` y `categoriaId` opcionales (`null`). Si `categoriaId` no existe → `400`.

**Response 201:**
```json
{ "mensaje": "Producto creado correctamente" }
```

### PUT /api/v1/productos/{id} — ADMINISTRADOR
Request: misma forma que POST. **Response 200:**
```json
{ "mensaje": "Producto actualizado correctamente" }
```

### DELETE /api/v1/productos/{id} — ADMINISTRADOR
**Response 200:**
```json
{ "mensaje": "Producto eliminado correctamente" }
```

---

## Carrito (requiere token)

### GET /api/v1/carrito
**Response 200:**
```json
{
  "items": [
    {
      "productoId": "3f4c...-uuid",
      "nombre": "Audífonos Bluetooth",
      "precio": 1299.99,
      "cantidad": 2,
      "subtotal": 2599.98
    }
  ],
  "total": 2599.98
}
```
`precio` es el `precio_unitario` guardado al agregar el producto.

### POST /api/v1/carrito/items
**Request:**
```json
{ "productoId": "3f4c...-uuid", "cantidad": 1 }
```
Si el producto ya está en el carrito, suma la cantidad.
**Response 201:**
```json
{ "mensaje": "Producto agregado al carrito", "total": 2599.98 }
```
**Errores:** `400` productoId/cantidad nulos o cantidad ≤ 0, `404` producto no existe, `409` stock insuficiente.

### PUT /api/v1/carrito/items/{productoId}
**Request:**
```json
{ "cantidad": 3 }
```
**Response 200:**
```json
{ "mensaje": "Cantidad actualizada", "subtotal": 3899.97, "total": 3899.97 }
```
**Errores:** `404` si no está en el carrito, `409` stock insuficiente.

### DELETE /api/v1/carrito/items/{productoId}
**Response 200:**
```json
{ "mensaje": "Producto eliminado del carrito" }
```

---

## Pedidos (requiere token)

### POST /api/v1/pedidos
Convierte el **carrito actual** en un pedido y lo vacía. Solo recibe nota opcional.

**Request:**
```json
{ "nota": "Entregar en la tarde" }
```

**Response 201:**
```json
{
  "id": "3f4c...-uuid",
  "estado": "PENDIENTE",
  "total": 2599.98,
  "mensaje": "Pedido creado correctamente"
}
```
**Errores:** `400` carrito vacío. `409` stock insuficiente para algún producto.

Estados: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`.

### GET /api/v1/pedidos
Lista los pedidos del usuario autenticado (más reciente primero).

**Response 200:**
```json
[
  {
    "id": "3f4c...-uuid",
    "fecha": "2026-09-16T16:00:00Z",
    "total": 2599.98,
    "estado": "PENDIENTE"
  }
]
```

### GET /api/v1/pedidos/{id}
Solo el dueño del pedido o un ADMINISTRADOR.

**Response 200:**
```json
{
  "id": "3f4c...-uuid",
  "estado": "PENDIENTE",
  "total": 2599.98,
  "items": [
    { "productoId": "3f4c...-uuid", "nombre": "Audífonos Bluetooth", "cantidad": 2, "precio": 1299.99 }
  ]
}
```
**Errores:** `404` no existe, `403` acceso denegado.

---

## Comentarios

### POST /api/v1/productos/{productoId}/comentarios — requiere token
**Request:**
```json
{ "puntuacion": 5, "contenido": "Excelente calidad" }
```
`puntuacion` 1–5, `contenido` no vacío.

**Response 201:**
```json
{
  "id": "3f4c...-uuid",
  "usuario": "Luna Barreto",
  "puntuacion": 5,
  "contenido": "Excelente calidad",
  "fecha": "2026-09-16T16:05:00Z"
}
```

### GET /api/v1/productos/{productoId}/comentarios — Público
**Response 200:** lista del mismo objeto anterior (más reciente primero). `404` si el producto no existe.