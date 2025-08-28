package com.crediactiva.ui.solicitudes;

import com.crediactiva.dao.SolicitudDao;
import com.crediactiva.service.SolicitudService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminSolicitudesPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","Fecha","Monto","Estado","ClienteID","DNI","Cliente","AsesorID","Asesor"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JComboBox<String> cbOrden = new JComboBox<>(new String[]{"id","fecha","asesor","estado"});

    public AdminSolicitudesPanel() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Solicitudes (Administrador)", SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnAprobar  = new JButton("Aprobar");
        JButton btnRechazar = new JButton("Rechazar");

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        left.add(title);
        left.add(new JLabel("Ordenar por:"));
        left.add(cbOrden);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,10));
        right.add(btnRefrescar);
        right.add(btnAprobar);
        right.add(btnRechazar);

        JPanel top = new JPanel(new BorderLayout());
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnRefrescar.addActionListener(e -> cargar());
        btnAprobar.addActionListener(e -> cambiarEstado(table, true));
        btnRechazar.addActionListener(e -> cambiarEstado(table, false));

        cbOrden.addActionListener(e -> cargar());

        cargar();
    }

    private void cargar() {
        try {
            model.setRowCount(0);
            SolicitudDao dao = new SolicitudDao();
            List<Object[]> data = dao.listarSolicitudes((String) cbOrden.getSelectedItem());
            for (Object[] row : data) {
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado(JTable table, boolean aprobar) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una solicitud en la tabla",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int solicitudId = (int) model.getValueAt(row, 0);
        String estadoActual = (String) model.getValueAt(row, 3);
        if (!"PENDIENTE".equalsIgnoreCase(estadoActual)) {
            JOptionPane.showMessageDialog(this, "Solo se pueden cambiar solicitudes en estado PENDIENTE",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                (aprobar ? "¿Aprobar" : "¿Rechazar") + " la solicitud " + solicitudId + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            SolicitudService svc = new SolicitudService();
            if (aprobar) {
                svc.aprobar(solicitudId);
            } else {
                svc.rechazar(solicitudId);
            }
            cargar();
            JOptionPane.showMessageDialog(this, (aprobar ? "Aprobada" : "Rechazada") + " correctamente.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Operación fallida: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

