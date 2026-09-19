# Manual del Tester — Evox (Frontend + Backend)

Guía paso a paso para que un tester pueda **bajar el código, instalar dependencias y
correr el proyecto en local**, tanto el **frontend** (Diseño UI_UX / InnovaCode-Frontend)
como el **backend** (Evox-Backend).

> **Puertos y URLs:**
> - Backend → `http://localhost:8081`
> - Frontend → `http://localhost:8443`

---

## 0. Inicio rápido (solo comandos)

Si ya tienes **Java 17, Maven, Node 20+, pnpm y Git** instalados, copia y pega esto.

**macOS:**
```bash
# 1. Descargar
mkdir -p ~/Documents/Evox && cd ~/Documents/Evox
git clone https://github.com/innovacode2026/Evox-Backend.innovacode.git Evox-Backend
cd Evox-Backend && git checkout feature/migracion-supabase && cd ..
git clone https://github.com/innovacode2026/InnovaCode-Frontend.git "Diseño UI_UX"
cd "Diseño UI_UX" && git checkout main && cd ..

# 2. Backend (crea el archivo .env con las credenciales antes de arrancar)
cd ~/Documents/Evox/Evox-Backend   # <-- crea aqui el archivo .env (ver seccion 3.1)
mvn dependency:resolve
mvn spring-boot:run                 # http://localhost:8081

# 3. Frontend (en OTRA terminal)
cd ~/Documents/Evox/Diseño\ UI_UX   # <-- crea aqui el archivo .env con VITE_API_URL
pnpm install
pnpm dev                            # http://localhost:8443
```

**Windows (PowerShell):**
```powershell
# 1. Descargar
New-Item -ItemType Directory -Path C:\Evox
Set-Location C:\Evox
git clone https://github.com/innovacode2026/Evox-Backend.innovacode.git Evox-Backend
Set-Location C:\Evox\Evox-Backend; git checkout feature/migracion-supabase; Set-Location C:\Evox
git clone https://github.com/innovacode2026/InnovaCode-Frontend.git "Diseno-UI-UX"
Set-Location C:\Evox\Diseno-UI-UX; git checkout main; Set-Location C:\Evox

# 2. Backend (crea el archivo .env con las credenciales antes de arrancar)
Set-Location C:\Evox\Evox-Backend   # <-- crea aqui el archivo .env (ver seccion 3.1)
mvn dependency:resolve
mvn spring-boot:run                 # http://localhost:8081

# 3. Frontend (en OTRA terminal)
Set-Location C:\Evox\Diseno-UI-UX   # <-- crea aqui el archivo .env con VITE_API_URL
pnpm install
pnpm dev                            # http://localhost:8443
```

**Checklist de arranque:**
- [ ] Crea `.env` del backend con `SUPABASE_*` y `JWT_SECRET_KEY` → `docs/MANUAL_TESTER.md:§3.1`
- [ ] Crea `.env` del frontend con `VITE_API_URL=http://localhost:8081/api/v1` → §4.1
- [ ] Backend responde en `http://localhost:8081/api/v1/productos`
- [ ] Frontend abre en `http://localhost:8443`

---

## Pasos detallados

En cada paso se muestra el comando para **macOS** y para **Windows**.

> 💡 **Recomendación para Windows:** instala **Git Bash** (viene con Git). Git Bash te
> permite usar los mismos comandos de macOS/Linux, lo que evita problemas con las comillas
> de `curl`, rutas y otros. La mayoría de ejemplos de esta guía funcionan igual en Git Bash.
> Si prefieres **PowerShell / CMD**, usa las variantes `Windows` que se indican.

---

## 1. Requisitos previos (instalar herramientas)

Antes de empezar, verifica que tienes instalado lo siguiente. Abre una terminal y ejecuta
cada comando; debe mostrar una versión sin error.

### 1.0 Instalar Homebrew (solo macOS)

Todos los comandos `brew install ...` de este manual necesitan **Homebrew**. Si al ejecutar
`brew --version` te sale **`zsh: command not found: brew`**, instálalo así:

1. Instala las herramientas de línea de comandos de Xcode:
   ```bash
   xcode-select --install
   ```
