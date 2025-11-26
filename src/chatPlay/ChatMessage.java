package chatPlay;

import javax.swing.ImageIcon;

public class ChatMessage {
    private String sender;
    private String content;
    private boolean isMine;
    private ImageIcon senderIcon; 

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
}