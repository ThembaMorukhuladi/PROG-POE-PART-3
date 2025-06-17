package com.mycompany.poe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class App {
    private static boolean isUserLoggedIn = false;
    private static Login loginSystem = new Login();
    private static int totalMessagesSent = 0;

    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<String> allMessageHashes = new ArrayList<>();
    private static List<String> allMessageIDs = new ArrayList<>();

    private static int messageCounter = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Part 1: Login and Register
        while (!isUserLoggedIn) {
            System.out.println("\nWelcome to quick chat, register and login!");
            System.out.println();
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit Registration/Login");

            System.out.print("\nEnter your choice: ");
            String part1Choice = scanner.nextLine();

            switch (part1Choice) {
                case "1":
                    System.out.println("\nRegistration");
                    System.out.println();
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter first name: ");
                    String firstName = scanner.nextLine();
                    System.out.print("Enter last name: ");
                    String lastName = scanner.nextLine();
                    System.out.print("Enter South African cell phone number (e.g., +27838968976): ");
                    String phoneNumber = scanner.nextLine();

                    String registrationResult = loginSystem.registerUser(username, password, firstName, lastName, phoneNumber);
                    System.out.println(registrationResult);
                    break;

                case "2":
                    System.out.println("\nLogin");
                    System.out.println();
                    System.out.print("Enter username: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String loginPassword = scanner.nextLine();

                    boolean loginSuccess = loginSystem.loginUser(loginUsername, loginPassword);
                    String loginStatus = loginSystem.returnLoginStatus(loginSuccess);
                    System.out.println(loginStatus);
                    isUserLoggedIn = loginSuccess;
                    break;

                case "3":
                    System.out.println("Exiting Registration/Login.");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            if (part1Choice.equals("3")) break;
        }

        // Part 2: Messaging
        if (isUserLoggedIn) {
            System.out.println();
            System.out.println("Welcome to QuickChat.");
            System.out.println();
            while (true) {
                System.out.println();
                System.out.println("1. Send Messages");
                System.out.println("2. Show recently sent messages (Coming Soon)");
                System.out.println("3. Message Management");
                System.out.println("4. Quit QuickChat");
                System.out.print("Enter your choice: ");
                String part2Choice = scanner.nextLine();

                switch (part2Choice) {
                    case "1":
                        System.out.print("Enter the number of messages you want to send: ");
                        int numMessagesToSend = 0;
                        try {
                            numMessagesToSend = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number of messages.");
                            continue;
                        }

                        // Retrieve the logged-in user to get their name as the sender
                        User currentUser = loginSystem.getLoggedInUser();
                        String senderName = (currentUser != null) ?
                                currentUser.getFirstName() + " " + currentUser.getLastName() : "Unknown Sender";

                        for (int i = 0; i < numMessagesToSend; i++) {
                            messageCounter++;
                            System.out.println();
                            System.out.println("Message " + messageCounter);
                            System.out.println();
                            System.out.print("Enter recipient's cell number (e.g., +27...): ");
                            String recipient = scanner.nextLine();
                            System.out.print("Enter your message (max 250 characters): ");
                            String messageContent = scanner.nextLine();

                            // Pass senderName to the Message constructor
                            Message currentMessage = new Message(senderName, recipient, messageContent, messageCounter);

                            allMessageHashes.add(currentMessage.getHash());
                            allMessageIDs.add(currentMessage.getMessageID());

                            if (currentMessage.checkRecipientCell() != 0) {
                                String errorMsg = "Invalid recipient number format.";
                                if(currentMessage.checkRecipientCell() == 1) {
                                    errorMsg = "Recipient number must start with '+'.";
                                } else if (currentMessage.checkRecipientCell() == 2) {
                                    errorMsg = "Recipient number is too long (max 13 characters including '+').";
                                }
                                System.out.println(errorMsg);
                                continue;
                            }

                            String lengthValidation = currentMessage.validateMessageLength();
                            if (!lengthValidation.equals("Message ready to send.")) {
                                System.out.println(lengthValidation);
                                continue;
                            }

                            String messageAction = JOptionPane.showInputDialog(
                                null,
                                "Message Hash: " + currentMessage.getHash() + "\nChoose an action:\n- Send\n- Disregard\n- -Store- (Note: 'Store' saves to JSON file)",
                                "Send Message Options",
                                JOptionPane.QUESTION_MESSAGE
                            );

                            if (messageAction != null) {
                                messageAction = messageAction.trim().toLowerCase();
                                if (messageAction.equals("send")) {
                                    totalMessagesSent++;
                                    sentMessages.add(currentMessage);
                                    JOptionPane.showMessageDialog(
                                        null,
                                        "Message Sent!\n" +
                                        "MessageID: " + currentMessage.getMessageID() + "\n" +
                                        "Message Hash: " + currentMessage.getHash() + "\n" +
                                        "Sender: " + currentMessage.getSender() + "\n" +
                                        "Recipient: " + currentMessage.getRecipient() + "\n" +
                                        "Message: " + currentMessage.getContent(),
                                        "Message Sent",
                                        JOptionPane.INFORMATION_MESSAGE
                                    );
                                } else if (messageAction.equals("store")) {
                                    storedMessages.add(currentMessage);
                                    currentMessage.storeMessage("stored_message_" + currentMessage.getMessageID() + ".json");
                                    System.out.println("Message stored for later.");
                                } else if (messageAction.equals("disregard")) {
                                    disregardedMessages.add(currentMessage);
                                    System.out.println("Message disregarded.");
                                } else {
                                    System.out.println("Invalid action. Message not sent, stored, or disregarded.");
                                }
                            } else {
                                disregardedMessages.add(currentMessage);
                                System.out.println("Message action cancelled. Message disregarded.");
                            }
                        }
                        System.out.println("\nTotal messages sent in this session: " + totalMessagesSent);
                        break;

                    case "2":
                        System.out.println("Coming Soon.");
                        break;

                    case "3":
                        displayMessageManagementMenu(scanner);
                        break;

                    case "4":
                        System.out.println("Exiting QuickChat. Goodbye!");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }

        scanner.close();
    }
    
    
    
    //TASK 3

    private static void displayMessageManagementMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Message Management ---");
            System.out.println("a. Display sender and recipient of all sent messages.");
            System.out.println("b. Display the longest sent message.");
            System.out.println("c. Search for a message ID and display corresponding recipient and message.");
            System.out.println("d. Search for all messages sent to a particular recipient.");
            System.out.println("e. Delete a message using the message hash.");
            System.out.println("f. Display a report that lists full details of all sent messages.");
            System.out.println("g. Display Disregarded Messages.");
            System.out.println("h. Display All Message Hashes.");
            System.out.println("i. Display All Message IDs.");
            System.out.println("j. Load and Display Stored Messages from JSON files.");
            System.out.println("k. Back to Main QuickChat Menu");
            System.out.print("Enter your choice: ");
            String managementChoice = scanner.nextLine().toLowerCase();

            switch (managementChoice) {
                case "a":
                    displaySenderAndRecipientOfSentMessages();
                    break;
                case "b":
                    displayLongestSentMessage();
                    break;
                case "c":
                    searchMessageByID(scanner);
                    break;
                case "d":
                    searchMessagesByRecipient(scanner);
                    break;
                case "e":
                    deleteMessageByHash(scanner);
                    break;
                case "f":
                    displaySentMessagesReport();
                    break;
                case "g":
                    displayDisregardedMessages();
                    break;
                case "h":
                    displayAllMessageHashes();
                    break;
                case "i":
                    displayAllMessageIDs();
                    break;
                case "j":
                    loadStoredMessagesFromJSON();
                    displayLoadedStoredMessages();
                    break;
                case "k":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displaySenderAndRecipientOfSentMessages() {
        System.out.println("\n--- Sender and Recipient of All Sent Messages ---");
        if (sentMessages.isEmpty()) {
            System.out.println("No messages have been sent yet.");
            return;
        }
        for (Message msg : sentMessages) {
            System.out.println("Sender: " + msg.getSender() + ", Recipient: " + msg.getRecipient());
        }
    }

    private static void displayLongestSentMessage() {
        System.out.println("\n--- Longest Sent Message ---");
        if (sentMessages.isEmpty()) {
            System.out.println("No messages have been sent yet to determine the longest.");
            return;
        }

        Message longestMessage = null;
        for (Message msg : sentMessages) {
            if (longestMessage == null || msg.getContent().length() > longestMessage.getContent().length()) {
                longestMessage = msg;
            }
        }
        if (longestMessage != null) {
            System.out.println("Longest Message (" + longestMessage.getContent().length() + " characters):");
            System.out.println("Sender: " + longestMessage.getSender());
            System.out.println("Recipient: " + longestMessage.getRecipient());
            System.out.println("Content: " + longestMessage.getContent());
        }
    }

    private static void searchMessageByID(Scanner scanner) {
        System.out.println("\n--- Search Message by ID ---");
        System.out.print("Enter Message ID to search: ");
        String searchID = scanner.nextLine();

        Message foundMessage = null;
        for (Message msg : sentMessages) {
            if (msg.getMessageID().equals(searchID)) {
                foundMessage = msg;
                break;
            }
        }
        if (foundMessage == null) {
            for (Message msg : storedMessages) {
                if (msg.getMessageID().equals(searchID)) {
                    foundMessage = msg;
                    break;
                }
            }
        }
        if (foundMessage == null) {
            for (Message msg : disregardedMessages) {
                if (msg.getMessageID().equals(searchID)) {
                    foundMessage = msg;
                    break;
                }
            }
        }

        if (foundMessage != null) {
            System.out.println("Message Found:");
            System.out.println("MessageID: " + foundMessage.getMessageID());
            System.out.println("Sender: " + foundMessage.getSender());
            System.out.println("Recipient: " + foundMessage.getRecipient());
            System.out.println("Content: " + foundMessage.getContent());
            System.out.println("Hash: " + foundMessage.getHash());
            System.out.println("Message Number: " + foundMessage.getMessageNumber());
        } else {
            System.out.println("Message with ID '" + searchID + "' not found.");
        }
    }

    private static void searchMessagesByRecipient(Scanner scanner) {
        System.out.println("\n--- Search Messages by Recipient ---");
        System.out.print("Enter recipient's cell number to search: ");
        String searchRecipient = scanner.nextLine();

        List<Message> foundMessages = new ArrayList<>();
        for (Message msg : sentMessages) {
            if (msg.getRecipient().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }
        for (Message msg : storedMessages) {
             if (msg.getRecipient().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }
        for (Message msg : disregardedMessages) {
             if (msg.getRecipient().equals(searchRecipient)) {
                foundMessages.add(msg);
            }
        }

        if (foundMessages.isEmpty()) {
            System.out.println("No messages found for recipient '" + searchRecipient + "'.");
        } else {
            System.out.println("Messages found for recipient '" + searchRecipient + "':");
            for (Message msg : foundMessages) {
                System.out.println("  - MessageID: " + msg.getMessageID());
                System.out.println("    Sender: " + msg.getSender());
                System.out.println("    Content: " + msg.getContent());
                System.out.println("    Hash: " + msg.getHash());
            }
        }
    }

    private static void deleteMessageByHash(Scanner scanner) {
        System.out.println("\n--- Delete Message by Hash ---");
        System.out.print("Enter Message Hash to delete: ");
        String searchHash = scanner.nextLine();

        boolean foundAndDeleted = false;

        if (removeMessageFromList(sentMessages, searchHash)) {
            System.out.println("Deleted from sent messages.");
            foundAndDeleted = true;
        } else if (removeMessageFromList(storedMessages, searchHash)) {
            String messageIdToDelete = getMessageIdByHash(storedMessages, searchHash);
            if (messageIdToDelete != null) {
                String filename = "stored_message_" + messageIdToDelete + ".json";
                File jsonFile = new File(filename);
                if (jsonFile.exists() && jsonFile.delete()) {
                    System.out.println("Deleted JSON file: " + filename);
                } else {
                    System.out.println("Could not delete JSON file: " + filename);
                }
            }
            System.out.println("Deleted from stored messages.");
            foundAndDeleted = true;
        } else if (removeMessageFromList(disregardedMessages, searchHash)) {
            System.out.println("Deleted from disregarded messages.");
            foundAndDeleted = true;
        }

        allMessageHashes.remove(searchHash);

        if (foundAndDeleted) {
            System.out.println("Message with hash '" + searchHash + "' deleted successfully.");
        } else {
            System.out.println("Message with hash '" + searchHash + "' not found in any list.");
        }
    }

    private static boolean removeMessageFromList(List<Message> messageList, String hash) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).getHash().equals(hash)) {
                messageList.remove(i);
                return true;
            }
        }
        return false;
    }

    private static String getMessageIdByHash(List<Message> messageList, String hash) {
        for (Message msg : messageList) {
            if (msg.getHash().equals(hash)) {
                return msg.getMessageID();
            }
        }
        return null;
    }

    private static void displaySentMessagesReport() {
        System.out.println("\n--- Report: Full Details of All Sent Messages ---");
        if (sentMessages.isEmpty()) {
            System.out.println("No messages have been sent yet to generate a report.");
            return;
        }

        for (int i = 0; i < sentMessages.size(); i++) {
            Message msg = sentMessages.get(i);
            System.out.println("--- Message " + (i + 1) + " ---");
            System.out.println("MessageID: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getHash());
            System.out.println("Sender: " + msg.getSender());
            System.out.println("Recipient: " + msg.getRecipient());
            System.out.println("Content: " + msg.getContent());
            System.out.println("Message Number (session): " + msg.getMessageNumber());
            System.out.println("--------------------");
        }
    }

    private static void displayDisregardedMessages() {
        System.out.println("\n--- Disregarded Messages ---");
        if (disregardedMessages.isEmpty()) {
            System.out.println("No messages have been disregarded yet.");
            return;
        }
        for (Message msg : disregardedMessages) {
            System.out.println("MessageID: " + msg.getMessageID() + ", Sender: " + msg.getSender() + ", Recipient: " + msg.getRecipient() + ", Content: " + msg.getContent() + ", Hash: " + msg.getHash());
        }
    }

    private static void displayAllMessageHashes() {
        System.out.println("\n--- All Message Hashes (Sent, Stored, Disregarded) ---");
        if (allMessageHashes.isEmpty()) {
            System.out.println("No message hashes have been generated yet.");
            return;
        }
        for (String hash : allMessageHashes) {
            System.out.println("- " + hash);
        }
    }

    private static void displayAllMessageIDs() {
        System.out.println("\n--- All Message IDs (Sent, Stored, Disregarded) ---");
        if (allMessageIDs.isEmpty()) {
            System.out.println("No message IDs have been generated yet.");
            return;
        }
        for (String id : allMessageIDs) {
            System.out.println("- " + id);
        }
    }

    private static void loadStoredMessagesFromJSON() {
        System.out.println("\n--- Loading Stored Messages from JSON files ---");
        storedMessages.clear();
        File currentDir = new File(".");

        File[] files = currentDir.listFiles((dir, name) -> name.startsWith("stored_message_") && name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println("No stored message JSON files found in the current directory.");
            return;
        }

        for (File file : files) {
            try (Scanner fileScanner = new Scanner(file)) {
                StringBuilder jsonContent = new StringBuilder();
                while (fileScanner.hasNextLine()) {
                    jsonContent.append(fileScanner.nextLine());
                }
                Message loadedMessage = parseJsonToMessage(jsonContent.toString());
                if (loadedMessage != null) {
                    storedMessages.add(loadedMessage);
                    if (!allMessageIDs.contains(loadedMessage.getMessageID())) {
                        allMessageIDs.add(loadedMessage.getMessageID());
                    }
                    if (!allMessageHashes.contains(loadedMessage.getHash())) {
                        allMessageHashes.add(loadedMessage.getHash());
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + file.getName());
            } catch (IOException e) { // Catches IO errors
                System.err.println("Error reading file " + file.getName() + ": " + e.getMessage());
            }
        }
        System.out.println("Loaded " + storedMessages.size() + " messages from JSON files.");
    }

    private static Message parseJsonToMessage(String jsonString) {
        String messageID = extractJsonValue(jsonString, "messageID");
        String sender = extractJsonValue(jsonString, "sender");
        String recipient = extractJsonValue(jsonString, "recipient");
        String content = extractJsonValue(jsonString, "content");
        int messageNumber = 0;
        try {
            messageNumber = Integer.parseInt(extractJsonValue(jsonString, "messageNumber"));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing messageNumber from JSON: " + jsonString);
            return null;
        }
        String hash = extractJsonValue(jsonString, "hash");

        if (messageID != null && sender != null && recipient != null && content != null && hash != null) {
           
            return new Message(sender, recipient, content, messageNumber);
        }
        return null;
    }

    private static String extractJsonValue(String jsonString, String key) {
        String searchKey = "\"" + key + "\": \"";
        int startIndex = jsonString.indexOf(searchKey);
        if (startIndex == -1) {
           
            if (key.equals("messageNumber")) {
                searchKey = "\"" + key + "\": ";
                startIndex = jsonString.indexOf(searchKey);
                if (startIndex == -1) return null;
                startIndex += searchKey.length();
                int endIndex = jsonString.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = jsonString.indexOf("}", startIndex); 
                return jsonString.substring(startIndex, endIndex).trim();
            }
            return null; 
        }
        startIndex += searchKey.length();
        int endIndex = jsonString.indexOf("\"", startIndex);
        if (endIndex == -1) return null; 
        return jsonString.substring(startIndex, endIndex);
    }

    private static void displayLoadedStoredMessages() {
        System.out.println("\n--- Stored Messages (Loaded from JSON) ---");
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages to display after loading.");
            return;
        }
        for (int i = 0; i < storedMessages.size(); i++) {
            Message msg = storedMessages.get(i);
            System.out.println("--- Stored Message " + (i + 1) + " ---");
            System.out.println("MessageID: " + msg.getMessageID());
            System.out.println("Message Hash: " + msg.getHash());
            System.out.println("Sender: " + msg.getSender());
            System.out.println("Recipient: " + msg.getRecipient());
            System.out.println("Content: " + msg.getContent());
            System.out.println("Message Number (session): " + msg.getMessageNumber());
            System.out.println("--------------------");
        }
    }
}