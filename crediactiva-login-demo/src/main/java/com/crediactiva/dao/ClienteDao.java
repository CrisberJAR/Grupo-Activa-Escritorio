package com.crediactiva.dao;

import com.crediactiva.db.ConnectionFactory;

import java.sql.*;

public class ClienteDao {

    public Integer findIdByDni(String dni) {
        final String sql = "SELECT id FROM cliente WHERE dni = ? LIMIT 1";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Crea un cliente y devuelve su ID. */
    public int createCliente(String dni, String nombres, String apellidos,
                             String direccion, String mapsUrl, String telefono,
                             Double lat, Double lon) throws SQLException {
        final String sql = "INSERT INTO cliente (dni, nombres, apellidos, direccion, maps_url, telefono, lat, lon) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dni);
            ps.setString(2, nombres);
            ps.setString(3, apellidos);
            ps.setString(4, direccion);
            ps.setString(5, mapsUrl);
            ps.setString(6, telefono);
            if (lat == null) ps.setNull(7, Types.DECIMAL); else ps.setDouble(7, lat);
            if (lon == null) ps.setNull(8, Types.DECIMAL); else ps.setDouble(8, lon);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
                throw new SQLException("No se obtuvo ID de cliente");
            }
        }
    }
}

