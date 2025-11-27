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

    // 이미지 메시지용
    public static ChatMessage image(String sender, String fileName, byte[] data, boolean isMine) {
        ChatMessage m = new ChatMessage(sender, "[이미지]", isMine, null);
        m.type = MessageType.IMAGE;
        m.fileName = fileName;
        m.fileData = data;
        return m;
    }

    // 일반 파일 메시지용
    public static ChatMessage file(String sender, String fileName, byte[] data, boolean isMine) {
        ChatMessage m = new ChatMessage(sender, "[파일] " + fileName, isMine, null);
        m.type = MessageType.FILE;
        m.fileName = fileName;
        m.fileData = data;
        return m;
    }
    
    public void setSenderIcon(ImageIcon icon) { this.senderIcon = icon; }

    public MessageType getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}