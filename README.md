# Sistema de Reservas de Restaurante

Este proyecto es un sistema de gestión de reservas para un restaurante, desarrollado como proyecto universitario.

## Tecnologías Utilizadas

* Java 21
* Spring Boot 3.3.0
* Spring Security
* Spring Data JPA
* Thymeleaf
* PostgreSQL
* Bootstrap 5
* Maven
* JasperReports Studio

## Funcionalidades

### Roles

* **ADMIN**: Gestionar las mesas del restaurante junto a las reservas y a los usuarios, además de visualizar el historial de cambios en las reservas y generar reportes de usuarios.
* **STAFF**: Gestionar las reservas y mesas del día.
* **USER**: Ver sus propias reservas, ver mesas disponibles, crear/modificar/cancelar sus reservas.

### Características

* Autenticación y autorización basada en roles
* Gestión de usuarios
* Gestión de mesas de restaurante
* Sistema de reservas con validaciones
* Historial de cambios en reservas (auditoría)
* Internacionalización (Español, Inglés, Italiano)
* Interfaz responsive
* Generación de reportes de usuarios

## Requisitos Previos

* Java 21
* PostgreSQL 
* Maven 3.8 o superior

## Instalación

1. Clona el repositorio:
   ```
   git clone https://github.com/AVargass19/restaurant.git
   ```

2. Configura la base de datos PostgreSQL:
    * Crea una base de datos llamada `restaurant_db`
    * Actualiza los datos de conexión en `application.properties` si es necesario

3. Compila y ejecuta con Maven:
   ```
   cd restaurant-reservation-system
   mvn spring-boot:run
   ```

4. Accede a la aplicación en tu navegador:
   ```
   http://localhost:8080
   ```

## Usuarios Predeterminados

* **Admin**: usuario: `anav`, contraseña: `ana123`
* **Staff**: usuario: `bero`, contraseña: `bero123`
* **Usuario**: usuario: `david`, contraseña: `david123`

## Estructura del Proyecto

```
proyecto-reservas-restaurante/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── restaurant/
│   │   │           ├── config/        # Configuraciones de Spring
│   │   │           ├── controller/    # Controladores MVC
│   │   │           ├── dto/           # DTOs
│   │   │           ├── model/         # Entidades
│   │   │           ├── repository/    # Repositorios JPA
│   │   │           ├── security/      # Configuración de seguridad
│   │   │           ├── service/       # Servicios de negocio
│   │   │           └── RestaurantReservationSystemApplication.java
│   │   └── resources/
│   │       ├── reports/               # Reporte de usuarios del sistema
│   │       ├── static/                # Recursos estáticos (CSS, JS)
│   │       ├── templates/             # Plantillas Thymeleaf
│   │       ├── messages*.properties   # Archivos de internacionalización
│   │       ├── schema.sql             # Esquema de base de datos
│   │       ├── data.sql               # Datos iniciales
│   │       └── application.properties # Configuración
│   └── test/                          # Pruebas unitarias
├── pom.xml                            # Configuración de Maven
└── README.md                          # Documentación
```

## Configuración de Lombok en IntelliJ IDEA

1. Instala el plugin Lombok desde Marketplace:
    * File > Settings > Plugins > Marketplace
    * Busca "Lombok" e instálalo
    * Reinicia IntelliJ

2. Habilita el procesamiento de anotaciones:
    * File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors
    * Marca "Enable annotation processing"

## Endpoints API

La documentación de la API está disponible en:
```
http://localhost:8080/swagger-ui.html
```

## Trigger de Auditoría

El sistema incluye un trigger en PostgreSQL que registra automáticamente los cambios en las reservas (creación, modificación, eliminación) en la tabla `historial_res`. Esta funcionalidad permite una auditoría completa de todas las operaciones realizadas sobre las reservas.

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.