2. Instala Homebrew (script oficial):
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```
3. **Si tu Mac es Apple Silicon (chip M1/M2/M3/M4)**, Homebrew queda en `/opt/homebrew` y
   hay que activarlo en la terminal actual:
   ```bash
   echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
   eval "$(/opt/homebrew/bin/brew shellenv)"
   ```
   En Mac **Intel**, Homebrew queda en `/usr/local` y normalmente ya funciona sin este paso.
4. Verifica: `brew --version` → debe mostrar una versión (ya no "command not found").
5. **Cierra la terminal y abre una nueva** antes de continuar con los demás comandos.

### 1.1 Backend necesita

| Herramienta | Versión mínima | Comando para verificar | Instalación (macOS) | Instalación (Windows) |
|---|---|---|---|---|
| Java (JDK) | 17 | `java -version` | `brew install openjdk@17` | `winget install Microsoft.OpenJDK.17` |
| Maven | 3.8+ | `mvn -version` | `brew install maven` | `winget install Apache.Maven` |

> **Nota Windows:** después de instalar con `winget` cierra y vuelve a abrir la terminal
> para que `PATH` se actualice. Alternativa a winget: descargar los instaladores desde
> <https://adoptium.net> (Java 17) y <https://maven.apache.org> (Maven, versión binaria
> `.zip`, descomprime en `C:\maven` y agrega `C:\maven\bin` al PATH).

> **Alternativa:** si ya usas IntelliJ IDEA, este trae Maven integrado. Puedes correr el
> backend desde el IDE sin instalar Maven a parte (ver sección 3.4).

### 1.2 Frontend necesita

| Herramienta | Versión | Comando para verificar | Instalación (macOS) | Instalación (Windows) |
|---|---|---|---|---|
| Node.js | 20+ (sugerida 22) | `node -v` | `brew install node@22` | `winget install OpenJS.NodeJS.LTS` |
| pnpm | 9+ (sugerida 10) | `pnpm -v` | `npm install -g pnpm@10.34.3` | `npm install -g pnpm@10.34.3` |
| Git | — | `git --version` | `brew install git` | `winget install Git.Git` |

> Si en Windows no usaste winget, Node también se instala desde
> <https://nodejs.org> (versión LTS).

---

## 2. Bajar el código (clonar los repositorios)

1. Crea una carpeta para el proyecto.

   **macOS:**
   ```bash
   mkdir -p ~/Documents/Evox && cd ~/Documents/Evox
   ```

   **Windows (PowerShell):**
   ```powershell
   New-Item -ItemType Directory -Path C:\Evox
   Set-Location C:\Evox
   ```

   **Windows (Git Bash):**
   ```bash
   mkdir -p /c/Evox && cd /c/Evox
   ```

2. Clona el backend:
   ```bash
   git clone https://github.com/innovacode2026/Evox-Backend.innovacode.git Evox-Backend
   cd Evox-Backend
   git checkout feature/migracion-supabase   # rama de trabajo actual (tiene los últimos cambios)
   cd ..
   ```

3. Clona el frontend:
   ```bash
   git clone https://github.com/innovacode2026/InnovaCode-Frontend.git "Diseño UI_UX"
   cd "Diseño UI_UX" && git checkout main && cd ..
   ```

   > **Sugerencia Windows:** para evitar problemas con el espacio y la letra `ñ` en la
   > carpeta `Diseño UI_UX`, clónala con un nombre sin espacios:
   > ```bash
   > git clone https://github.com/innovacode2026/InnovaCode-Frontend.git "Diseno-UI-UX"
   > ```
   > Y usa `Diseno-UI-UX` en lugar de `Diseño UI_UX` en todos los comandos siguientes.

Quedará una estructura así:
```
~/Documents/Evox/  (macOS)  o  C:\Evox  (Windows)
├── Evox-Backend/      ← backend (Spring Boot)
└── Diseño UI_UX/      ← frontend (React + Vite)
```

---

## 3. Configurar el backend

El backend se conecta a una **base de datos PostgreSQL en Supabase** (online). La
configuración se guarda en un archivo `.env` que **no se sube al repositorio** (es secreto).

### 3.1 Crear el archivo `.env`

Pide el contenido del archivo `.env` al líder del equipo (o al desarrollador que tenga el
backend corriendo). Debe crearse en la **raíz de la carpeta `Evox-Backend`**.

Entra a la carpeta:

**macOS:**
```bash
cd ~/Documents/Evox/Evox-Backend
```

**Windows (PowerShell):**
```powershell
Set-Location C:\Evox\Evox-Backend
```

**Windows (Git Bash):**
```bash
cd /c/Evox/Evox-Backend
```

Crea un archivo llamado `.env` (en Windows puedes usar `notepad .env` dentro de la carpeta
y pegar lo siguiente). Reemplaza los valores con los reales:

```dotenv
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

> ⚠️ **Importante:** el valor de `SUPABASE_DB_PASSWORD` es el **password de la base de
> datos** (Supabase → Settings → Database → Connection string), no la API key. No
> compartas este archivo ni lo subas a GitHub.

### 3.2 Instalar dependencias

