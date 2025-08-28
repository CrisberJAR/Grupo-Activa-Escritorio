package com.crediactiva.ui;

import javax.swing.*;
import java.awt.*;

public class UsersPanel extends JPanel {

    private final JFrame owner;

    public UsersPanel(JFrame owner) {
        this.owner = owner;
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Gestión de usuarios", SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        // Botones de acción
        JButton btnNuevo = new JButton("Nuevo usuario (Asesor / Cliente)");
        JButton btnListar = new JButton("Listar todos los usuarios");

        btnNuevo.addActionListener(e -> {
            UserFormDialog dlg = new UserFormDialog(owner, (id, u, r) -> {
                // cuando tengamos tabla, refrescamos
            });
            dlg.setVisible(true);
        });

        btnListar.addActionListener(e -> {
            UserListDialog dlg = new UserListDialog(owner);
            dlg.setVisible(true);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actions.add(btnNuevo);
        actions.add(btnListar);

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // Placeholder por ahora
        add(new JLabel("Selecciona una acción arriba.", SwingConstants.CENTER),
                BorderLayout.CENTER);
    }
}
