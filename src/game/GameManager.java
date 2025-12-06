package game;

import java.util.*;
import catchmind.CatchMindGame;

public class GameManager {

    private Map<Integer, GameInstance> games = new HashMap<>();
    
    // 게임별 대기 중인 참여자 (게임 시작 전)
    private Map<Integer, Set<String>> waitingParticipants = new HashMap<>();
    
    // 게임별 활성 참여자 (게임 진행 중)
    private Map<Integer, Set<String>> activeParticipants = new HashMap<>();

    // 게임 참여자 추가
     
    public synchronized boolean addParticipant(int roomId, String userName, GameType gameType) {
        if (!waitingParticipants.containsKey(roomId)) {
            waitingParticipants.put(roomId, new HashSet<>());
        }
        return waitingParticipants.get(roomId).add(userName);
    }
    
    // 참여자 수 조회
    
    public synchronized int getParticipantCount(int roomId) {
        Set<String> participants = waitingParticipants.get(roomId);
        return participants != null ? participants.size() : 0;
    }
    
    // 게임 시작 가능 여부 확인
   
    public synchronized boolean canStartGame(int roomId, GameType gameType) {
        int minPlayers = getMinPlayers(gameType);
        return getParticipantCount(roomId) >= minPlayers;
    }
    
    // 게임별 최소 인원
   
    private int getMinPlayers(GameType gameType) {
        switch (gameType) {
            case CATCH_MIND: return 2;
            case OX_QUIZ: return 1;
            case WORD_CHAIN: return 2;
            default: return 2;
        }
    }

    // 게임 시작
   
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

        GameInstance game = null;

        switch (type) {
            case CATCH_MIND:
                game = new CatchMindGame(roomId, participantList);
                break;
                
            case OX_QUIZ:
                // game = new OXQuizGame(roomId, participantList);
                break;
                
            case WORD_CHAIN:
                // game = new WordChainGame(roomId, participantList);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }

        if (game != null) {
            game.setMessageBroadcaster(broadcaster);
            games.put(roomId, game);
            
            // 활성 참여자로 이동
            activeParticipants.put(roomId, new HashSet<>(participants));
            waitingParticipants.remove(roomId);
            
            game.start();
        }

        return game;
    }

    // 게임 메시지 처리
     
    public synchronized void handleGameMessage(int roomId, String userName, String msg) {
        GameInstance game = games.get(roomId);
        if (game != null) {
            game.handleMessage(userName, msg);
        }
    }

    // 게임 종료
    public synchronized void endGame(int roomId) {
        GameInstance game = games.remove(roomId);
        if (game != null) {
            game.end();
        }
        
        // 참여자 목록 정리
        activeParticipants.remove(roomId);
        waitingParticipants.remove(roomId);
    }

    // 게임 인스턴스 조회
     
    public synchronized GameInstance getGame(int roomId) {
        return games.get(roomId);
    }

    //  게임 실행 여부
     
    public synchronized boolean isGameRunning(int roomId) {
        return games.containsKey(roomId);
    }
    
    //  활성 게임 참여자 조회 (그림 데이터 전송용)
   
    public synchronized Set<String> getActiveParticipants(int roomId) {
        return activeParticipants.get(roomId);
    }
    
    // 대기 중인 참여자 조회
     
    public synchronized Set<String> getWaitingParticipants(int roomId) {
        return waitingParticipants.get(roomId);
    }
}