package chatPlay;

import javax.swing.ImageIcon;

public class ChatMessage {
    private String sender;
    private String content;
    private boolean isMine;
    private ImageIcon senderIcon; 
    
    public enum MessageType {
        TEXT,
        IMAGE,
        FILE
    }

    private MessageType type = MessageType.TEXT;
    
    private String fileName; 
    private byte[] fileData;

    public ChatMessage(String sender, String content, boolean isMine, ImageIcon senderIcon) {
        this.sender = sender;
        this.content = content;
        this.isMine = isMine;
        this.senderIcon = senderIcon;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isMine() { return isMine; }
    
    public ImageIcon getSenderIcon() { return senderIcon; }

    public boolean isBotMessage() {
        return sender.equals("ChatBot") && content.startsWith("BOT_MENU:");
    }
    
    public static ChatMessage text(String sender, String content, boolean isMine) {
        ChatMessage m = new ChatMessage(sender, content, isMine, null);
        m.type = MessageType.TEXT;
        return m;
    }
    
    public void setSenderIcon(ImageIcon icon) { this.senderIcon = icon; }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileData(byte[] data) {
        this.fileData = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
    
    private ImageIcon imageIcon;
    public void setImageIcon(ImageIcon icon) { this.imageIcon = icon; }
    public ImageIcon getImageIcon() { return imageIcon; }

}