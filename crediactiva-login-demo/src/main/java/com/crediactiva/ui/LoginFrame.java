package com.crediactiva.ui;

import com.crediactiva.dao.UserDao;
import com.crediactiva.model.User;
import com.crediactiva.security.Role;
import com.crediactiva.security.Session;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField(16);
    private final JPasswordField txtPass = new JPasswordField(16);
    private final JButton btnLogin = new JButton("Ingresar");

    public LoginFrame() {
        super("CrediActiva - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin, gbc);

        setContentPane(panel);
    }

    private void login() {
        String u = txtUser.getText().trim();
        String p = String.valueOf(txtPass.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa usuario y contraseña", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserDao dao = new UserDao();
        Optional<User> userOpt = dao.authenticate(u, p);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Abrir ventana principal con sesión y rol correcto
            abrirMainWindow(user);

        } else {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas o usuario inactivo",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Crea la sesión y abre la ventana principal según el rol del usuario. */
    private void abrirMainWindow(User user) {
        Role role = mapRole(user.getRol()); // "ADMIN" | "ASESOR" | "CLIENTE"
        if (role == null) {
            JOptionPane.showMessageDialog(this,
                    "El rol del usuario no es válido: " + user.getRol(),
                    "Error de rol", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Session session = new Session(user.getId(), user.getUsername(), role);

        // Mostrar MainWindow y cerrar el login
        SwingUtilities.invokeLater(() -> new MainWindow(session).setVisible(true));
        dispose();
    }

    /** Mapea el String de la BD al enum Role de forma segura. */
    private Role mapRole(String rolStr) {
        if (rolStr == null) return null;
        try {
            return Role.valueOf(rolStr.trim().toUpperCase()); // admite "admin", "ASESOR", etc.
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
