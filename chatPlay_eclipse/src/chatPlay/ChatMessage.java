package chatPlay;

public class ChatMessage {
    private String sender;
    private String content;
    private boolean isMine;

    public ChatMessage(String sender, String content, boolean isMine) {
        this.sender = sender;
        this.content = content;
        this.isMine = isMine;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isMine() { return isMine; }

    // 챗봇 메시지 여부 확인
    public boolean isBotMessage() {
        return sender.equals("ChatBot") && content.startsWith("BOT_MENU:");
    }
}