package com.crediactiva.service;

import com.crediactiva.db.ConnectionFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * Aprueba/Rechaza solicitudes con reglas:
 * - APROBAR: crear usuario CLIENTE si no tiene, username = "CLI" + cliente_id (mayúscula),
 *            password = DNI (hash BCrypt), set cliente.usuario_id, luego set solicitud.estado = APROBADA.
 * - RECHAZAR: set estado = RECHAZADA (trigger ya marca purge_at/fecha_rechazo).
 */
public class SolicitudService {

    /** Cambia estado a RECHAZADA. */
    public void rechazar(int solicitudId) throws SQLException {
        final String sql = "UPDATE solicitud SET estado='RECHAZADA' WHERE id=? AND estado='PENDIENTE'";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, solicitudId);
            ps.executeUpdate();
        }
    }

    /** Aprueba y crea usuario CLIENTE si no existe; todo en una transacción. */
    public void aprobar(int solicitudId) throws SQLException {
        try (Connection cn = ConnectionFactory.getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                // 1) Obtener cliente_id y dni
                int clienteId = -1;
                String dni = null;
                String q1 = "SELECT c.id AS cliente_id, c.dni, c.usuario_id " +
                        "FROM solicitud s JOIN cliente c ON c.id = s.cliente_id " +
                        "WHERE s.id = ? FOR UPDATE";
                try (PreparedStatement ps = cn.prepareStatement(q1)) {
                    ps.setInt(1, solicitudId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            clienteId = rs.getInt("cliente_id");
                            dni = rs.getString("dni");
                        } else {
                            throw new SQLException("Solicitud no encontrada: " + solicitudId);
                        }
                    }
                }

                // 2) Crear usuario CLIENTE si no tiene
                Integer userId = null;
                String checkUser = "SELECT usuario_id FROM cliente WHERE id=? FOR UPDATE";
                try (PreparedStatement ps = cn.prepareStatement(checkUser)) {
                    ps.setInt(1, clienteId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int uid = rs.getInt(1);
                            if (!rs.wasNull()) userId = uid;
                        }
                    }
                }
                if (userId == null) {
                    // rol CLIENTE id
                    int rolId = -1;
                    try (PreparedStatement ps = cn.prepareStatement("SELECT id FROM roles WHERE nombre='CLIENTE' LIMIT 1");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) rolId = rs.getInt(1);
                    }
                    if (rolId <= 0) throw new SQLException("Rol CLIENTE no encontrado");

                    String username = ("CLI" + clienteId).toUpperCase();
                    String hash = BCrypt.hashpw(dni, BCrypt.gensalt(10));

                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO usuario (username, password_hash, rol_id, estado) VALUES (?,?,?, 'ACTIVO')",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, username);
                        ps.setString(2, hash);
                        ps.setInt(3, rolId);
                        ps.executeUpdate();
                        try (ResultSet k = ps.getGeneratedKeys()) {
                            if (k.next()) userId = k.getInt(1);
                        }
                    }
                    if (userId == null) throw new SQLException("No se pudo crear usuario CLIENTE");

                    try (PreparedStatement ps = cn.prepareStatement("UPDATE cliente SET usuario_id=? WHERE id=?")) {
                        ps.setInt(1, userId);
                        ps.setInt(2, clienteId);
                        ps.executeUpdate();
                    }
                }

                // 3) Marcar solicitud como APROBADA
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE solicitud SET estado='APROBADA' WHERE id=?")) {
                    ps.setInt(1, solicitudId);
                    ps.executeUpdate();
                }

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}
