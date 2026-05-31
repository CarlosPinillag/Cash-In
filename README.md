# Cash-In

Cash-In es una aplicación de gestión de finanzas personales construida con arquitectura de microservicios. Cada servicio tiene su propia base de datos MySQL, corre en un puerto independiente y se comunica con los demás a través de HTTP usando WebClient. La autenticación entre servicios se hace con JWT.

El proyecto está desarrollado en Java con Spring Boot y gestionado con Maven.

---

## Estructura general

```
Cash-In/
├── UserService          → puerto 8080
├── auth-service         → puerto 8081
├── Expense_Service      → puerto 8082
├── Income_Service       → puerto 8083
├── Budget_Service       → puerto 8084
├── Category_Service     → puerto 8085
├── Analytics_Service    → puerto 8086
├── Alert_Service        → puerto 8087
├── Notification_Service → puerto 8088
└── Promotion_Service    → puerto 8089
```

Cada servicio sigue la misma estructura interna: Controller, Service, Repository, Model, dto (Request/Response), Config y Exception.

---

## Requisitos

- Java 17+
- Maven 3.8+
- MySQL 8+
- Cada servicio necesita su propia base de datos creada antes de arrancar

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

Las credenciales por defecto en todos los `application.properties` son `root` / `123456`. Cambiarlo antes de usar en cualquier entorno que no sea local.

---

## Levantar un servicio

```bash
cd <nombre-del-servicio>
mvn spring-boot:run
```

O ejecutar directamente el `.jar` que ya viene compilado en cada carpeta `target/`.

---

## Descripción de cada servicio

---

-- UserService

Puerto: `8080` | Base de datos: `db_users`

Es el servicio base del sistema. Gestiona el registro y la información de los usuarios. No tiene JWT filter propio porque los otros servicios lo consultan internamente para validar a quién pertenece un token.

Entidad principal: `User` — guarda nombre, email, passwordHash, teléfono, fecha de registro, estado activo y presupuesto mensual.

Endpoints principales (`/api/v1/users`):

- `POST /` — crear usuario
- `GET /{id}` — obtener por id
- `GET /email/{email}` — obtener por email
- `GET /` — listar todos
- `PUT /{id}` — actualizar
- `DELETE /{id}` — eliminar

También expone `/internal/email/{email}` para que otros servicios obtengan el modelo completo del usuario (incluyendo el hash de contraseña).

---

-- auth-service

Puerto: `8081` | Base de datos: `db_auth`

Maneja el login y la generación de tokens JWT. Cuando un usuario hace login, este servicio consulta al UserService para verificar el email, compara el hash de la contraseña, genera un JWT y lo persiste en la tabla `AuthToken`.

Entidad principal: `AuthToken` — guarda el username, el token generado, la fecha de emisión, la fecha de expiración y si está activo.

La expiración del token por defecto es de 1 hora (3600000 ms).

Endpoints (`/api/v1/auth`):

- `POST /login` — recibe email y password, devuelve el token
- `POST /logout` — invalida el token del header Authorization
- `GET /validate?token=` — valida si un token sigue siendo válido
- `GET /{id}` — obtener un token por id
- `DELETE /{id}` — eliminar un registro de token

---

-- Expense_Service

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

-- Income_Service

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

-- Budget_Service

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

-- Category_Service

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

-- Analytics_Service

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

-- Alert_Service

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

-- Notification_Service

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

-- Promotion_Service

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

- El JWT secret está hardcodeado en los `application.properties` como `clave-super-secreta-para-clase-123456`. Esto es solo para el entorno de desarrollo y no debería usarse en producción.
- Todos los servicios usan `spring.jpa.hibernate.ddl-auto=update`, lo que significa que Hibernate crea o actualiza las tablas automáticamente al arrancar.
- Flyway está desactivado en todos los servicios.
- La comunicación entre servicios usa Spring WebClient de forma reactiva.
