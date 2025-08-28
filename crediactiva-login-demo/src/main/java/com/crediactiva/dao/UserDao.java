package com.crediactiva.dao;

import com.crediactiva.db.ConnectionFactory;
import com.crediactiva.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    // ====== SQLs ======
    private static final String SQL_AUTH =
            "SELECT u.id, u.username, u.password_hash, r.nombre AS rol, u.estado " +
                    "FROM usuario u JOIN roles r ON u.rol_id = r.id " +
                    "WHERE u.username = ? LIMIT 1";

    // ====== LOGIN ======
    public Optional<User> authenticate(String username, String plainPassword) {
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_AUTH)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int id = rs.getInt("id");
                String user = rs.getString("username");
                String hash = rs.getString("password_hash");
                String rol = rs.getString("rol");
                String estado = rs.getString("estado");

                if (!"ACTIVO".equalsIgnoreCase(estado)) return Optional.empty();

                boolean ok = (hash != null && BCrypt.checkpw(plainPassword, hash));
                if (!ok) return Optional.empty();

                return Optional.of(new User(id, user, rol, estado));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // ====== UTILIDADES BÁSICAS ======

    /** Verifica si el username ya existe (fuera de transacción). */
    public boolean usernameExists(String username) {
        final String sql = "SELECT 1 FROM usuario WHERE username = ? LIMIT 1";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // por seguridad, asumimos true si hay error para evitar crear duplicados
            return true;
        }
    }

    // ====== CREACIÓN DE USUARIOS (ADMIN/ASESOR/CLIENTE) ======

    /**
     * Crea un usuario en tabla 'usuario' con estado ACTIVO.
     * @param username username único
     * @param rawPassword contraseña en texto plano (se hashea con BCrypt)
     * @param roleNameUpper "ADMIN" | "ASESOR" | "CLIENTE"
     * @return id generado del nuevo usuario
     * @throws SQLException si falla o el rol no existe / username duplicado
     */
    public int createUser(String username, String rawPassword, String roleNameUpper) throws SQLException {
        try (Connection cn = ConnectionFactory.getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (usernameExistsTx(cn, username)) {
                    throw new SQLException("El usuario ya existe: " + username);
                }

                int rolId = getRoleIdByNameTx(cn, roleNameUpper);
                if (rolId <= 0) throw new SQLException("Rol no encontrado: " + roleNameUpper);

                String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

                final String sql = "INSERT INTO usuario (username, password_hash, rol_id, estado) " +
                        "VALUES (?,?,?, 'ACTIVO')";
                try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, hash);
                    ps.setInt(3, rolId);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            int newId = keys.getInt(1);
                            cn.commit();
                            return newId;
                        }
                    }
                    throw new SQLException("No se obtuvo ID generado");
                }
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    // ====== LISTAR USUARIOS ======

    /** Devuelve todos los usuarios con su rol y estado. */
    public List<User> findAll() throws SQLException {
        String sql = "SELECT u.id, u.username, r.nombre AS rol, u.estado " +
                "FROM usuario u JOIN roles r ON u.rol_id = r.id " +
                "ORDER BY u.id ASC";
        List<User> lista = new ArrayList<>();
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("rol"),
                        rs.getString("estado")
                ));
            }
        }
        return lista;
    }

    // ====== HELPERS TRANSACCIONALES ======

    private boolean usernameExistsTx(Connection cn, String username) throws SQLException {
        final String sql = "SELECT 1 FROM usuario WHERE username = ? LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int getRoleIdByNameTx(Connection cn, String roleUpper) throws SQLException {
        final String sql = "SELECT id FROM roles WHERE UPPER(nombre)=? LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, roleUpper);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}
