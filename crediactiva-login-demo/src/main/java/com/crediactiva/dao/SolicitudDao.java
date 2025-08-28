package com.crediactiva.dao;

import com.crediactiva.db.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDao {

    /** Inserta solicitud en estado PENDIENTE. */
    public int createSolicitud(int clienteId, int asesorId, Date fecha, double monto,
                               String motivo, Double ingreso) throws SQLException {
        final String sql = "INSERT INTO solicitud (cliente_id, asesor_id, fecha, monto, motivo, ingreso, estado) " +
                "VALUES (?,?,?,?,?,?, 'PENDIENTE')";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, clienteId);
            ps.setInt(2, asesorId);
            ps.setDate(3, fecha);
            ps.setDouble(4, monto);
            ps.setString(5, motivo);
            if (ingreso == null) ps.setNull(6, Types.DECIMAL); else ps.setDouble(6, ingreso);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
                throw new SQLException("No se obtuvo ID de solicitud");
            }
        }
    }

    /** Lista TODAS las solicitudes (para Admin) con ordenamiento simple. */
    public List<Object[]> listarSolicitudes(String orderBy) throws SQLException {
        // orderBy seguro (whitelist)
        String order = switch (orderBy == null ? "" : orderBy) {
            case "fecha" -> "s.fecha DESC";
            case "asesor" -> "a.apellidos, a.nombres";
            case "estado" -> "s.estado";
            default -> "s.id DESC";
        };
        String sql = "SELECT s.id, s.fecha, s.monto, s.estado, " +
                "c.id AS cliente_id, c.dni, c.nombres AS cnom, c.apellidos AS cape, " +
                "a.id AS asesor_id, a.nombres AS anom, a.apellidos AS aape " +
                "FROM solicitud s " +
                "JOIN cliente c ON c.id = s.cliente_id " +
                "JOIN asesor a  ON a.id = s.asesor_id " +
                "ORDER BY " + order;

        List<Object[]> data = new ArrayList<>();
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.add(new Object[]{
                        rs.getInt("id"),
                        rs.getDate("fecha"),
                        rs.getBigDecimal("monto"),
                        rs.getString("estado"),
                        rs.getInt("cliente_id"),
                        rs.getString("dni"),
                        rs.getString("cnom") + " " + rs.getString("cape"),
                        rs.getInt("asesor_id"),
                        rs.getString("anom") + " " + rs.getString("aape")
                });
            }
        }
        return data;
    }

    /** Solo para el Asesor: lista sus solicitudes. */
    public List<Object[]> listarPorAsesor(int asesorId) throws SQLException {
        String sql = "SELECT s.id, s.fecha, s.monto, s.estado, c.dni, c.nombres, c.apellidos " +
                "FROM solicitud s JOIN cliente c ON c.id = s.cliente_id " +
                "WHERE s.asesor_id = ? ORDER BY s.id DESC";
        List<Object[]> data = new ArrayList<>();
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, asesorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(new Object[]{
                            rs.getInt("id"),
                            rs.getDate("fecha"),
                            rs.getBigDecimal("monto"),
                            rs.getString("estado"),
                            rs.getString("dni"),
                            rs.getString("nombres") + " " + rs.getString("apellidos")
                    });
                }
            }
        }
        return data;
    }
}
