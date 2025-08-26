package com.crediactiva.model;

public class User {
    private int id;
    private String username;
    private String rol;
    private String estado;

    public User(int id, String username, String rol, String estado) {
        this.id = id;
        this.username = username;
        this.rol = rol;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }
}
