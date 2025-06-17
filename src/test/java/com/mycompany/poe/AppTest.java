package com.mycompany.poe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class AppTest {

    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream outputStreamCaptor;

    private Field sentMessagesField;
    private Field storedMessagesField;
    private Field disregardedMessagesField;
    private Field allMessageHashesField;
    private Field allMessageIDsField;
    private Field messageCounterField;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        sentMessagesField = App.class.getDeclaredField("sentMessages");
        sentMessagesField.setAccessible(true);
        storedMessagesField = App.class.getDeclaredField("storedMessages");
        storedMessagesField.setAccessible(true);
        disregardedMessagesField = App.class.getDeclaredField("disregardedMessages");
        disregardedMessagesField.setAccessible(true);
        allMessageHashesField = App.class.getDeclaredField("allMessageHashes");
        allMessageHashesField.setAccessible(true);
        allMessageIDsField = App.class.getDeclaredField("allMessageIDs");
        allMessageIDsField.setAccessible(true);
        messageCounterField = App.class.getDeclaredField("messageCounter");
        messageCounterField.setAccessible(true);

        ((List<Message>) sentMessagesField.get(null)).clear();
        ((List<Message>) storedMessagesField.get(null)).clear();
        ((List<Message>) disregardedMessagesField.get(null)).clear();
        ((List<String>) allMessageHashesField.get(null)).clear();
        ((List<String>) allMessageIDsField.get(null)).clear();
        messageCounterField.set(null, 0);

        cleanupTestFiles();
    }

    @AfterEach
    void tearDown() throws IllegalAccessException {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);

        ((List<Message>) sentMessagesField.get(null)).clear();
        ((List<Message>) storedMessagesField.get(null)).clear();
        ((List<Message>) disregardedMessagesField.get(null)).clear();
        ((List<String>) allMessageHashesField.get(null)).clear();
        ((List<String>) allMessageIDsField.get(null)).clear();
        messageCounterField.set(null, 0);

        cleanupTestFiles();
    }

    private void cleanupTestFiles() {
        try (Stream<Path> walk = Files.walk(Path.of("."))) {
            walk.filter(p -> p.getFileName().toString().startsWith("stored_message_") && p.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Error during test file cleanup: " + e.getMessage());
        }
    }

    @Test
    void testDisplaySenderAndRecipientOfSentMessages_NoMessages() throws IllegalAccessException {
        App.displaySenderAndRecipientOfSentMessages();
        assertTrue(outputStreamCaptor.toString().contains("No messages have been sent yet."));
    }

    @Test
    void testDisplaySenderAndRecipientOfSentMessages_WithMessages() throws IllegalAccessException {
        List<Message> sentMessages = (List<Message>) sentMessagesField.get(null);
        sentMessages.add(new Message("Sender1", "+27123456789", "Content1", 1));
        sentMessages.add(new Message("Sender2", "+27987654321", "Content2", 2));

        App.displaySenderAndRecipientOfSentMessages();

        assertTrue(outputStreamCaptor.toString().trim().contains("Sender: Sender1, Recipient: +27123456789"));
        assertTrue(outputStreamCaptor.toString().trim().contains("Sender: Sender2, Recipient: +27987654321"));
    }

    @Test
    void testDisplayLongestSentMessage_NoMessages() throws IllegalAccessException {
        App.displayLongestSentMessage();
        assertTrue(outputStreamCaptor.toString().contains("No messages have been sent yet to determine the longest."));
    }

    @Test
    void testDisplayLongestSentMessage_MultipleMessages() throws IllegalAccessException {
        List<Message> sentMessages = (List<Message>) sentMessagesField.get(null);
        sentMessages.add(new Message("S1", "+271", "Short message.", 1));
        sentMessages.add(new Message("S2", "+272", "A very, very, very long message that should be the longest one.", 2));
        sentMessages.add(new Message("S3", "+273", "Medium msg.", 3));

        App.displayLongestSentMessage();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("A very, very, very long message that should be the longest one."));
        assertTrue(output.contains("Longest Message (63 characters):"));
        assertTrue(output.contains("Sender: S2"));
        assertTrue(output.contains("Recipient: +272"));
    }

    @Test
    void testSearchMessageByID_FoundInSent() throws IllegalAccessException {
        List<Message> sentMessages = (List<Message>) sentMessagesField.get(null);
        Message msg = new Message("SearchSender", "+27123", "SearchContent", 10);
        sentMessages.add(msg);

        String simulatedInput = msg.getMessageID() + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner scanner = new Scanner(System.in);

        App.searchMessageByID(scanner);
        scanner.close();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Message Found:"));
        assertTrue(output.contains("MessageID: " + msg.getMessageID()));
        assertTrue(output.contains("Sender: SearchSender"));
    }

    @Test
    void testSearchMessageByID_NotFound() throws IllegalAccessException {
        String simulatedInput = "NON_EXISTENT_ID" + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner scanner = new Scanner(System.in);

        App.searchMessageByID(scanner);
        scanner.close();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Message with ID 'NON_EXISTENT_ID' not found."));
    }

    @Test
    void testParseJsonToMessage_ValidJson() throws Exception {
        String json = """
            {
                "messageID": "MSG-0001",
                "sender": "John Doe",
                "recipient": "+27123456789",
                "content": "Hello World",
                "messageNumber": 1,
                "hash": "somehash"
            }
            """;
        java.lang.reflect.Method parseJsonMethod = App.class.getDeclaredMethod("parseJsonToMessage", String.class);
        parseJsonMethod.setAccessible(true);
        Message message = (Message) parseJsonMethod.invoke(null, json);

        assertNotNull(message);
        assertEquals("John Doe", message.getSender());
        assertEquals("+27123456789", message.getRecipient());
        assertEquals("Hello World", message.getContent());
        assertEquals(1, message.getMessageNumber());
        assertEquals("MSG-0001", message.getMessageID());
    }

    @Test
    void testExtractJsonValue() throws Exception {
        String json = """
            {
                "messageID": "MSG-0001",
                "sender": "John Doe",
                "recipient": "+27123456789",
                "content": "Hello World",
                "messageNumber": 1,
                "hash": "somehash"
            }
            """;
        java.lang.reflect.Method extractMethod = App.class.getDeclaredMethod("extractJsonValue", String.class, String.class);
        extractMethod.setAccessible(true);

        assertEquals("MSG-0001", extractMethod.invoke(null, json, "messageID"));
        assertEquals("John Doe", extractMethod.invoke(null, json, "sender"));
        assertEquals("Hello World", extractMethod.invoke(null, json, "content"));
        assertEquals("1", extractMethod.invoke(null, json, "messageNumber"));
        assertEquals("somehash", extractMethod.invoke(null, json, "hash"));
        assertNull(extractMethod.invoke(null, json, "nonExistentKey"));
    }

    @Test
    void testLoadStoredMessagesFromJSON_NoFiles() throws IllegalAccessException {
        App.loadStoredMessagesFromJSON();
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("No stored message JSON files found in the current directory."));
        assertEquals(0, ((List<Message>) storedMessagesField.get(null)).size());
    }

    @Test
    void testLoadStoredMessagesFromJSON_WithFiles() throws IOException, IllegalAccessException {
        String filename1 = "stored_message_MSG-T001.json";
        String filename2 = "stored_message_MSG-T002.json";

        Files.writeString(Path.of(filename1), """
            {
                "messageID": "MSG-T001",
                "sender": "TestUser1",
                "recipient": "+27111111111",
                "content": "Test content 1",
                "messageNumber": 1001,
                "hash": "hash1"
            }
            """);
        Files.writeString(Path.of(filename2), """
            {
                "messageID": "MSG-T002",
                "sender": "TestUser2",
                "recipient": "+27222222222",
                "content": "Test content 2",
                "messageNumber": 1002,
                "hash": "hash2"
            }
            """);

        App.loadStoredMessagesFromJSON();

        List<Message> storedMessages = (List<Message>) storedMessagesField.get(null);
        assertEquals(2, storedMessages.size());
        assertTrue(storedMessages.stream().anyMatch(m -> m.getMessageID().equals("MSG-T001")));
        assertTrue(storedMessages.stream().anyMatch(m -> m.getSender().equals("TestUser2")));

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Loaded 2 messages from JSON files."));

        Files.deleteIfExists(Path.of(filename1));
        Files.deleteIfExists(Path.of(filename2));
    }
}