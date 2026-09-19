# Prompt: generar los servicios del frontend (React + TypeScript)

Copia y pega este bloque completo como prompt (para un asistente de IA o para un dev que
trabaje con los services del frontend). Está pensado para **React + TypeScript + axios**,
pero los contratos JSON son el punto de referencia sin importar el framework.

> El backend corre **local** en `http://localhost:8080` y ya permite CORS para
> `http://localhost:*`. Solo la base de datos está en línea (Supabase), el frontend no
> interactúa con Supabase directamente: **todo sale del backend local**.

---

```
Vas a generar la capa de servicios y tipos para el frontend React + TypeScript de una
tienda electrónica. La API vive en http://localhost:8080/api/v1, el backend corre local
y permite CORS para http://localhost:* (no hace falta proxy). Todos los ids son UUID
(string).

REQUISITOS:
1. Tipos TypeScript en src/types/api.ts que reflejen EXACTAMENTE los JSON de abajo.
2. Cliente axios en src/api/client.ts:
   - baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'
   - timeout de 15000ms, y un interceptador de request que agrega el header
     Authorization: Bearer <token> desde localStorage ('evox_token') si existe.
   - interceptador de respuesta que en 401 limpia el token y dispara un evento
     'evox:logout' (el componente raíz escucha para redirigir a /login).
3. Función getMensajeError(err) que extrae el mensaje backend: los errores llegan como
   { codigo: number, mensaje: string } (ej. codigo 400, 401, 403, 404, 409, 500).
4. Un archivo de servicio por módulo, todos con firmas tipadas y async:
   - src/api/authService.ts
   - src/api/productoService.ts
   - src/api/carritoService.ts
   - src/api/pedidoService.ts
   - src/api/comentarioService.ts
5. No implementes rutas ni componentes: SOLO servicios + tipos.

CONTRATO JSON DE LA API (fuente de verdad):

POST /auth/register  (público)
  req:  { nombre: string, apellido: string, correo: string, password: string }
  res 201: { id: string, nombreCompleto: string, correo: string, rol: 'CLIENTE' | 'ADMINISTRADOR' }
  error 409 si el correo ya existe; 400 con mensaje "campo: motivo" si falta algo.

POST /auth/login  (público)
  req:  { correo: string, password: string }
  res 200: { token: string, usuario: { id: string, nombre: string, rol: 'CLIENTE' | 'ADMINISTRADOR' } }
  error 401 credenciales incorrectas; 400 campos vacíos.

GET /productos?categoria=&buscar=&pagina=1&limite=10  (público)
  res 200: {
    pagina: number, total: number,
    productos: [{ id: string, nombre: string, descripcion: string|null,
                  precio: number, stock: number, imagen: string|null, categoria: string|null }]
  }

GET /productos/{id}  (público)
  res 200: objeto Producto igual que arriba.
  error 404 si no existe.

POST /productos  (rol ADMINISTRADOR)
  req:  { nombre: string, descripcion?: string|null, precio: number, stock: number,
          imagen?: string|null, categoriaId?: string|null }
  res 201: { mensaje: string }
  error 400 si categoriaId no existe o el payload no va válido.

PUT /productos/{id}  (ADMINISTRADOR)
  req: misma forma que POST.
  res 200: { mensaje: string }

DELETE /productos/{id}  (ADMINISTRADOR)
  res 200: { mensaje: string }

GET /carrito  (token)
  res 200: { items: [{ productoId: string, nombre: string, precio: number,
                       cantidad: number, subtotal: number }], total: number }

POST /carrito/items  (token)
  req:  { productoId: string, cantidad: number }
  res 201: { mensaje: string, total: number }
  error 400 campos nulos/o cantidad <= 0; 404 producto no existe; 409 stock insuficiente.

PUT /carrito/items/{productoId}  (token)
  req:  { cantidad: number }
  res 200: { mensaje: string, subtotal: number, total: number }
  error 404 si el producto no está en el carrito; 409 stock insuficiente.

DELETE /carrito/items/{productoId}  (token)
  res 200: { mensaje: string }

POST /pedidos  (token; convierte el carrito en pedido y luego LO VACÍA)
  req:  { nota?: string }
  res 201: { id: string, estado: 'PENDIENTE'|'PAGADO'|'ENVIADO'|'ENTREGADO',
             total: number, mensaje: string }
  error 400 carrito vacío; 409 stock insuficiente.

GET /pedidos  (token)
  res 200: [{ id: string, fecha: string /* ISO-8601 */, total: number, estado: ... }]

GET /pedidos/{id}  (token, dueño o admin)
  res 200: { id: string, estado: ..., total: number,
             items: [{ productoId: string, nombre: string, cantidad: number, precio: number }] }
  error 403 si no es dueño ni admin; 404 si no existe.

POST /productos/{productoId}/comentarios  (token)
  req:  { puntuacion: number /* 1-5 */, contenido: string }
  res 201: { id: string, usuario: string, puntuacion: number, contenido: string,
             fecha: string /* ISO-8601 */ }
  error 400 si puntuacion fuera de rango o contenido vacío.

GET /productos/{productoId}/comentarios  (público)
  res 200: listado igual que arriba, más recientes primero.
  error 404 si el producto no existe.

Formato de error de TODOS los endpoints:
  { codigo: number, mensaje: string }
Estados útiles: 400 validación, 401 no autenticado / token vencido,
403 sin permiso, 404 no encontrado, 409 conflicto (correo repetido / stock insuficiente),
500 error interno.

ENTREGABLES: los 5 archivos de servicio, el cliente axios y el archivo de tipos.
Adjunta las interfaces exactas para cada JSON y usa genéricos de axios para tipar.
Si algún endpoint devuelve 401, el interceptor ya se encarga de la sesión: no lo repitas.
```