Los comandos son iguales en macOS y Windows (asegúrate de estar dentro de la carpeta
`Evox-Backend`):

```bash
mvn dependency:resolve
```

> La primera vez descarga todas las dependencias de Maven (puede tardar varios minutos).

### 3.3 Correr el backend

```bash
mvn spring-boot:run
```

**Señales de éxito:**
- El log termina con algo como `Started EvoxBackendApplication in X.X seconds`.
- Hibernate se conecta a Supabase y crea/actualiza las tablas automáticamente.

**Verificar** (abre una segunda terminal):

**macOS / Windows (Git Bash):**
```bash
curl http://localhost:8081/api/v1/productos
```

**Windows (PowerShell):**
```powershell
curl.exe http://localhost:8081/api/v1/productos
```

Debe responder un JSON (aunque sea `{"productos":[]}` o con datos).

> ⚠️ No cierres esta terminal mientras uses la app: el backend debe quedarse corriendo en
> segundo plano. Si algo falla, ve la sección 7 (Solución de problemas).

### 3.4 (Alternativa) Correr desde IntelliJ IDEA

1. Abre la carpeta `Evox-Backend` en IntelliJ (como proyecto Maven).
2. Espera a que se importen las dependencias.
3. Instala los plugins si te lo pide: **Lombok** (el proyecto lo usa).
4. Asegúrate de que el JDK seleccionado sea **17** (Project Structure → Project SDK).
5. Ejecuta la clase principal `EvoxBackendApplication` (botón ▶ verde).

---

## 4. Configurar el frontend

> 📌 En esta sección, la carpeta se llama `Diseño UI_UX` (macOS) o `Diseno-UI-UX` si
> seguiste la sugerencia de Windows de la sección 2. Ajusta los comandos según el nombre
> que usaste.

### 4.1 Crear el archivo `.env`

**macOS:**
```bash
cd ~/Documents/Evox/Diseño\ UI_UX
```

**Windows (PowerShell):**
```powershell
Set-Location C:\Evox\Diseno-UI-UX
```

**Windows (Git Bash):**
```bash
cd /c/Evox/Diseno-UI-UX
```

Crea un archivo `.env` (en Windows: `notepad .env`) en la **raíz de esa carpeta** con este
contenido (apunta al backend local):

```dotenv
VITE_API_URL=http://localhost:8081/api/v1
```

> 📌 La URL debe coincidir con el backend. Si el backend corre en otra máquina o puerto,
> ajusta este valor. Sin este archivo el frontend también funciona: cae a un default de
> `http://localhost:8081/api/v1`.

### 4.2 Instalar dependencias

Los comandos son iguales en ambos sistemas (estando dentro de la carpeta del frontend):

```bash
pnpm install
```

> Si `pnpm` no está instalado: `npm install -g pnpm@10.34.3`.

### 4.3 Correr el frontend

```bash
pnpm dev
```

**Señales de éxito:**
- Vite muestra algo como:
  ```
  VITE v8.x.x  ready in xxx ms
  ➜  Local:   http://localhost:8443/
  ```

**Verificar:** abre `http://localhost:8443` en el navegador → debe cargar la página de
Evox (Landing / catálogo).

> ⚠️ Mantén esta terminal abierta también. Con las dos terminales abiertas (backend +
> frontend) la app queda funcional.

---

## 5. Orden recomendado para probar

Configuración completa de la app:

| Terminal | Comando | Qué hace |
|---|---|---|
| Terminal A | `mvn spring-boot:run` (en `Evox-Backend`) | Inicia la API en el puerto 8081 |
| Terminal B | `pnpm dev` (en `Diseño UI_UX` o `Diseno-UI-UX`) | Inicia el frontend en el puerto 8443 |

### 5.1 Loop de prueba funcional básico

En el navegador (`http://localhost:8443`):

1. **Registrarse** como cliente (nombre, apellido, correo, contraseña).
2. **Iniciar sesión** con el correo/contraseña recién creados.
3. **Ver el catálogo** de productos (público, también funciona sin loguearse).
4. **Agregar un producto al carrito** (requiere sesión iniciada).
5. **Crear un pedido** desde el carrito.

### 5.2 Probar la API directamente (opcional, con curl o Postman)

**macOS / Windows (Git Bash):**
```bash
# 1. Registro
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Luna","apellido":"Barreto","correo":"luna@email.com","password":"123456"}'

# 2. Login (te devuelve el token)
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"luna@email.com","password":"123456"}'

# 3. Catálogo (público)
curl http://localhost:8081/api/v1/productos
```

