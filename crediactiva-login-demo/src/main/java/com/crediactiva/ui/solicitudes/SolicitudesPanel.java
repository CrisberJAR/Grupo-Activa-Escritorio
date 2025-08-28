package com.crediactiva.ui.solicitudes;

import com.crediactiva.dao.SolicitudDao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SolicitudesPanel extends JPanel {

    private final int asesorId;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Fecha", "Monto", "Estado", "DNI", "Cliente"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public SolicitudesPanel(JFrame owner, int asesorId) {
        this.asesorId = asesorId;

        setLayout(new BorderLayout());
        JLabel title = new JLabel("Mis solicitudes", SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JButton btnNueva = new JButton("Nueva solicitud");
        btnNueva.addActionListener(e -> {
            SolicitudFormDialog dlg = new SolicitudFormDialog(owner, asesorId, (id) -> cargar());
            dlg.setVisible(true);
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(btnNueva, BorderLayout.EAST);

        JTable table = new JTable(model);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        cargar();
    }

    private void cargar() {
        try {
            model.setRowCount(0);
            SolicitudDao dao = new SolicitudDao();
            List<Object[]> data = dao.listarPorAsesor(asesorId);
            for (Object[] row : data) model.addRow(row);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
