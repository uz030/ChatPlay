package game;

/**
 * 게임 인스턴스 인터페이스
 * 모든 게임이 구현해야 하는 공통 기능을 정의
 */
public interface GameInstance {
    // ========== 게임 생명주기 메서드 ==========
    /**
     * 게임 시작
     */
    void start();
    
    /**
     * 게임 메시지 처리
     * @param userName 사용자명
     * @param msg 메시지 내용
     */
    void handleMessage(String userName, String msg);
    
    /**
     * 게임 종료
     */
    void end();
    
    // ========== 게임 상태 조회 메서드 ==========
    /**
     * 게임 실행 여부 확인
     * @return 게임이 실행 중이면 true
     */
    boolean isRunning();
    
    /**
     * 방 ID 반환
     * @return 방 ID
     */
    int getRoomId();
    
    // ========== 설정 메서드 ==========
    /**
     * 메시지 브로드캐스터 설정
     * @param broadcaster 메시지 브로드캐스터
     */
    void setMessageBroadcaster(MessageBroadcaster broadcaster);
}