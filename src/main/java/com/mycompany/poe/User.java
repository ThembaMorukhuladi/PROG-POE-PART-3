
package com.mycompany.poe;

/**
 *
 * @author Themba
 */
public class User {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String cellPhoneNumber;

    public User(String username, String password, String firstName, String lastName, String cellPhoneNumber) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cellPhoneNumber = cellPhoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCellPhoneNumber() {
        return cellPhoneNumber;
    }
}