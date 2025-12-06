package game;

// 모든 게임이 구현해야 하는 인터페이스
 
public interface GameInstance {
    
    // 게임 시작
    void start();
    
    //게임 메시지 처리 
    void handleMessage(String userName, String msg);
    
    // 게임 종료 
    void end();
    
    // 게임 상태 확인 
    boolean isRunning();
    
    // 방 ID 반환 
    int getRoomId();
    
    // 메시지 브로드캐스터 설정 
    void setMessageBroadcaster(MessageBroadcaster broadcaster);
}