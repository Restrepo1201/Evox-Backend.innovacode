# Evox Backend - Plataforma de Venta de Productos Electrónicos

Backend en **Spring Boot 3 + Java 17 + MySQL**, construido siguiendo:
- El **Documento de Contratos de API REST** (rutas, JSON de entrada/salida, códigos HTTP).
- El **diagrama entidad-relación** (usuario, producto, comentario/calificación, carrito, pedidos).
- El **diagrama de casos de uso** (roles: usuario final y administrador).

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
- Maven (o usar el `mvnw` si lo agregas con `mvn wrapper:wrapper`)
- MySQL corriendo localmente (o cambiar la URL en `application.properties`)

## 2. Configuración
Edita `src/main/resources/application.properties`:
- `spring.datasource.username` / `spring.datasource.password`: tus credenciales de MySQL.
- `app.jwt.secret`: cámbiala por una clave propia y larga antes de subir a producción.

No necesitas crear la base de datos a mano: `createDatabaseIfNotExist=true` la crea sola,
y `spring.jpa.hibernate.ddl-auto=update` crea las tablas automáticamente a partir de las
entidades la primera vez que ejecutes la aplicación.

## 3. Ejecutar el proyecto
```bash
mvn spring-boot:run
```
El servidor queda disponible en `http://localhost:8080`.

## 4. Cómo probar rápido (con Postman o curl)

1. **Registrarse**
   `POST /api/v1/auth/register`
   ```json
   { "nombre": "Luna", "apellido": "Barreto", "correo": "luna@email.com", "password": "123456" }
   ```

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

5. **Crear un producto** (necesita un usuario con rol ADMINISTRADOR).
   Para tener un administrador, regístrate como cliente y luego, directamente en la base
   de datos, cambia su columna `rol` de `CLIENTE` a `ADMINISTRADOR`:
   ```sql
   UPDATE usuario SET rol = 'ADMINISTRADOR' WHERE correo = 'admin@email.com';
   ```
   Vuelve a iniciar sesión con ese usuario para obtener un token con el rol actualizado.

## 5. Endpoints implementados (igual que el contrato de API)

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

## 6. Decisiones tomadas para mantenerlo simple
- **Lombok** (`@Data`) genera los getters/setters automáticamente para no llenar las
  entidades y DTOs de código repetido. Si usas IntelliJ o VS Code, instala el plugin de
  Lombok para que el editor lo entienda.
- **Comentario + Calificación** se guardan en una sola tabla (`comentario`), porque el
  contrato de API los pide juntos en un mismo endpoint (`puntuacion` + `contenido`).
- **JWT** es "stateless": el servidor no guarda sesiones, cada petición trae su propio
  token en el header `Authorization`.
- El primer administrador se crea manualmente en la base de datos (no hay un endpoint
  público para crear admins, por seguridad).

## 7. Próximos pasos sugeridos
- Agregar `lista_deseos` (aparece en el ER pero no en el contrato de API todavía).
- Escribir la colección de Postman para probar cada endpoint (el documento maestro lo pide).
- Añadir tests unitarios de los servicios.
