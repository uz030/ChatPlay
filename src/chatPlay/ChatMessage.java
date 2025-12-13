package chatPlay;

import javax.swing.ImageIcon;

/**
 * 채팅 메시지 데이터 모델 클래스
 * 텍스트, 이미지, 파일 등 다양한 타입의 메시지를 관리
 */
public class ChatMessage {
    // ========== 기본 메시지 정보 ==========
    private String sender;          // 발신자 이름
    private String content;         // 메시지 내용
    private boolean isMine;         // 내 메시지 여부
    private ImageIcon senderIcon;   // 발신자 프로필 아이콘
    
    /**
     * 메시지 타입 열거형
     */
    public enum MessageType {
        TEXT,   // 일반 텍스트 메시지
        IMAGE,  // 이미지 메시지
        FILE    // 파일 메시지
    }

    // ========== 메시지 타입 및 파일 정보 ==========
    private MessageType type = MessageType.TEXT;  // 메시지 타입 (기본값: 텍스트)
    private String fileName;                      // 파일명 (이미지/파일 메시지용)
    private byte[] fileData;                      // 파일 데이터 (파일 메시지용)
    private ImageIcon imageIcon;                  // 이미지 아이콘 (이미지 메시지용)

    /**
     * 채팅 메시지 생성자
     * @param sender 발신자 이름
     * @param content 메시지 내용
     * @param isMine 내 메시지 여부
     * @param senderIcon 발신자 프로필 아이콘
     */
    public ChatMessage(String sender, String content, boolean isMine, ImageIcon senderIcon) {
        this.sender = sender;
        this.content = content;
        this.isMine = isMine;
        this.senderIcon = senderIcon;
    }

    // ========== Getter 메서드 ==========
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public boolean isMine() { return isMine; }
    public ImageIcon getSenderIcon() { return senderIcon; }
    public MessageType getType() { return type; }
    public String getFileName() { return fileName; }
    public byte[] getFileData() { return fileData; }
    public ImageIcon getImageIcon() { return imageIcon; }

    // ========== Setter 메서드 ==========
    public void setSenderIcon(ImageIcon icon) { this.senderIcon = icon; }
    public void setType(MessageType type) { this.type = type; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFileData(byte[] data) { this.fileData = data; }
    public void setImageIcon(ImageIcon icon) { this.imageIcon = icon; }

    // ========== 유틸리티 메서드 ==========
    /**
     * 챗봇 메시지인지 확인
     * @return 챗봇 메시지이면 true
     */
    public boolean isBotMessage() {
        return sender.equals("ChatBot") && content.startsWith("BOT_MENU:");
    }
    
    /**
     * 텍스트 메시지 생성 팩토리 메서드
     * @param sender 발신자 이름
     * @param content 메시지 내용
     * @param isMine 내 메시지 여부
     * @return 생성된 텍스트 메시지
     */
    public static ChatMessage text(String sender, String content, boolean isMine) {
        ChatMessage m = new ChatMessage(sender, content, isMine, null);
        m.type = MessageType.TEXT;
        return m;
    }
}