package com.crediactiva.dao;

import com.crediactiva.db.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AsesorDao {

    /** Devuelve el ID del asesor por el ID de usuario (tabla usuario). */
    public Integer findIdByUsuarioId(int usuarioId) {
        final String sql = "SELECT id FROM asesor WHERE usuario_id = ? LIMIT 1";
        try (Connection cn = ConnectionFactory.getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
