package game;

/**
 * 메시지 브로드캐스터 인터페이스
 * 게임에서 채팅 서버로 메시지를 전송하기 위한 콜백 인터페이스
 */
public interface MessageBroadcaster {
    /**
     * 특정 방 전체에 메시지 전송
     * @param roomId 방 ID
     * @param message 전송할 메시지
     */
    void broadcastToRoom(int roomId, String message);
    
    /**
     * 특정 유저에게만 메시지 전송
     * @param userName 사용자명
     * @param message 전송할 메시지
     */
    void sendToUser(String userName, String message);
}