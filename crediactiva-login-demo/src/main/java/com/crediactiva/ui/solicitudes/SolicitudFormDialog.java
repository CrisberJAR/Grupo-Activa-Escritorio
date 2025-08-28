package com.crediactiva.ui.solicitudes;

import com.crediactiva.dao.ClienteDao;
import com.crediactiva.dao.SolicitudDao;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class SolicitudFormDialog extends JDialog {

    // Datos cliente
    private final JTextField txtDni = new JTextField(12);
    private final JTextField txtNom = new JTextField(18);
    private final JTextField txtApe = new JTextField(18);
    private final JTextField txtDir = new JTextField(24);
    private final JTextField txtMaps = new JTextField(24);
    private final JTextField txtTel = new JTextField(14);
    private final JTextField txtLat = new JTextField(8);
    private final JTextField txtLon = new JTextField(8);

    // Datos solicitud
    private final JSpinner spFecha = new JSpinner(new SpinnerDateModel());
    private final JTextField txtMonto = new JTextField(10);
    private final JTextField txtMotivo = new JTextField(24);
    private final JTextField txtIngreso = new JTextField(10);

    private final int asesorId;

    public interface OnCreated { void ok(int solicitudId); }

    public SolicitudFormDialog(Window owner, int asesorId, OnCreated cb) {
        super(owner, "Nueva solicitud de préstamo", ModalityType.APPLICATION_MODAL);
        this.asesorId = asesorId;

        setSize(640, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,8,6,8);
        g.fill = GridBagConstraints.HORIZONTAL;
        int y=0;

        // Cliente
        g.gridx=0; g.gridy=y; form.add(new JLabel("DNI:"), g);
        g.gridx=1; form.add(txtDni, g);
        g.gridx=2; form.add(new JLabel("Tel:"), g);
        g.gridx=3; form.add(txtTel, g); y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Nombres:"), g);
        g.gridx=1; form.add(txtNom, g);
        g.gridx=2; form.add(new JLabel("Apellidos:"), g);
        g.gridx=3; form.add(txtApe, g); y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Dirección:"), g);
        g.gridx=1; g.gridwidth=3; form.add(txtDir, g); g.gridwidth=1; y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Google Maps URL:"), g);
        g.gridx=1; g.gridwidth=3; form.add(txtMaps, g); g.gridwidth=1; y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Lat:"), g);
        g.gridx=1; form.add(txtLat, g);
        g.gridx=2; form.add(new JLabel("Lon:"), g);
        g.gridx=3; form.add(txtLon, g); y++;

        // Solicitud
        g.gridx=0; g.gridy=y; form.add(new JLabel("Fecha:"), g);
        ((JSpinner.DateEditor) (spFecha.getEditor())).getFormat().applyPattern("yyyy-MM-dd");
        g.gridx=1; form.add(spFecha, g);
        g.gridx=2; form.add(new JLabel("Monto:"), g);
        g.gridx=3; form.add(txtMonto, g); y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Motivo:"), g);
        g.gridx=1; g.gridwidth=3; form.add(txtMotivo, g); g.gridwidth=1; y++;

        g.gridx=0; g.gridy=y; form.add(new JLabel("Ingreso mensual:"), g);
        g.gridx=1; form.add(txtIngreso, g); y++;

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar = new JButton("Enviar solicitud");
        bottom.add(btnCancelar); bottom.add(btnGuardar);

        add(new JLabel("Complete los datos del cliente y del préstamo", SwingConstants.LEFT), BorderLayout.NORTH);
        add(new JScrollPane(form), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar(cb));
    }

    private void guardar(OnCreated cb) {
        try {
            String dni = txtDni.getText().trim();
            String nom = txtNom.getText().trim();
            String ape = txtApe.getText().trim();
            String dir = txtDir.getText().trim();
            String maps = txtMaps.getText().trim();
            String tel = txtTel.getText().trim();
            Double lat = txtLat.getText().isBlank()? null : Double.parseDouble(txtLat.getText().trim());
            Double lon = txtLon.getText().isBlank()? null : Double.parseDouble(txtLon.getText().trim());

            double monto = Double.parseDouble(txtMonto.getText().trim());
            String motivo = txtMotivo.getText().trim();
            Double ingreso = txtIngreso.getText().isBlank()? null : Double.parseDouble(txtIngreso.getText().trim());
            java.util.Date fechaUtil = (java.util.Date) spFecha.getValue();
            Date fecha = new Date(fechaUtil.getTime());

            if (dni.isEmpty() || nom.isEmpty() || ape.isEmpty()) {
                JOptionPane.showMessageDialog(this, "DNI, Nombres y Apellidos son obligatorios",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ClienteDao cdao = new ClienteDao();
            Integer clienteId = cdao.findIdByDni(dni);
            if (clienteId == null) {
                clienteId = cdao.createCliente(dni, nom, ape, dir, maps, tel, lat, lon);
            }

            SolicitudDao sdao = new SolicitudDao();
            int solId = sdao.createSolicitud(clienteId, asesorId, fecha, monto, motivo, ingreso);

            if (cb != null) cb.ok(solId);
            JOptionPane.showMessageDialog(this, "Solicitud registrada con ID: " + solId + " (PENDIENTE)");
            dispose();

        } catch (NumberFormatException nx) {
            JOptionPane.showMessageDialog(this, "Revisa los valores numéricos (monto/ingreso/lat/lon).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
