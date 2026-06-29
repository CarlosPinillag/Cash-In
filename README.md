# Cash-In

Cash-In es una aplicación de gestión de finanzas personales construida con arquitectura de microservicios. Cada servicio de negocio tiene su propia base de datos MySQL, corre en un puerto independiente y se comunica con los demás a través de HTTP usando Spring WebClient. El descubrimiento de servicios se hace con **Eureka** y el acceso externo se centraliza en un **API Gateway**. La autenticación entre servicios se hace con **JWT**.

El proyecto está desarrollado en **Java 21 + Spring Boot** y gestionado con **Maven**. La configuración de cada servicio vive en archivos **YAML** (`application.yml` / `application-dev.yml`).

---

## Estructura general

```
Cash-In/
├── eureka-server         → puerto 8761   (Service Discovery)
├── api-gateway           → puerto 8090   (punto de entrada único)
├── UserService           → puerto 8080
├── auth-service          → puerto 8081
├── Expense_Service       → puerto 8082
├── Income_Service        → puerto 8083
├── Budget_Service        → puerto 8084
├── Category_Service      → puerto 8085
├── Analytics_Service     → puerto 8086
├── Alert_Service         → puerto 8087
├── Notification_Service  → puerto 8088
└── Promotion_Service     → puerto 8089
```

Cada microservicio de negocio sigue la misma estructura interna: `Controller`, `Service`, `Repository`, `Model`, `dto` (Request/Response), `Config`, `Exception` y `Client` (cuando consume a otro microservicio). También incluyen su propia clase de **tests unitarios** (`src/test/java/.../*ApplicationTests.java`) con JUnit 5 + Mockito.

---

## Requisitos

- Java 21+
- Maven 3.9+
- MySQL 8+
- Cada servicio de negocio necesita su propia base de datos creada antes de arrancar

---

## Bases de datos necesarias

```sql
CREATE DATABASE db_users;
CREATE DATABASE db_auth;
CREATE DATABASE db_expenses;
CREATE DATABASE db_incomes;
CREATE DATABASE db_budgets;
CREATE DATABASE db_categories;
CREATE DATABASE db_analytics;
CREATE DATABASE db_alerts;
CREATE DATABASE db_notifications;
CREATE DATABASE db_promotions;
```

Las credenciales por defecto en todos los `application-dev.yml` son `root` / `123456`. Cambiarlas antes de usar en cualquier entorno que no sea local.

---

## Configuración (YAML)

Cada microservicio tiene dos (o tres) archivos de configuración:

- `application.yml` — define el nombre de la app y el perfil activo (`spring.profiles.active: dev`).
- `application-dev.yml` — configuración real de entorno local: puerto, datasource, JPA, JWT, Eureka y logging.
- `application-docker.yml` *(solo `auth-service`, como ejemplo)* — misma configuración pero leyendo valores desde variables de entorno del contenedor (`${VARIABLE:valor-por-defecto}`), pensado para `docker-compose`.

`api-gateway` y `eureka-server` solo tienen un `application.yml`, ya que no manejan perfiles ni base de datos.

---

## Orden recomendado de arranque

El Gateway y los microservicios dependen de que **Eureka** esté disponible para registrarse y descubrirse entre sí. El orden correcto es:

1. **eureka-server** (`8761`) — esperar a que termine de levantar.
2. **Todos los microservicios de negocio** (`UserService`, `auth-service`, `Expense_Service`, etc.) — cada uno se registra en Eureka al iniciar.
3. Esperar ~10-15 segundos para que el registro/lease en Eureka se propague.
4. **api-gateway** (`8090`) — al iniciar, descarga el registro de Eureka y puede resolver `lb://NOMBRE-SERVICE` hacia cada microservicio.

Puedes confirmar que todo esté registrado entrando a `http://localhost:8761` en el navegador: ahí debes ver cada servicio listado como instancia `UP`.

```bash
cd <nombre-del-servicio>
mvn spring-boot:run
```

O ejecutar directamente el `.jar` ya compilado en la carpeta `target/` de cada servicio.

---

## API Gateway

Todo el tráfico externo (Postman, frontend, etc.) debería entrar por el Gateway en vez de pegarle directo a cada microservicio:

```
http://localhost:8090
```

El Gateway enruta por *path*, resolviendo el destino real vía Eureka (`lb://NOMBRE-SERVICE`):

