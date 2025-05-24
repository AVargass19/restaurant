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
* SSL/TLS (HTTPS)

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
* Comunicación segura mediante HTTPS con certificado SSL

## Requisitos Previos

* Java 21
* PostgreSQL
* Maven 3.8 o superior
* KeyStore Explorer (opcional, para gestión de certificados SSL)

## Instalación

1. Clona el repositorio:
   ```
   git clone https://github.com/AVargass19/restaurant.git
   ```

2. Configura la base de datos PostgreSQL:
   * Crea una base de datos llamada `restaurant_db`
   * Actualiza los datos de conexión en `application.properties` si es necesario

3. Configuración SSL (Requerida):

   El proyecto incluye un certificado SSL auto-firmado (`certificado.p12`) para desarrollo local. Si necesitas regenerarlo:

   Opción A: Usando KeyStore Explorer (Recomendado)
   1. Abre KeyStore Explorer
   2. File → New → Selecciona PKCS #12
   3. Clic derecho → Generate Key Pair
   4. Configura:
      - Algorithm: RSA
      - Key Size: 2048 bits
      - Common Name (CN): `localhost`
      - Validity: 365 días
   5. Establece alias: `AnaVargas`
   6. Contraseña: `Vargas.2006`
   7. Guarda como `certificado.p12` en `src/main/resources/`

   Opción B: Usando keytool (Terminal)
    ```bash
    keytool -genkeypair -alias AnaVargas -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/certificado.p12 -validity 365 -storepass Vargas.2006 -keypass Vargas.2006 -dname "CN=localhost"
    ```

4. Compila y ejecuta con Maven:
   ```
   cd restaurant-reservation-system
   mvn spring-boot:run
   ```
5. Accede a la aplicación mediante HTTPS:
   ```
   https://localhost:8443
   ```

   Nota importante: El navegador mostrará una advertencia de seguridad debido a que es un certificado auto-firmado. Haz clic en "Avanzado" y "Continuar" para acceder de forma segura.

## Usuarios Predeterminados

* Admin: usuario: `anav`, contraseña: `ana123`
* Staff: usuario: `bero`, contraseña: `bero123`
* Usuario: usuario: `david`, contraseña: `david123`

## Configuración SSL

La aplicación está configurada para usar HTTPS exclusivamente en el puerto `8443`. La configuración SSL incluye:

* Puerto HTTPS: 8443
* Protocolo: TLS 1.2+
* Tipo de KeyStore: PKCS12
* Certificado: Auto-firmado para desarrollo local
* Validez: 365 días

### Configuración en application.properties

```properties
# Configuración de servidor con SSL
server.port=8443
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificado.p12
server.ssl.key-store-password=Vargas.2006
server.ssl.key-alias=AnaVargas
```

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
│   │       ├── certificado.p12        # Certificado SSL
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
https://localhost:8443/swagger-ui/index.html#/
```

## Solución de Problemas SSL

### Advertencia de Certificado en el Navegador
Es normal ver una advertencia de seguridad. Para continuar:
1. Haz clic en "Avanzado" o "Advanced"
2. Selecciona "Continuar a localhost (no seguro)" o "Proceed to localhost (unsafe)"

### Error de Conexión
Si experimentas problemas de conexión:
1. Verifica que el archivo `certificado.p12` esté en `src/main/resources/`
2. Confirma que la contraseña y alias coincidan con la configuración
3. Asegúrate de usar `https://` en lugar de `http://`

### Regenerar Certificado
Si el certificado expira o presenta problemas, elimina `certificado.p12` y sigue los pasos de configuración SSL nuevamente.

## Trigger de Auditoría

El sistema incluye un trigger en PostgreSQL que registra automáticamente los cambios en las reservas (creación, modificación, eliminación) en la tabla `historial_res`. Esta funcionalidad permite una auditoría completa de todas las operaciones realizadas sobre las reservas.

## Seguridad

El proyecto implementa múltiples capas de seguridad:
* Autenticación y autorización basada en roles con Spring Security
* Comunicación cifrada mediante HTTPS/TLS
* Protección CSRF habilitada
* Validación de datos en formularios
* Auditoría de cambios en reservas

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.