---

### Recordatorios para el equipo

- Variables de entorno del frontend: `VITE_API_URL=http://localhost:8080/api/v1` (Vite) o `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1` (Next.js).
- El token se guarda como `evox_token` en localStorage.
- `fecha` y `fecha_creacion` llegan como ISO-8601 con `Z` (`2026-09-16T16:00:00Z`); formatear en la UI con `Intl.DateTimeFormat`.
- Precios: números decimales (ej. `1299.99`); formatear moneda en la UI (`Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' })` si aplica).
- El backend corre en local con el entorno listo: `JAVA_HOME=$(/usr/libexec/java_home -v 17)` y `$HOME/tools/apache-maven-3.9.16/bin/mvn spring-boot:run` desde la raíz del proyecto (puerto 8080).
- La única pieza que NO está en local es la base de datos (Supabase en línea); el frontend nunca la toca, solo habla con el backend.

JAVA_HOME=$(/usr/libexec/java_home -v 17) $HOME/tools/apache-maven-3.9.16/bin/mvn spring-boot:run




Listo. Todo verificado:
Lo que pediste (migrar cada artículo a BD con SQL) — hecho:
- Nuevo src/main/resources/db/seed-catalogo.sql (idempotente, se puede re-ejecutar sin duplicar): crea las 6 categorías y sembró los 33 productos del mock en productos con su nombre, descripción, precio, imagen, stock y categoría.
- Ejecutado contra Supabase: BD queda con 34 productos (33 nuevos + "Audifonos Sony") y 6 categorías. 2ª corrida = sigue en 34 (sin duplicados).
- Resultado: GET /productos ahora devuelve 34 con 21 con video (video: "/acer-n100-promo.mp4", etc.).
Sobre los videos (tu pregunta): No los metí como binario en la BD — lo correcto es que Postgres guarde la URL, y el archivo .mp4 siga siendo asset del frontend (public/). Para eso:
- Agregué columnas video, video_duration, video_start_time a productos (vía SQL y en la entidad JPA + DTO + API).
- catalogo.ts ahora prefiere video de la BD (p.video ?? mock?.video), así el hover de la tarjeta y el carrusel del detalle siguen funcionando igual que antes.
"Mismo resultado que antes": sí — como los nombres sembrados son idénticos a los del mock, el resto del toque visual (features, colores, galería, rating) sigue enriqueciéndose del mock que coincide, así que la página principal vuelve a mostrar presencia real (antes solo salían 1-2 productos).
Además corregí: Catalog.tsx/Landing.tsx usaban cat.count (ya no existe); ahora el conteo por categoría sale de los datos reales. tsc y build limpios. Reinicié tu backend (ahora PID 56546); tu dev server en :8443 ya tiene los cambios por HMR — recarga el navegador para ver el catálogo completo.