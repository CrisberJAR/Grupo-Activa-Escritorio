package com.crediactiva.dao;

import com.crediactiva.db.ConnectionFactory;
import com.crediactiva.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class UserDao {

    private static final String SQL =
            "SELECT u.id, u.username, u.password_hash, r.nombre AS rol, u.estado " +
            "FROM usuario u JOIN roles r ON u.rol_id = r.id " +
            "WHERE u.username = ? LIMIT 1";

    public Optional<User> authenticate(String username, String plainPassword) {
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int id = rs.getInt("id");
                String user = rs.getString("username");
                String hash = rs.getString("password_hash");
                String rol = rs.getString("rol");
                String estado = rs.getString("estado");

                if (!"ACTIVO".equalsIgnoreCase(estado)) {
                    return Optional.empty();
                }

                boolean ok = (hash != null && BCrypt.checkpw(plainPassword, hash));
                if (!ok) return Optional.empty();

                return Optional.of(new User(id, user, rol, estado));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
