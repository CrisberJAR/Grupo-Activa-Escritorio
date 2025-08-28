package com.crediactiva.ui;

import com.crediactiva.dao.UserDao;
import com.crediactiva.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserListDialog extends JDialog {

    private final UserDao userDao = new UserDao();
    private final DefaultTableModel model;

    public UserListDialog(Window owner) {
        super(owner, "Listado de usuarios", ModalityType.APPLICATION_MODAL);
        setSize(600, 400);
        setLocationRelativeTo(owner);

        String[] cols = {"ID", "Usuario", "Rol", "Estado"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            List<User> lista = userDao.findAll();
            model.setRowCount(0); // limpiar
            for (User u : lista) {
                model.addRow(new Object[]{u.getId(), u.getUsername(), u.getRol(), u.getEstado()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