**Windows (PowerShell)** — nota: usa `curl.exe` y escapa las comillas del JSON con `\"`:
```powershell
# 1. Registro
curl.exe -X POST http://localhost:8081/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"nombre\":\"Luna\",\"apellido\":\"Barreto\",\"correo\":\"luna@email.com\",\"password\":\"123456\"}'

# 2. Login (te devuelve el token)
curl.exe -X POST http://localhost:8081/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"correo\":\"luna@email.com\",\"password\":\"123456\"}'

# 3. Catálogo (público)
curl.exe http://localhost:8081/api/v1/productos
```

> 💡 En PowerShell usar `curl` (sin `.exe`) **no funciona**: en ese shell `curl` es un
> alias de `Invoke-WebRequest`. Usa siempre `curl.exe`, o instala Git Bash.
> Otra opción es usar **Postman** (más cómodo y funciona igual en ambos sistemas).

Con el `token` del paso 2, úsalo en las rutas protegidas:

```
Authorization: Bearer <token>
```

**Nota sobre el rol ADMINISTRADOR:** los endpoints de crear/editar/borrar productos
requieren rol `ADMINISTRADOR`. No hay un endpoint público para crear admins; el equipo de
desarrollo lo habilita. Pide al líder un usuario administrador para probar el panel.

---

## 6. Resumen de la app

| Aspecto | Backend | Frontend |
|---|---|---|
| Tecnología | Spring Boot 3 + Java 17 | React 19 + TypeScript + Vite |
| Base de datos | PostgreSQL (Supabase, online) | — |
| Gestor de paquetes | Maven | pnpm |
| Archivo de configuración | `.env` (raíz del backend) | `.env` (raíz del frontend) |
| Puerto | 8081 | 8443 |
| URL de pruebas | `http://localhost:8081` | `http://localhost:8443` |

---

## 7. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `mvn: command not found` | Maven no está instalado | macOS: `brew install maven` · Windows: `winget install Apache.Maven` |
| `java` / `mvn` no reconocido tras instalarlo | `PATH` no actualizado | Cierra y abre una terminal nueva |
| `JAVA_HOME` no encontrado | JDK 17 no instalado o no apuntado | macOS: `brew install openjdk@17` y sigue sus instrucciones · Windows: reinstala el JDK y deja que el instalador configure `JAVA_HOME` |
| Backend no arranca / error de conexión a la BD | `.env` faltante, mal escrito o con credenciales viejas | Verifica el archivo `.env` en la raíz de `Evox-Backend` y los valores de Supabase |
| Backend arranca pero en otro puerto | Confusión con la doc vieja | El puerto real está en `application.properties` (`server.port=8081`); úsalo siempre |
| `SpringApplication ... APPLICATION FAILED TO START` | Faltan variables de entorno | No arranca sin el `.env` completo, no uses versión con valores vacíos |
| Frontend no carga datos | El backend no está corriendo o el `.env` apunta mal | Confirma `curl http://localhost:8081/api/v1/productos` (macOS/Git Bash) o `curl.exe ...` (PowerShell) y revisa `VITE_API_URL` |
| Puerto 8443 ocupado | Otra app usa ese puerto | Cierra el proceso o cambia `PORT` al levantar Vite: macOS/Git Bash: `PORT=8444 pnpm dev` · Windows PowerShell: `$env:PORT="8444"; pnpm dev` |
| Error de CORS en el navegador | Se llama a otra máquina/puerto no permitido | El backend permite `localhost` y `127.0.0.1` en cualquier puerto; usa esos hosts |
| `pnpm: command not found` | pnpm no instalado | `npm install -g pnpm@10.34.3` |
| Error raro con Vite / versión de Node | Node.js viejo | Actualiza a Node 22 |
| Login da 401 pero las credenciales parecen bien | Contraseña diferente / usuario no existe | Registra un usuario nuevo y prueba con él |
| `curl` no funciona en PowerShell | `curl` es un alias de `Invoke-WebRequest` | Usa `curl.exe` o instala Git Bash |

---

## 8. Checklist final del tester

- [ ] `java -version` → 17+
- [ ] `mvn -version` → 3.8+
- [ ] `node -v` → 20+
- [ ] `pnpm -v` → 9+
- [ ] Backend clonado y en rama `feature/migracion-supabase`
- [ ] Frontend clonado (carpeta `Diseño UI_UX` o `Diseno-UI-UX`)
- [ ] `.env` del backend creado con credenciales reales (lo dio el líder del equipo)
- [ ] `.env` del frontend con `VITE_API_URL=http://localhost:8081/api/v1`
- [ ] `mvn spring-boot:run` responde en `http://localhost:8081/api/v1/productos`
- [ ] `pnpm dev` sirve la app en `http://localhost:8443`
- [ ] Registro, login, catálogo, carrito y pedido funcionan en el frontend