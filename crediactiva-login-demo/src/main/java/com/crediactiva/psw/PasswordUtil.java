package com.crediactiva.psw;

import org.mindrot.jbcrypt.BCrypt;

import java.io.Console;
import java.util.Scanner;

public class PasswordUtil {

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static void main(String[] args) {
        String password;
        if (args.length > 0) {
            password = args[0];
        } else {
            Console console = System.console();
            if (console != null) {
                char[] pwd = console.readPassword("Ingrese contraseña a hashear: ");
                password = new String(pwd);
            } else {
                System.out.print("Ingrese contraseña a hashear: ");
                password = new Scanner(System.in).nextLine();
            }
        }
        String h = hash(password);
        System.out.println("Hash BCrypt: " + h);
        System.out.println("Úsalo en la columna usuario.password_hash");
    }
}
