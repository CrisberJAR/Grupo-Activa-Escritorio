# CrediActiva - Login Demo (Maven, Java 21, Swing)

Proyecto mínimo para:
- Conectar a MySQL (`crediactiva`)
- Probar login por **consola** y con una **UI Swing** sencilla
- Incluir el **script del Día 7** en `/scripts`

## Requisitos
- Java 21 (JDK 21)
- Maven 3.9+
- MySQL 8.x
- IntelliJ IDEA (recomendado) o NetBeans

## 1) Crear la base de datos
1. Abre MySQL Workbench.
2. Ejecuta el script `scripts/Scrip_dia7.sql` (activa el *Event Scheduler* si lo necesitas).
3. El script crea el schema `crediactiva`, tablas, vistas, trigger y evento.
4. Inserta los `roles` y un usuario base con **hash pendiente**.

> En el script verás: `'$2y$10$colocaAQUIunHashBCryptValidoxxxxxxxxxxxxxxx'`. Debes reemplazarlo por un hash **BCrypt** real.

### ¿Cómo genero el hash BCrypt?
Tienes dos opciones:
- **Opción A (desde este proyecto)**: Ejecuta
  ```bash
  mvn -q -Dexec.mainClass=com.crediactiva.util.PasswordUtil exec:java
  ```
  Escribe la contraseña (por ejemplo `Ander_123`) y copia el **Hash BCrypt** que aparece.
- **Opción B (con argumentos)**:
  ```bash
  mvn -q -Dexec.mainClass=com.crediactiva.util.PasswordUtil -Dexec.args="Ander_123" exec:java
  ```

Con el hash copiado, actualiza en MySQL:
```sql
UPDATE usuario SET password_hash = '<TU_HASH_BCRYPT>' WHERE username = 'admin';
```

> Si quieres crear tu usuario admin `CRISBER` con contraseña `Ander_123`, puedes ejecutar luego:
```sql
INSERT INTO usuario (username, password_hash, rol_id, estado)
VALUES ('CRISBER', '<TU_HASH_BCRYPT>', 1, 'ACTIVO');
```
(y si vas a usar asesor/cliente, crea sus filas respectivas en `asesor` / `cliente` según corresponda).

## 2) Configurar conexión
Edita `src/main/resources/db.properties` con tus credenciales reales:
```
db.url=jdbc:mysql://127.0.0.1:3306/crediactiva?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true
db.user=root
db.password=tu_contraseña
db.pool.size=5
```

> Si tu servidor MySQL está en otra PC de la red, reemplaza `127.0.0.1` por la IP del servidor.

## 3) Compilar y ejecutar

### Por consola (login en terminal)
```bash
mvn -q clean package
mvn -q -Dexec.mainClass=com.crediactiva.Main exec:java
```
- Ingresa `Usuario` y `Contraseña` que existan en la tabla `usuario` (estado `ACTIVO`).

### UI Swing (ventana de login)
```bash
mvn -q -Dexec.mainClass=com.crediactiva.Main -Dexec.args="swing" exec:java
```
- Aparecerá una ventana simple que valida usuario/contraseña contra MySQL.

### Fat JAR ejecutable (opcional)
```bash
mvn -q clean package
java -jar target/crediactiva-login-demo-1.0.0-SNAPSHOT-shaded.jar
# Para abrir Swing:
java -jar target/crediactiva-login-demo-1.0.0-SNAPSHOT-shaded.jar swing
```

## 4) ¿Qué hace cada módulo?
- `com.crediactiva.db.ConnectionFactory`: Crea un pool de conexiones HikariCP leyendo `db.properties`.
- `com.crediactiva.dao.UserDao`: Consulta a MySQL y valida la contraseña con **BCrypt**.
- `com.crediactiva.model.User`: POJO de usuario autenticado.
- `com.crediactiva.ui.LoginFrame`: Ventana Swing con usuario/contraseña.
- `com.crediactiva.Main`: Punto de entrada (consola por defecto, o `swing` como argumento).
- `com.crediactiva.util.PasswordUtil`: Utilidad para generar hashes **BCrypt**.

## 5) Errores comunes
- **`Communications link failure`**: Verifica IP/puerto, firewall, y que MySQL esté levantado.
- **`Public Key Retrieval is not allowed`**: Por eso usamos `allowPublicKeyRetrieval=true`.
- **Zona horaria**: Usamos `serverTimezone=America/Lima` para evitar warnings.
- **`Usuario inactivo`**: En `usuario.estado` debe ser `ACTIVO`.

---

¡Listo! Con esto ya puedes:
1) Crear la base de datos con el script del Día 7.
2) Generar un hash BCrypt y asignarlo a tu usuario.
3) Probar el login por consola y por UI Swing.
