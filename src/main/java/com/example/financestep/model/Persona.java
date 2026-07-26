package com.example.financestep.model;

public abstract class Persona {
    private int id;
    private String username;
    private String password;

    public Persona(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Methods

    public boolean login(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public void logout() {
        // Logica di logout
    }

    // Getter and Setter

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
