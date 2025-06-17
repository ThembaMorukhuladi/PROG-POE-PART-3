package com.mycompany.poe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    private Login loginSystem; // An instance of the Login class to be tested

    @BeforeEach
    void setUp() {
        // Initializes a fresh Login system before each test
        loginSystem = new Login();
    }

    // --- Tests for checkUserName ---
    @Test
    void testCheckUserName_Valid() {
        assertTrue(loginSystem.checkUserName("kyl_1"));
        assertTrue(loginSystem.checkUserName("jane_"));
        assertTrue(loginSystem.checkUserName("a_b"));
    }

    @Test
    void testCheckUserName_NoUnderscore() {
        assertFalse(loginSystem.checkUserName("kyle1"));
    }

    @Test
    void testCheckUserName_TooLong() {
        assertFalse(loginSystem.checkUserName("kyl_12")); // 6 characters
        assertFalse(loginSystem.checkUserName("long_name"));
    }

    @Test
    void testCheckUserName_Empty() {
        assertFalse(loginSystem.checkUserName(""));
    }

    // --- Tests for checkPasswordComplexity ---
    @Test
    void testCheckPasswordComplexity_Valid() {
        assertTrue(loginSystem.checkPasswordComplexity("Password123!"));
        assertTrue(loginSystem.checkPasswordComplexity("MyP@ssw0rd"));
        assertTrue(loginSystem.checkPasswordComplexity("StrongP4$$"));
    }

    @Test
    void testCheckPasswordComplexity_TooShort() {
        assertFalse(loginSystem.checkPasswordComplexity("Short1!"));
    }

    @Test
    void testCheckPasswordComplexity_NoUppercase() {
        assertFalse(loginSystem.checkPasswordComplexity("password123!"));
    }

    @Test
    void testCheckPasswordComplexity_NoDigit() {
        assertFalse(loginSystem.checkPasswordComplexity("Password_xyz!"));
    }

    @Test
    void testCheckPasswordComplexity_NoSpecialChar() {
        assertFalse(loginSystem.checkPasswordComplexity("Password123"));
    }

    @Test
    void testCheckPasswordComplexity_Empty() {
        assertFalse(loginSystem.checkPasswordComplexity(""));
    }

    // --- Tests for checkCellPhoneNumber ---
    @Test
    void testCheckCellPhoneNumber_Valid() {
        // Based on your updated regex: ^\\+27\\d{9,10}$ (12 or 13 chars total)
        assertTrue(loginSystem.checkCellPhoneNumber("+27123456789"));  // 12 chars
        assertTrue(loginSystem.checkCellPhoneNumber("+271234567890")); // 13 chars
    }

    @Test
    void testCheckCellPhoneNumber_InvalidStart() {
        assertFalse(loginSystem.checkCellPhoneNumber("0831234567"));
        assertFalse(loginSystem.checkCellPhoneNumber("27123456789"));
    }

    @Test
    void testCheckCellPhoneNumber_TooShort() {
        assertFalse(loginSystem.checkCellPhoneNumber("+2712345678")); // 11 chars, too short for {9,10}
    }

    @Test
    void testCheckCellPhoneNumber_TooLong() {
        assertFalse(loginSystem.checkCellPhoneNumber("+2712345678901")); // 14 chars, too long for {9,10}
    }

    @Test
    void testCheckCellPhoneNumber_ContainsLetters() {
        assertFalse(loginSystem.checkCellPhoneNumber("+27123abc789"));
    }

    @Test
    void testCheckCellPhoneNumber_Empty() {
        assertFalse(loginSystem.checkCellPhoneNumber(""));
    }

    // --- Tests for registerUser ---
    @Test
    void testRegisterUser_Success() {
        String result = loginSystem.registerUser("john_d", "Pass123!", "John", "Doe", "+27831234567");
        assertEquals("Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.\nUser registered successfully.", result);
        // Verify user is actually registered
        assertTrue(loginSystem.loginUser("john_d", "Pass123!"));
    }

    @Test
    void testRegisterUser_InvalidUsername() {
        String result = loginSystem.registerUser("john_doe", "Pass123!", "John", "Doe", "+27831234567");
        assertTrue(result.contains("Username is not correctly formatted"));
    }

    @Test
    void testRegisterUser_InvalidPassword() {
        String result = loginSystem.registerUser("jane_", "pass", "Jane", "Smith", "+27831234567");
        assertTrue(result.contains("Password is not correctly formatted"));
    }

    @Test
    void testRegisterUser_InvalidPhoneNumber() {
        String result = loginSystem.registerUser("mike_", "Pass123!", "Mike", "Brown", "0831234567");
        assertTrue(result.contains("Cell phone number incorrectly formatted"));
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        loginSystem.registerUser("test_", "Pass123!", "Test", "User", "+27831234567");
        String result = loginSystem.registerUser("test_", "Pass123!", "Another", "User", "+27831234568");
        assertEquals("Username already exists. Please choose a different username.", result);
    }

    // --- Tests for loginUser ---
    @Test
    void testLoginUser_Success() {
        loginSystem.registerUser("user_a", "Pass123@", "User", "A", "+27831234567");
        assertTrue(loginSystem.loginUser("user_a", "Pass123@"));
        assertNotNull(loginSystem.getLoggedInUser());
        assertEquals("User", loginSystem.getLoggedInUser().getFirstName());
    }

    @Test
    void testLoginUser_IncorrectPassword() {
        loginSystem.registerUser("user_b", "Pass123#", "User", "B", "+27831234567");
        assertFalse(loginSystem.loginUser("user_b", "WrongPass"));
        assertNull(loginSystem.getLoggedInUser());
    }

    @Test
    void testLoginUser_UsernameNotFound() {
        loginSystem.registerUser("user_c", "Pass123$", "User", "C", "+27831234567");
        assertFalse(loginSystem.loginUser("non_exist", "Pass123$"));
        assertNull(loginSystem.getLoggedInUser());
    }

    // --- Tests for returnLoginStatus ---
    @Test
    void testReturnLoginStatus_Success() {
        loginSystem.registerUser("test_u", "Pass123%", "Test", "User", "+27831234567");
        loginSystem.loginUser("test_u", "Pass123%");
        String status = loginSystem.returnLoginStatus(true);
        assertEquals("Welcome Test, User! It is great to see you again.", status);
    }

    @Test
    void testReturnLoginStatus_Failure() {
        loginSystem.registerUser("test_v", "Pass123^", "Test", "V", "+27831234567");
        loginSystem.loginUser("test_v", "WrongPass"); // This makes login fail
        String status = loginSystem.returnLoginStatus(false);
        assertEquals("Username or password incorrect, please try again.", status);
    }

    // --- Tests for getLoggedInUser ---
    @Test
    void testGetLoggedInUser_WhenLoggedIn() {
        loginSystem.registerUser("logged_", "Secure123*", "Logged", "In", "+27831234567");
        loginSystem.loginUser("logged_", "Secure123*");
        User user = loginSystem.getLoggedInUser();
        assertNotNull(user);
        assertEquals("Logged", user.getFirstName());
        assertEquals("In", user.getLastName());
        assertEquals("logged_", user.getUsername());
    }

    @Test
    void testGetLoggedInUser_WhenNotLoggedIn() {
        User user = loginSystem.getLoggedInUser();
        assertNull(user);
    }

    @Test
    void testGetLoggedInUser_AfterFailedLogin() {
        loginSystem.registerUser("fail_log", "Pass123(", "Fail", "Login", "+27831234567");
        loginSystem.loginUser("fail_log", "WrongPass");
        User user = loginSystem.getLoggedInUser();
        assertNull(user);
    }
}