package chatPlay;

import javax.swing.DefaultListModel;

/**
 * 채팅방 데이터 모델 클래스
 * 방 ID, 방 이름, 메시지 로그를 관리
 */
public class ChatRoomData {
    // ========== 채팅방 정보 ==========
    private int roomId;                                    // 방 고유 ID
    private String roomName;                               // 방 이름
    private DefaultListModel<ChatMessage> messageLog;     // 메시지 로그 (순서 보장)

    /**
     * 채팅방 데이터 생성자
     * @param roomId 방 ID
     * @param roomName 방 이름
     */
    public ChatRoomData(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.messageLog = new DefaultListModel<>();
    }

    // ========== Getter 메서드 ==========
    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public DefaultListModel<ChatMessage> getMessageLog() { return messageLog; }
    
    // ========== Object 메서드 오버라이드 ==========
    @Override
    public String toString() { return roomName; }
}