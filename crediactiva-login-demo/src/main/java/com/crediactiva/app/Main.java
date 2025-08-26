package com.crediactiva.app;

import com.crediactiva.dao.UserDao;
import com.crediactiva.model.User;
import com.crediactiva.ui.LoginFrame;

import java.io.Console;
import java.util.Optional;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {

            // Abre la UI Swing
            javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));


        // Login por consola
        System.out.println("=== CrediActiva :: Login por consola ===");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Usuario: ");
        String username = scanner.nextLine();

        char[] pwdChars = null;
        Console console = System.console();
        if (console != null) {
            pwdChars = console.readPassword("Contraseña: ");
        } else {
            System.out.print("Contraseña: ");
            pwdChars = scanner.nextLine().toCharArray();
        }
        String password = new String(pwdChars);

        UserDao userDao = new UserDao();
        Optional<User> userOpt = userDao.authenticate(username, password);

        if (userOpt.isPresent()) {
            User u = userOpt.get();
            System.out.println("✅ Bienvenido, " + u.getUsername() + " [" + u.getRol() + "]");
        } else {
            System.out.println("❌ Credenciales inválidas o usuario inactivo.");
        }
    }
}
