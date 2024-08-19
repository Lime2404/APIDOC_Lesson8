package Lesson8;

public class Authorisation {
    private String username;
    private String password;

    public Authorisation(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Authorisation(){}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
