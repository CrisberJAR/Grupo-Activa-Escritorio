package com.crediactiva.ui;

import com.crediactiva.dao.UserDao;

import javax.swing.*;
import java.awt.*;

public class UserFormDialog extends JDialog {
    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JComboBox<String> cbRol = new JComboBox<>(new String[]{"ASESOR","CLIENTE"});
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnCancelar = new JButton("Cancelar");

    private final UserDao userDao = new UserDao();

    public interface OnCreated {
        void ok(int newUserId, String username, String role);
    }

    public UserFormDialog(Window owner, OnCreated callback) {
        super(owner, "Nuevo usuario", ModalityType.APPLICATION_MODAL);
        setSize(360, 230);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,10,6,10);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; form.add(new JLabel("Usuario:"), g);
        g.gridx=1; form.add(txtUsername, g);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Contraseña:"), g);
        g.gridx=1; form.add(txtPassword, g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Rol:"), g);
        g.gridx=1; form.add(cbRol, g);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnCancelar);
        actions.add(btnGuardar);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar(callback));
    }

    private void guardar(OnCreated cb) {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());
        String r = ((String) cbRol.getSelectedItem()).toUpperCase();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa usuario y contraseña",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int id = userDao.createUser(u, p, r);
            if (cb != null) cb.ok(id, u, r);
            JOptionPane.showMessageDialog(this, "Usuario creado: " + u + " (" + r + ")");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
