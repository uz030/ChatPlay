package game;

import java.util.*;
import catchmind.CatchMindGame;
import yacht.YachtGame;

/**
 * 게임 관리자 클래스
 * 게임 인스턴스 생성, 참여자 관리, 게임 상태 관리를 담당
 */
public class GameManager {
    // ========== 게임 인스턴스 및 타입 관리 ==========
    private Map<Integer, GameInstance> games = new HashMap<>();           // 방 ID -> 게임 인스턴스
    private Map<Integer, GameType> gameTypes = new HashMap<>();           // 방 ID -> 게임 타입
    
    // ========== 참여자 관리 ==========
    private Map<Integer, Set<String>> waitingParticipants = new HashMap<>();  // 게임 시작 전 대기 중인 참여자
    private Map<Integer, Set<String>> activeParticipants = new HashMap<>();  // 게임 진행 중 활성 참여자

    // ========== 참여자 관리 메서드 ==========
    /**
     * 게임 참여자 추가
     * @param roomId 방 ID
     * @param userName 사용자명
     * @param gameType 게임 타입
     * @return 추가 성공 여부 (이미 참여 중이면 false)
     */
    public synchronized boolean addParticipant(int roomId, String userName, GameType gameType) {
        // 대기 참여자 목록이 없으면 생성
        if (!waitingParticipants.containsKey(roomId)) {
            waitingParticipants.put(roomId, new HashSet<>());
        }
        // 참여자 추가 (중복이면 false 반환)
        return waitingParticipants.get(roomId).add(userName);
    }
    
    /**
     * 참여자 수 조회
     * @param roomId 방 ID
     * @return 참여자 수
     */
    public synchronized int getParticipantCount(int roomId) {
        Set<String> participants = waitingParticipants.get(roomId);
        return participants != null ? participants.size() : 0;
    }
    
    /**
     * 게임 시작 가능 여부 확인
     * @param roomId 방 ID
     * @param gameType 게임 타입
     * @return 최소 인원 이상이면 true
     */
    public synchronized boolean canStartGame(int roomId, GameType gameType) {
        int minPlayers = getMinPlayers(gameType);
        return getParticipantCount(roomId) >= minPlayers;
    }
    
    /**
     * 게임별 최소 인원 조회
     * @param gameType 게임 타입
     * @return 최소 인원 수
     */
    private int getMinPlayers(GameType gameType) {
        switch (gameType) {
            case CATCH_MIND: return 2;
            case YACHT: return 2;
            default: return 2;
        }
    }

    // ========== 게임 시작 및 종료 메서드 ==========
    /**
     * 게임 시작
     * @param roomId 방 ID
     * @param type 게임 타입
     * @param broadcaster 메시지 브로드캐스터
     * @return 생성된 게임 인스턴스 (실패 시 null)
     */
    public synchronized GameInstance startGame(
        int roomId, 
        GameType type,
        MessageBroadcaster broadcaster
    ) {
        // 이미 게임이 실행 중이면 기존 게임 반환
        if (games.containsKey(roomId)) {
            return games.get(roomId);
        }
        
        // 대기 중인 참여자 가져오기
        Set<String> participants = waitingParticipants.get(roomId);
        if (participants == null || participants.isEmpty()) {
            return null;
        }
        
        List<String> participantList = new ArrayList<>(participants);

        // 게임 타입에 따라 인스턴스 생성
        GameInstance game = null;
        switch (type) {
            case CATCH_MIND:
                game = new CatchMindGame(roomId, participantList);
                break;
            case YACHT:  
                game = new YachtGame(roomId, participantList);
                break;
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }

        if (game != null) {
            // 게임 인스턴스 설정 및 저장
            game.setMessageBroadcaster(broadcaster);
            games.put(roomId, game);
            gameTypes.put(roomId, type);
            
            // 대기 참여자를 활성 참여자로 이동
            activeParticipants.put(roomId, new HashSet<>(participants));
            waitingParticipants.remove(roomId);
            
            // 게임 시작
            game.start();
        }

        return game;
    }

    /**
     * 게임 메시지 처리
     * @param roomId 방 ID
     * @param userName 사용자명
     * @param msg 메시지 내용
     */
    public synchronized void handleGameMessage(int roomId, String userName, String msg) {
        GameInstance game = games.get(roomId);
        if (game != null) {
            game.handleMessage(userName, msg);
        }
    }

    /**
     * 게임 참여자 제거 (방 나가기, 연결 종료 시)
     * @param roomId 방 ID
     * @param userName 사용자명
     * @return 게임이 종료되었으면 true
     */
    public synchronized boolean removeParticipant(int roomId, String userName) {
        boolean removed = false;
        
        // 1단계: 대기 중인 참여자에서 제거
        Set<String> waiting = waitingParticipants.get(roomId);
        if (waiting != null) {
            removed = waiting.remove(userName);
            // 대기 목록이 비어있으면 제거
            if (waiting.isEmpty()) {
                waitingParticipants.remove(roomId);
            }
        }
        
        // 2단계: 활성 참여자에서 제거
        Set<String> active = activeParticipants.get(roomId);
        if (active != null) {
            boolean activeRemoved = active.remove(userName);
            if (activeRemoved) {
                removed = true;
                // 활성 참여자가 모두 나가면 게임 종료
                if (active.isEmpty()) {
                    endGame(roomId);
                    return true; // 게임 종료됨
                }
            }
        }
        
        return removed;
    }
    
    /**
     * 게임 종료
     * @param roomId 방 ID
     */
    public synchronized void endGame(int roomId) {
        // 게임 인스턴스 종료 및 제거
        GameInstance game = games.remove(roomId);
        if (game != null) {
            game.end();
        }
        
        // 관련 데이터 정리
        gameTypes.remove(roomId);
        activeParticipants.remove(roomId);
        waitingParticipants.remove(roomId);
    }

    // ========== 조회 메서드 ==========
    /**
     * 게임 인스턴스 조회
     * @param roomId 방 ID
     * @return 게임 인스턴스 (없으면 null)
     */
    public synchronized GameInstance getGame(int roomId) {
        return games.get(roomId);
    }

    /**
     * 게임 실행 여부 확인
     * @param roomId 방 ID
     * @return 게임이 실행 중이면 true
     */
    public synchronized boolean isGameRunning(int roomId) {
        return games.containsKey(roomId);
    }
    
    /**
     * 활성 게임 참여자 조회 (그림 데이터 전송용)
     * @param roomId 방 ID
     * @return 활성 참여자 목록 (없으면 null)
     */
    public synchronized Set<String> getActiveParticipants(int roomId) {
        return activeParticipants.get(roomId);
    }
    
    /**
     * 대기 중인 참여자 조회
     * @param roomId 방 ID
     * @return 대기 참여자 목록 (없으면 null)
     */
    public synchronized Set<String> getWaitingParticipants(int roomId) {
        return waitingParticipants.get(roomId);
    }
    
    /**
     * 게임 타입 조회
     * @param roomId 방 ID
     * @return 게임 타입 (없으면 null)
     */
    public synchronized GameType getGameType(int roomId) {
        return gameTypes.get(roomId);
    }
    
    /**
     * 캐치마인드 게임인지 확인
     * @param roomId 방 ID
     * @return 캐치마인드 게임이면 true
     */
    public synchronized boolean isCatchMindGame(int roomId) {
        return gameTypes.get(roomId) == GameType.CATCH_MIND;
    }
}