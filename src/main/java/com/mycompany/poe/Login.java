package com.mycompany.poe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {
    private List<User> registeredUsers;
    private User loggedInUser = null; // To keep track of the logged-in user

    public Login() {
        this.registeredUsers = new ArrayList<>();
    }

    public boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity(String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;
        String specialCharacters = "!@#$%^&*()_+=-`~[]\\{}|;':\",./<>?";

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (specialCharacters.contains(String.valueOf(c))) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasNumber && hasSpecial;
    }

    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        // Updated regex to allow up to 10 digits after +27 (total 13 chars)
        Pattern pattern = Pattern.compile("^\\+27\\d{9,10}$"); // Changed from 0,9 to 9,10 for 12/13 total length
        Matcher matcher = pattern.matcher(cellPhoneNumber);
        return matcher.matches();
    }

    public String registerUser(String username, String password, String firstName, String lastName, String cellPhoneNumber) {
        if (!checkUserName(username)) {
            return "Username not correctly formatted, please make sure that your username contains an underscore and is no more than five characters .";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password not correctly formatted; please make sure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(cellPhoneNumber)) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }

        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                return "Username already exists. Please use a different username.";
            }
        }

        User newUser = new User(username, password, firstName, lastName, cellPhoneNumber);
        registeredUsers.add(newUser);
        return "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.\nUser registered successfully.";
    }

    public boolean loginUser(String username, String password) {
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                this.loggedInUser = user; // Set the logged-in user
                return true;
            }
        }
        this.loggedInUser = null; // reset and set to null if login is unsuccessful
        return false;
    }

    public String returnLoginStatus(boolean success) {
        if (success && loggedInUser != null) {
            return "Welcome " + loggedInUser.getFirstName() + ", " + loggedInUser.getLastName() + "! It is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }

    // Task 3 Added  method to grant access to the logged-in user object
    public User getLoggedInUser() {
        return loggedInUser;
    }
}