| Ruta en el Gateway | Servicio destino |
|---|---|
| `/api/v1/users/**` | UserService (8080) |
| `/api/v1/auth/**` | auth-service (8081) |
| `/api/v1/expenses/**` | Expense_Service (8082) |
| `/api/v1/incomes/**` | Income_Service (8083) |
| `/api/v1/budgets/**` | Budget_Service (8084) |
| `/api/v1/categories/**` | Category_Service (8085) |
| `/api/v1/analytics/**` | Analytics_Service (8086) |
| `/api/v1/alerts/**` | Alert_Service (8087) |
| `/api/v1/notifications/**` | Notification_Service (8088) |
| `/api/v1/promotions/**` | Promotion_Service (8089) |

> El Gateway solo reenvía las rutas `/api/v1/**`. La documentación Swagger de cada servicio **no** pasa por el Gateway; hay que acceder a ella directo en el puerto de cada microservicio (ver siguiente sección).

---

## Documentación Swagger por servicio

Todos los microservicios de negocio incluyen `springdoc-openapi`. Se accede directo al puerto de cada uno (sin pasar por el Gateway):

| Servicio | Swagger UI |
|---|---|
| UserService | http://localhost:8080/swagger-ui/index.html |
| auth-service | http://localhost:8081/swagger-ui/index.html |
| Expense_Service | http://localhost:8082/swagger-ui/index.html |
| Income_Service | http://localhost:8083/swagger-ui/index.html |
| Budget_Service | http://localhost:8084/swagger-ui/index.html |
| Category_Service | http://localhost:8085/swagger-ui/index.html |
| Analytics_Service | http://localhost:8086/swagger-ui/index.html |
| Alert_Service | http://localhost:8087/swagger-ui/index.html |
| Notification_Service | http://localhost:8088/swagger-ui/index.html |
| Promotion_Service | http://localhost:8089/swagger-ui/index.html |

El JSON de cada definición OpenAPI está en `/v3/api-docs` del puerto correspondiente.

---

## Flujo de prueba end-to-end (Postman)

Usando el Gateway (`http://localhost:8090`) como base de todas las requests:

**1. Crear usuario** — `POST /api/v1/users`
```json
{
  "nombre": "Juan Perez",
  "email": "juan.perez@cashin.cl",
  "password": "clave123",
  "telefono": "987654321",
  "presupuestoMensual": 500000
}
```

**2. Login** — `POST /api/v1/auth/login`
```json
{
  "username": "juan.perez@cashin.cl",
  "password": "clave123"
}
```
Devuelve `{ "token": "...", "username": "...", "expiresAt": "..." }`.

**3. Obtener el mismo usuario** — `GET /api/v1/users/{idUser}` (o `GET /api/v1/users/email/juan.perez@cashin.cl`)

> `UserService` y `auth-service` no tienen filtro JWT propio, así que estos tres pasos funcionan sin enviar el header `Authorization`. Para los demás servicios (gastos, ingresos, presupuestos, alertas, etc.) sí hay que enviar `Authorization: Bearer {token}` obtenido en el paso 2.

---

## Tests unitarios

Cada microservicio de negocio (excepto `api-gateway` y `eureka-server`, que no tienen lógica propia que probar) tiene una clase de test en `src/test/java/.../*ApplicationTests.java` con:

