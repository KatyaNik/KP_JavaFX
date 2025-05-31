package model;

public class CurrentUser {
    private final int id;
    private final String username;
    private final String email;
    private final int roleId;

    public CurrentUser(int id, String username, String email, int roleId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roleId = roleId;
    }

    // Геттеры
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getRoleId() { return roleId; }
}