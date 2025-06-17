package com.mycompany.poe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class MessageTest {

    private String senderName;
    private String validRecipient;
    private String validContent;

    @BeforeEach
    void setUp() {
        senderName = "Test Sender";
        validRecipient = "+27831234567";
        validContent = "This is a valid test message.";
        // Ensure no leftover test files from previous runs
        cleanupTestFiles();
    }

    @AfterEach
    void tearDown() {
        // Clean up any files created during the test
        cleanupTestFiles();
    }

    // Helper method to clean up test JSON files
    private void cleanupTestFiles() {
        try (Stream<Path> walk = Files.walk(Path.of("."))) {
            walk.filter(p -> p.getFileName().toString().startsWith("stored_message_") && p.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.reverseOrder()) // Sort in reverse to delete inner directories first if any
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Error during test file cleanup: " + e.getMessage());
        }
    }

    // --- Tests for Message Constructor and Getters ---
    @Test
    void testMessageConstructorAndGetters() {
        Message message = new Message(senderName, validRecipient, validContent, 1);
        assertEquals(senderName, message.getSender());
        assertEquals(validRecipient, message.getRecipient());
        assertEquals(validContent, message.getContent());
        assertEquals(1, message.getMessageNumber());
        assertNotNull(message.getMessageID());
        assertNotNull(message.getHash());
        // Message ID should be in the format 'MSG-XXXX' where XXXX is messageNumber
        assertTrue(message.getMessageID().startsWith("MSG-"));
        assertEquals("MSG-0001", message.getMessageID()); // For message number 1, it should be MSG-0001
    }

    @Test
    void testMessageIDFormatting() {
        Message message1 = new Message(senderName, validRecipient, validContent, 5);
        assertEquals("MSG-0005", message1.getMessageID());

        Message message2 = new Message(senderName, validRecipient, validContent, 100);
        assertEquals("MSG-0100", message2.getMessageID());

        Message message3 = new Message(senderName, validRecipient, validContent, 1234);
        assertEquals("MSG-1234", message3.getMessageID());
    }

    // --- Tests for checkRecipientCell ---
    @Test
    void testCheckRecipientCell_Valid() {
        Message message = new Message(senderName, "+27831234567", validContent, 1);
        assertEquals(0, message.checkRecipientCell()); // 0 indicates valid

        message = new Message(senderName, "+271234567890", validContent, 2);
        assertEquals(0, message.checkRecipientCell()); // 13 chars long
    }

    @Test
    void testCheckRecipientCell_NoPlusSign() {
        Message message = new Message(senderName, "27831234567", validContent, 1);
        assertEquals(1, message.checkRecipientCell()); // 1 indicates not starting with '+'
    }

    @Test
    void testCheckRecipientCell_TooShort() {
        Message message = new Message(senderName, "+278312345", validContent, 1); // 11 chars
        assertEquals(2, message.checkRecipientCell()); // 2 indicates too short (based on +27 plus 10 digits as "normal")
    }

    @Test
    void testCheckRecipientCell_TooLong() {
        Message message = new Message(senderName, "+2783123456789", validContent, 1); // 14 chars
        assertEquals(2, message.checkRecipientCell()); // 2 indicates too long
    }

    @Test
    void testCheckRecipientCell_ContainsNonDigitsAfterPlus27() {
        Message message = new Message(senderName, "+27abcde1234", validContent, 1);
        assertEquals(2, message.checkRecipientCell()); // Should fail as it contains non-digits
    }

    // --- Tests for validateMessageLength ---
    @Test
    void testValidateMessageLength_Valid() {
        String shortContent = "Hello.";
        Message message = new Message(senderName, validRecipient, shortContent, 1);
        assertEquals("Message ready to send.", message.validateMessageLength());

        String maxContent = "a".repeat(250); // 250 characters
        message = new Message(senderName, validRecipient, maxContent, 2);
        assertEquals("Message ready to send.", message.validateMessageLength());
    }

    @Test
    void testValidateMessageLength_TooLong() {
        String tooLongContent = "a".repeat(251); // 251 characters
        Message message = new Message(senderName, validRecipient, tooLongContent, 1);
        assertEquals("Message must be 250 characters or less.", message.validateMessageLength());
    }

    @Test
    void testValidateMessageLength_Empty() {
        String emptyContent = "";
        Message message = new Message(senderName, validRecipient, emptyContent, 1);
        assertEquals("Message must be 250 characters or less.", message.validateMessageLength()); // Or you might want a specific error for empty
    }


    // --- Tests for storeMessage (requires file system interaction) ---
    @Test
    void testStoreMessage_CreatesFileAndContent() throws IOException {
        Message message = new Message(senderName, validRecipient, validContent, 99);
        String filename = "stored_message_" + message.getMessageID() + ".json";
        Path filePath = Path.of(filename);

        message.storeMessage(filename);

        // Assert file exists
        assertTrue(Files.exists(filePath), "JSON file should be created.");

        // Assert file content
        String fileContent = Files.readString(filePath);
        assertTrue(fileContent.contains("\"messageID\": \"" + message.getMessageID() + "\""));
        assertTrue(fileContent.contains("\"sender\": \"" + senderName + "\""));
        assertTrue(fileContent.contains("\"recipient\": \"" + validRecipient + "\""));
        assertTrue(fileContent.contains("\"content\": \"" + validContent + "\""));
        assertTrue(fileContent.contains("\"messageNumber\": " + message.getMessageNumber()));
        assertTrue(fileContent.contains("\"hash\": \"" + message.getHash() + "\""));

        // Clean up the created file immediately after the test
        Files.deleteIfExists(filePath);
    }
}