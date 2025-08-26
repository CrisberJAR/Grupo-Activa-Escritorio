-- Script opcional para crear usuario admin 'CRISBER' (reemplaza <TU_HASH_BCRYPT>)
USE crediactiva;

INSERT INTO usuario (username, password_hash, rol_id, estado)
VALUES ('CRISBER', 'Ander_123', 1, 'ACTIVO');

-- Si deseas asociar al nuevo admin a tabla 'asesor' (no siempre necesario para admin), puedes crear su fila:
-- INSERT INTO asesor (usuario_id, nombres, apellidos, telefono)
-- SELECT id, 'Crisber', 'Admin', '999999999' FROM usuario WHERE username='CRISBER';
