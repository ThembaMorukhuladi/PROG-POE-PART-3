package com.mycompany.poe;

/**
 *
 * @author Themba
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Message {
    private String messageID;
    private String sender; 
    private String recipient;
    private String content;
    private int messageNumber;
    private String hash;

   
    public Message(String sender, String recipient, String content, int messageNumber) {
        this.messageID = generateMessageID();
        this.sender = sender; 
        this.recipient = recipient;
        this.content = content;
        this.messageNumber = messageNumber;
        this.hash = createMessageHash();
    }

    public String getMessageID() {
        return messageID;
    }


    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getHash() {
        return hash;
    }

    private String generateMessageID() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    public int checkRecipientCell() {
       
        if (recipient == null || !recipient.startsWith("+")) return 1;
        if (recipient.length() > 13) return 2;
        return 0;
    }

    private String createMessageHash() {
        if (messageID == null || content == null || content.isEmpty()) return "";
        String[] words = content.split("\\s+");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];
        return (messageID.substring(0, Math.min(2, messageID.length())) + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    public String validateMessageLength() {
        if (content.length() <= 250) {
            return "Message ready to send.";
        } else {
            return "Message exceeds 250 characters by " + (content.length() - 250) + ", please reduce size.";
        }
    }

    public void storeMessage(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("{");
            writer.println("  \"messageID\": \"" + escapeJsonString(this.messageID) + "\",");
            writer.println("  \"sender\": \"" + escapeJsonString(this.sender) + "\","); 
            writer.println("  \"recipient\": \"" + escapeJsonString(this.recipient) + "\",");
            writer.println("  \"content\": \"" + escapeJsonString(this.content) + "\",");
            writer.println("  \"messageNumber\": " + this.messageNumber + ",");
            writer.println("  \"hash\": \"" + escapeJsonString(this.hash) + "\"");
            writer.println("}");
            System.out.println("Message stored in " + filename);
        } catch (IOException e) {
            System.err.println("Error storing message to JSON file: " + e.getMessage());
        }
    }

    private String escapeJsonString(String str) {
        if (str == null) return null;
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"");
    }
}