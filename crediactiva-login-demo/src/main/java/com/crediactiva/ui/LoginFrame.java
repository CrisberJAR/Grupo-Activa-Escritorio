package com.crediactiva.ui;

import com.crediactiva.dao.UserDao;
import com.crediactiva.model.User;

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
            JOptionPane.showMessageDialog(this, "Bienvenido " + user.getUsername() + " [" + user.getRol() + "]");
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales inválidas o usuario inactivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
