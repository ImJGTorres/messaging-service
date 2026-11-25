# messaging-service

Microservicio de **mensajería** de la plazoleta de comidas. Recibe de `food-court-service` la orden de enviar un SMS (por ejemplo, el PIN de seguridad cuando un pedido queda listo) y lo entrega con **AWS SNS**. No tiene base de datos.

```
food-court-service  --(HTTP + JWT)-->  messaging-service  -->  AWS SNS  -->  celular del cliente
```

Arquitectura hexagonal (`domain` / `application` / `infrastructure`), Spring Boot 3.5, Java 17, Gradle, Spring Security (JWT), Swagger.

## Endpoint

| Método | Ruta | Quién | Respuesta |
|---|---|---|---|
| `POST` | `/api/v1/sms` | Solo token con rol `EMPLOYEE` | `202` sin cuerpo |

Cuerpo: `{"phone": "+573005698325", "message": "texto de hasta 160 caracteres"}`. El celular debe venir en formato **E.164** (con `+` y código de país). Errores: `400` datos inválidos, `401` sin token, `403` otro rol, `503` el proveedor de SMS no está disponible.

Swagger: `http://localhost:8083/swagger-ui/index.html`

## Variables de entorno

| Variable | Obligatoria | Para qué |
|---|---|---|
| `JWT_SECRET` | Sí | Mismo secreto (Base64) que usan `users-service` y `food-court-service` para firmar y validar el token. |
| `SMS_PROVIDER` | No (por defecto `log`) | `log`: solo escribe el SMS en la consola, **sin costo**. `sns`: envía de verdad con AWS SNS. |
| `AWS_REGION` | No (por defecto `us-east-1`) | Región de SNS. Solo se usa con `SMS_PROVIDER=sns`. |
| `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` | Solo con `sns` | Credenciales de AWS (cadena de credenciales por defecto del SDK). **Nunca** se guardan en el repositorio. |

## Cómo correrlo

```
.\gradlew bootRun
```

Sin configurar nada más que `JWT_SECRET`, el servicio arranca con el proveedor `log`: cada SMS aparece en la consola con el celular enmascarado y no se gasta dinero.

## Probar con AWS SNS de verdad (con cuidado)

Las cuentas de AWS nuevas están en **sandbox** y tienen un presupuesto limitado:

1. Solo se envía SMS a números **verificados** en la consola de AWS (SNS → Text messaging (SMS) → Sandbox destination phone numbers).
2. Usa un usuario IAM con permiso únicamente de `sns:Publish` y define en la consola un límite de gasto mensual pequeño para SMS.
3. Arranca con `SMS_PROVIDER=sns` y las variables de AWS puestas **en tu terminal o en IntelliJ**, no en archivos del proyecto.
4. Haz **pocos** envíos (2 o 3): el mensaje está limitado a 160 caracteres ASCII para que cueste un solo segmento.

## Pruebas

```
.\gradlew clean test jacocoTestReport
```

El informe de cobertura queda en `build/reports/jacoco/test/html/index.html`.