- **JUnit 5** (`@Test`, `@DisplayName`, `@BeforeEach`)
- **Mockito** (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`) para simular `Repository` y los `Client` de otros servicios sin necesidad de levantar base de datos ni el resto del ecosistema.
- **AssertJ** (`assertThat`, `assertThatThrownBy`) para las aserciones.

Cubren la capa de `Service`: creación, validaciones de negocio (duplicados, estados, umbrales), búsquedas, actualizaciones y eliminación, incluyendo los casos de error (`ResourceNotFoundException`, `RuntimeException`).

### Cómo correrlos

```bash
cd <nombre-del-servicio>
mvn test
```

O desde el IDE: panel **Testing** → ícono ▶ sobre el servicio o sobre un test individual.

---

## Descripción de cada servicio

---

-- **eureka-server**

Puerto: `8761`

Servidor de registro y descubrimiento de servicios (Netflix Eureka). Todos los microservicios se registran aquí al arrancar; el Gateway lo consulta para resolver `lb://NOMBRE-SERVICE` a una instancia real. Expone un dashboard web en `http://localhost:8761` donde se puede ver qué servicios están `UP`.

---

-- **api-gateway**

Puerto: `8090`

Punto de entrada único del sistema. Enruta las peticiones externas hacia el microservicio correspondiente según el path (`/api/v1/<recurso>/**`), resolviendo el destino vía Eureka. Incluye un filtro de logging simple que imprime método y ruta de cada petición que pasa por el Gateway.

---

-- **UserService**

Puerto: `8080` | Base de datos: `db_users`

Es el servicio base del sistema. Gestiona el registro y la información de los usuarios. No tiene JWT filter propio porque los otros servicios lo consultan internamente para validar a quién pertenece un token.

Entidad principal: `User` — guarda nombre, email, password, teléfono, fecha de registro, estado activo y presupuesto mensual.

Endpoints principales (`/api/v1/users`):

- `POST /` — crear usuario
- `GET /{id}` — obtener por id
- `GET /email/{email}` — obtener por email
- `GET /` — listar todos
- `PUT /{id}` — actualizar
- `DELETE /{id}` — eliminar

También expone `/internal/email/{email}` para que otros servicios obtengan el modelo completo del usuario (uso interno entre microservicios, por ejemplo `auth-service`).

---

-- **auth-service**

Puerto: `8081` | Base de datos: `db_auth`

Maneja el login y la generación de tokens JWT. Cuando un usuario hace login, este servicio consulta al UserService para verificar el email y la contraseña, genera un JWT y lo persiste en la tabla `AuthToken`.

Entidad principal: `AuthToken` — guarda el username, el token generado, la fecha de emisión, la fecha de expiración y si está activo.

La expiración del token por defecto es de 1 hora (3600000 ms).

Endpoints (`/api/v1/auth`):

- `POST /login` — recibe email y password, devuelve el token
- `POST /logout` — invalida el token del header Authorization
- `GET /validate?token=` — valida si un token sigue siendo válido
- `GET /{id}` — obtener un token por id
- `DELETE /{id}` — eliminar un registro de token

---

-- **Expense_Service**

Puerto: `8082` | Base de datos: `db_expenses`

Gestiona los gastos de los usuarios. Cada gasto está asociado a un usuario y a una categoría. Al crear un gasto, este servicio consulta al UserService para verificar que el usuario existe y al CategoryService para validar la categoría.

Entidad principal: `Expense` — guarda userId, categoryId, nombre de categoría (denormalizado), monto, descripción, fecha y tipo.

Endpoints (`/api/v1/expenses`):

- `POST /` — registrar un gasto (requiere header Authorization)
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — listar gastos de un usuario
- `GET /user/{userId}/total` — suma total de gastos del usuario
- `PUT /{id}` — actualizar un gasto
- `DELETE /{id}` — eliminar

---

-- **Income_Service**

Puerto: `8083` | Base de datos: `db_incomes`

Gestiona los ingresos de los usuarios. Al registrar un ingreso, valida que el usuario exista llamando al UserService.

Entidad principal: `Income` — guarda userId, monto, descripción, categoría, fecha, si es recurrente y la frecuencia en caso de serlo.

Endpoints (`/api/v1/incomes`):

- `POST /` — registrar un ingreso (requiere header Authorization)
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — listar ingresos de un usuario
- `GET /user/{userId}/total` — suma total de ingresos del usuario
- `PUT /{id}` — actualizar
- `DELETE /{id}` — eliminar

---

-- **Budget_Service**

Puerto: `8084` | Base de datos: `db_budgets`

Gestiona los presupuestos por categoría que un usuario define para controlar sus gastos. Tiene una funcionalidad de seguimiento que consulta al ExpenseService para calcular cuánto se ha gastado en relación al límite del presupuesto. Cuando el porcentaje de uso supera cierto umbral, dispara una alerta al AlertService.

Entidad principal: `Budget` — guarda userId, categoryId, monto límite, periodo, estado activo, porcentaje de uso y fecha de inicio.

Endpoints (`/api/v1/budgets`):

- `POST /` — crear presupuesto
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — listar presupuestos de un usuario
- `GET /{id}/seguimiento` — calcula el porcentaje de uso en tiempo real (requiere Authorization)
- `PUT /{id}` — actualizar
- `DELETE /{id}` — eliminar

---

-- **Category_Service**

Puerto: `8085` | Base de datos: `db_categories`

Gestiona las categorías que se usan para clasificar gastos y promociones. Es uno de los servicios más simples. No depende de ningún otro servicio.

Entidad principal: `Category` — guarda nombre (único), descripción, tipo y estado activo.

Endpoints (`/api/v1/categories`):

- `POST /` — crear categoría
- `GET /{id}` — obtener por id
- `GET /` — listar todas (admite filtro `?activo=true/false`)
- `GET /tipo/{tipo}` — filtrar por tipo
- `PUT /{id}` — actualizar
- `PATCH /{id}/desactivar` — desactivar sin eliminar
- `DELETE /{id}` — eliminar

---

-- **Analytics_Service**

Puerto: `8086` | Base de datos: `db_analytics`

Genera análisis financieros para un usuario. Al generarlo, consulta al IncomeService y al ExpenseService para calcular el total de ingresos, total de gastos, balance y tasa de ahorro. También puede mostrar un resumen financiero consolidado.

Entidad principal: `Analytics` — guarda userId, totalIngresos, totalGastos, balance, tasaAhorro, estadoBalance y fechaGeneracion.

Endpoints (`/api/v1/analytics`):

- `POST /` — generar un nuevo análisis (requiere Authorization)
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — historial de análisis del usuario
- `GET /user/{userId}/resumen` — resumen financiero en tiempo real (requiere Authorization)
- `DELETE /{id}` — eliminar

---

-- **Alert_Service**

Puerto: `8087` | Base de datos: `db_alerts`

Gestiona alertas financieras asociadas a presupuestos. Una alerta se crea cuando un presupuesto supera su límite o está próximo a hacerlo. Cada alerta tiene un estado de leída/no leída.

Entidad principal: `Alert` — guarda userId, budgetId, tipo, mensaje, estado leída y fechaCreacion.

Endpoints (`/api/v1/alerts`):

- `POST /` — crear alerta (requiere Authorization)
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — listar alertas de un usuario
- `GET /user/{userId}/no-leidas` — listar alertas no leídas
- `GET /user/{userId}/contador` — contar alertas no leídas
- `PUT /{id}/leer` — marcar como leída
- `DELETE /{id}` — eliminar

---

-- **Notification_Service**

Puerto: `8088` | Base de datos: `db_notifications`

Gestiona notificaciones que se envían a los usuarios. A diferencia del AlertService (que está más orientado a eventos de presupuesto), este servicio maneja comunicaciones más generales con canal de envío, tipo, estado del envío y fecha real de envío.

Entidad principal: `Notification` — guarda userId, canal, tipo, título, mensaje, estado, leída, fechaCreacion y fechaEnvio.

Endpoints (`/api/v1/notifications`):

- `POST /` — crear notificación (requiere Authorization)
- `GET /{id}` — obtener por id
- `GET /user/{userId}` — listar notificaciones (admite filtro `?tipo=`)
- `GET /user/{userId}/no-leidas` — listar no leídas
- `GET /user/{userId}/contador` — contar no leídas
- `GET /estado/{estado}` — filtrar por estado
- `PUT /{id}` — actualizar
- `PUT /{id}/leer` — marcar como leída
- `PUT /{id}/enviar` — marcar como enviada
- `PUT /{id}/fallar` — marcar como fallida
- `DELETE /{id}` — eliminar

---

-- **Promotion_Service**

Puerto: `8089` | Base de datos: `db_promotions`

Gestiona promociones con códigos de descuento. Cada promoción está asociada a una categoría y tiene un tipo de descuento (porcentaje o monto fijo), fecha de inicio, fecha de fin y un máximo de usos. Al aplicar una promoción, valida que esté activa, que no haya expirado y que no haya alcanzado su límite de usos.

Entidad principal: `Promotion` — guarda categoryId, nombre de categoría, código único, descripción, tipoDescuento, valorDescuento, fechaInicio, fechaFin, usoMaximo, usosActuales y activo.

Endpoints (`/api/v1/promotions`):

- `POST /` — crear promoción (requiere Authorization)
- `GET /{id}` — obtener por id
- `GET /codigo/{codigo}` — buscar por código
- `GET /` — listar todas
- `GET /activas` — listar solo las activas y vigentes
- `GET /activas/categoria?categoryId=` — listar activas por categoría
- `POST /aplicar` — aplicar una promoción a un monto (requiere Authorization)
- `PUT /{id}` — actualizar
- `PUT /{id}/desactivar` — desactivar
- `DELETE /{id}` — eliminar

---

## Notas

- El JWT secret está hardcodeado en los `application-dev.yml` como `clave-super-secreta-para-clase-123456`. Esto es solo para el entorno de desarrollo y no debería usarse en producción.
- Todos los servicios usan `spring.jpa.hibernate.ddl-auto: update` en perfil `dev`, lo que significa que Hibernate crea o actualiza las tablas automáticamente al arrancar. En el perfil `docker` de `auth-service` se usa `validate` porque las tablas las crea Flyway.
- Flyway está desactivado en perfil `dev` en todos los servicios; en `auth-service` perfil `docker` está activo y corre las migraciones de `classpath:db/migration`.
- La comunicación entre servicios usa Spring WebClient de forma reactiva.
- El JWT solo protege las rutas `/api/v1/**` de cada microservicio que tiene `JwtFilter` configurado; los endpoints de Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) quedan siempre abiertos.
- `UserService` y `auth-service` no validan JWT en sus propios endpoints (son el punto de entrada de autenticación), por eso se puede crear un usuario y loguearse sin necesidad de un token previo.
