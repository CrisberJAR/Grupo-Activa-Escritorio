package com.crediactiva.ui;

import com.crediactiva.security.Role;
import com.crediactiva.security.Session;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

public class MainWindow extends JFrame {
    private final Session session;
    private final CardLayout card = new CardLayout();
    private final JPanel content = new JPanel(card);

    public MainWindow(Session session) {
        this.session = session;
        setTitle("CrediActiva — " + session.getUsername() + " (" + session.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
        setLayout(new BorderLayout());

        add(buildTopbar(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
        setJMenuBar(buildMenuBar());

        card.show(content, "dashboard");
    }

    private JComponent buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        JLabel brand = new JLabel("  CrediActiva");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));

        JLabel lblUser = new JLabel("Usuario: " + session.getUsername());
        JLabel lblRole = new JLabel("Rol: " + session.getRole());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        right.add(lblUser);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(lblRole);

        top.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        top.add(brand, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        side.add(btn("Dashboard", () -> show("dashboard")));

        if (session.getRole() == Role.ADMIN || session.getRole() == Role.ASESOR) {
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Clientes", () -> show("clientes")));
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Solicitudes", () -> show("solicitudes")));
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Préstamos", () -> show("prestamos")));
        }
        if (session.getRole() == Role.ADMIN) {
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Reportes", () -> show("reportes")));
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Usuarios", () -> show("usuarios")));
        }
        if (session.getRole() == Role.CLIENTE) {
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Mi Cronograma", () -> show("mi_cronograma")));
            side.add(Box.createVerticalStrut(8));
            side.add(btn("Contactar Asesor (WhatsApp)", this::openWhatsApp));
        }

        side.add(Box.createVerticalGlue());
        side.add(new JSeparator());
        side.add(Box.createVerticalStrut(8));
        side.add(btn("Cerrar sesión", this::logout));
        return side;
    }

    private JButton btn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JComponent buildContent() {
        content.add(placeholder("Dashboard / Resumen"), "dashboard");
        content.add(placeholder("Clientes (lista + filtros)"), "clientes");
        content.add(placeholder("Solicitudes (pendientes / aprobar / rechazar)"), "solicitudes");
        content.add(placeholder("Préstamos (activos / finalizados)"), "prestamos");
        content.add(placeholder("Reportes de cobros / por asesor / por rango"), "reportes");
        content.add(placeholder("Gestión de usuarios (solo Admin)"), "usuarios");
        content.add(placeholder("Mi Cronograma (Cliente)"), "mi_cronograma");
        return content;
    }

    private JPanel placeholder(String title) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 16f));
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mSistema = new JMenu("Sistema");
        JMenuItem miLogout = new JMenuItem("Cerrar sesión");
        miLogout.addActionListener(e -> logout());
        mSistema.add(miLogout);
        bar.add(mSistema);

        if (session.getRole() == Role.ADMIN || session.getRole() == Role.ASESOR) {
            JMenu mGestion = new JMenu("Gestión");
            JMenuItem miClientes = new JMenuItem("Clientes");
            miClientes.addActionListener(e -> show("clientes"));
            JMenuItem miSolicitudes = new JMenuItem("Solicitudes");
            miSolicitudes.addActionListener(e -> show("solicitudes"));
            mGestion.add(miClientes);
            mGestion.add(miSolicitudes);
            bar.add(mGestion);
        }

        if (session.getRole() == Role.ADMIN) {
            JMenu mAdmin = new JMenu("Administración");
            JMenuItem miUsuarios = new JMenuItem("Usuarios");
            miUsuarios.addActionListener(e -> show("usuarios"));
            JMenuItem miReportes = new JMenuItem("Reportes");
            miReportes.addActionListener(e -> show("reportes"));
            mAdmin.add(miUsuarios);
            mAdmin.add(miReportes);
            bar.add(mAdmin);
        }

        if (session.getRole() == Role.CLIENTE) {
            JMenu mCliente = new JMenu("Cliente");
            JMenuItem miCronograma = new JMenuItem("Mi Cronograma");
            miCronograma.addActionListener(e -> show("mi_cronograma"));
            JMenuItem miWhats = new JMenuItem("Contactar Asesor");
            miWhats.addActionListener(e -> openWhatsApp());
            mCliente.add(miCronograma);
            mCliente.add(miWhats);
            bar.add(mCliente);
        }

        return bar;
    }

    private void show(String name) { card.show(content, name); }

    private void openWhatsApp() {
        try {
            // TODO: reemplazar por el teléfono real desde la BD
            String phone = "51999999999";
            String msg = "Hola, tengo una consulta sobre mi préstamo.";
            String url = "https://wa.me/" + phone + "?text=" + java.net.URLEncoder.encode(msg, "UTF-8");
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir WhatsApp: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        dispose(); // Cierra y (si quieres) vuelve a mostrar el LoginFrame
        // new LoginFrame().setVisible(true);
    }
}
