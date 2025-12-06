package catchmind;

import game.GameInstance;
import game.MessageBroadcaster;
import java.util.*;

/**
 * 캐치마인드 게임 구현
 */
public class CatchMindGame implements GameInstance {
    
    private int roomId;
    private List<String> players;
    private String currentDrawer;
    private String currentWord;
    private boolean isRunning = false;
    private MessageBroadcaster broadcaster;
    private int currentRound = 0;
    private Map<String, Integer> scores = new HashMap<>();
    
    // 게임 단어 목록
    private List<String> wordList = Arrays.asList(
        "사과", "컴퓨터", "자동차", "비행기", "고양이", 
        "햄버거", "책", "축구공", "피자", "텔레비전",
        "커피", "강아지", "선풍기", "의자", "냉장고"
    );
    
    public CatchMindGame(int roomId, List<String> players) {
        this.roomId = roomId;
        this.players = new ArrayList<>(players);
        
        // 점수 초기화
        for (String player : players) {
            scores.put(player, 0);
        }
    }
    
    @Override
    public void setMessageBroadcaster(MessageBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }
    
    @Override
    public void start() {
        if (players.isEmpty()) {
            return;
        }
        
        isRunning = true;
        currentRound = 1;
        startNewRound();
    }
    
    /**
     * 새 라운드 시작
     */
    private void startNewRound() {
        // 출제자 선정 (라운드마다 순환)
        int drawerIndex = (currentRound - 1) % players.size();
        currentDrawer = players.get(drawerIndex);
        
        // 랜덤 단어 선정
        currentWord = wordList.get(new Random().nextInt(wordList.size()));
        
        // 출제자에게만 단어 전송
        broadcaster.sendToUser(currentDrawer, "/catchmind_word " + roomId + " " + currentWord);
        
        // 게임 참여자에게 라운드 시작 알림
        broadcaster.broadcastToRoom(roomId, 
            "/game_chat " + roomId + " System 🎨 Round " + currentRound + " 시작! 출제자: " + currentDrawer);
        
        // 그림 그리기 권한 설정
        broadcaster.broadcastToRoom(roomId, "/catchmind_drawer " + roomId + " " + currentDrawer);
    }
    
    @Override
    public void handleMessage(String userName, String msg) {
        if (!isRunning) return;
        
        // 출제자는 정답 체크 제외
        if (userName.equals(currentDrawer)) {
            // 출제자의 메시지도 게임 채팅에 표시 (게임 참여자에게만)
            broadcaster.broadcastToRoom(roomId, "/game_chat " + roomId + " " + userName + " " + msg);
            return;
        }
        
        // 모든 정답 시도를 게임 채팅에 표시 (게임 참여자에게만)
        broadcaster.broadcastToRoom(roomId, "/game_chat " + roomId + " " + userName + " " + msg);
        
        // 정답 체크
        if (msg.trim().equalsIgnoreCase(currentWord)) {
            
            // 점수 부여
            scores.put(userName, scores.get(userName) + 10);
            
            // 정답 알림 (게임 채팅 - 게임 참여자에게만)
            broadcaster.broadcastToRoom(roomId, 
                "/game_chat " + roomId + " System 🎉 " + userName + "님이 정답을 맞췄습니다!");
            
            // 정답 알림 (클라이언트 처리용 - 점수 업데이트)
            broadcaster.broadcastToRoom(roomId, 
                "/correct_answer " + roomId + " " + userName + " " + currentWord);
            
            // 다음 라운드로
            currentRound++;
            
            // 모든 플레이어가 한 번씩 출제했으면 게임 종료
            if (currentRound > players.size()) {
                endGame();
            } else {
                // 3초 후 다음 라운드 시작
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startNewRound();
                    }
                }, 3000);
            }
        }
    }
    
    /**
     * 게임 종료
     */
    private void endGame() {
        isRunning = false;
        
        // 최종 우승자 찾기
        String winner = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("없음");
        
        int winnerScore = scores.getOrDefault(winner, 0);
        
        // 게임 참여자에게만 종료 알림
        broadcaster.broadcastToRoom(roomId, 
            "/game_chat " + roomId + " System 🏆 게임 종료! 우승자: " + winner + " (" + winnerScore + "점)");
        
        // 게임 종료 신호
        broadcaster.broadcastToRoom(roomId, "/game_ended " + roomId);
    }
    
    @Override
    public void end() {
        isRunning = false;
        broadcaster.broadcastToRoom(roomId, "/game_chat " + roomId + " System 캐치마인드 게임이 강제 종료되었습니다.");
    }
    
    @Override
    public boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public int getRoomId() {
        return roomId;
    }
    
    /**
     * 현재 출제자 반환
     */
    public String getCurrentDrawer() {
        return currentDrawer;
    }
    
    /**
     * 현재 정답 반환 (디버깅용)
     */
    public String getCurrentWord() {
        return currentWord;
    }
}