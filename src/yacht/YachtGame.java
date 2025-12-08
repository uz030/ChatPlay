package yacht;

import game.GameInstance;
import game.MessageBroadcaster;
import java.util.*;

public class YachtGame implements GameInstance {

    private int roomId;
    private List<YachtPlayer> players = new ArrayList<>();
    private MessageBroadcaster broadcaster;
    private boolean isRunning;

    private int currentPlayerIdx = 0;
    private int rollsLeft = 3;
    private int[] dice = {1, 1, 1, 1, 1};
    private int round = 1;
    private final int MAX_ROUND = 12;

    public YachtGame(int roomId, List<String> playerNames) {
        this.roomId = roomId;
        for (String name : playerNames) {
            players.add(new YachtPlayer(name));
        }
    }

    @Override
    public void start() {
        isRunning = true;
        // 참가자 이름 리스트 생성
        StringBuilder names = new StringBuilder();
        for (YachtPlayer p : players) {
            if (names.length() > 0) names.append(",");
            names.append(p.getName());
        }
        
        // 클라이언트에 게임 시작 신호 및 참여자 목록 전송
        broadcaster.broadcastToRoom(roomId, "/yacht_start " + roomId + " " + names.toString());
        
        // 첫 턴 데이터 전송
        broadcastState();
        broadcaster.broadcastToRoom(roomId, "/roommsg " + roomId + " [System] 요트다이스 게임이 시작되었습니다!");
    }

    @Override
    public void handleMessage(String userName, String msg) {
        
        if (!players.get(currentPlayerIdx).getName().equals(userName)) {
            return; // 현재 턴인 사람만 조작 가능
        }

        if (msg.startsWith("roll ")) {
            if (rollsLeft > 0) {
                String keepMask = msg.split(" ")[1];
                rollDice(keepMask);
                rollsLeft--;
                broadcastState();
            }
        } else if (msg.startsWith("select ")) {
            // 한 번 이상 굴려야 선택 가능 (rollsLeft < 3)
            if (rollsLeft == 3) {
                return; // 아직 주사위를 굴리지 않았으면 무시
            }
            
            int categoryIdx = Integer.parseInt(msg.split(" ")[1]);
            YachtPlayer currPlayer = players.get(currentPlayerIdx);
            
            if (!currPlayer.isFilled(categoryIdx)) {
                int score = calculateScore(categoryIdx, dice);
                currPlayer.setScore(categoryIdx, score);
                
                nextTurn();
            }
        }
    }

    private void rollDice(String keepMask) {
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            if (keepMask.length() > i && keepMask.charAt(i) == '0') {
                dice[i] = r.nextInt(6) + 1;
            }
        }
    }

    private void nextTurn() {
        rollsLeft = 3;
        Arrays.fill(dice, 1);
        
        currentPlayerIdx++;
        if (currentPlayerIdx >= players.size()) {
            currentPlayerIdx = 0;
            round++;
        }

        if (round > MAX_ROUND) {
            endGame();
        } else {
            broadcastState();
        }
    }
    
    // 점수 계산 로직
    private int calculateScore(int cat, int[] d) {
        int[] counts = new int[7];
        int sum = 0;
        for (int v : d) { counts[v]++; sum += v; }
        
        switch(cat) {
            case 0: return counts[1] * 1; // Ones
            case 1: return counts[2] * 2; // Twos
            case 2: return counts[3] * 3; // Threes
            case 3: return counts[4] * 4; // Fours
            case 4: return counts[5] * 5; // Fives
            case 5: return counts[6] * 6; // Sixes
            case 6: return sum; // Choice
            case 7: // 4 of a Kind
                for(int i=1; i<=6; i++) if(counts[i] >= 4) return sum;
                return 0;
            case 8: // Full House
                boolean has3 = false, has2 = false;
                for(int i=1; i<=6; i++) {
                    if(counts[i] == 3) has3 = true;
                    if(counts[i] == 2) has2 = true;
                    if(counts[i] == 5) { has3 = true; has2 = true; }
                }
                return (has3 && has2) ? sum : 0;
            case 9: // Small Straight (4연속)
                if(isStraight(counts, 4)) return 15;
                return 0;
            case 10: // Large Straight (5연속)
                if(isStraight(counts, 5)) return 30;
                return 0;
            case 11: // Yacht
                for(int i=1; i<=6; i++) if(counts[i] == 5) return 50;
                return 0;
        }
        return 0;
    }

    private boolean isStraight(int[] counts, int len) {
        int consecutive = 0;
        for (int i=1; i<=6; i++) {
            if (counts[i] > 0) consecutive++;
            else consecutive = 0;
            if (consecutive >= len) return true;
        }
        return false;
    }

    private void broadcastState() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentPlayerIdx).append("/");
        sb.append(rollsLeft).append("/");
        for(int i=0; i<5; i++) sb.append(dice[i]).append(i==4?"":",");
        sb.append("/");
        
        // 각 플레이어 점수 상태
        for (YachtPlayer p : players) {
            for (int i=0; i<14; i++) { // 12 categories + bonus + total
                sb.append(p.getScore(i)).append(",");
            }
            sb.append(p.isFilled(0) ? "1" : "0"); 
            sb.append(";"); // 플레이어 구분
        }
        
        broadcaster.broadcastToRoom(roomId, "/yacht_update " + roomId + " " + sb.toString());
    }

    private void endGame() {
        isRunning = false;
        
        // 우승자 계산
        YachtPlayer winner = players.stream().max(Comparator.comparingInt(YachtPlayer::getTotalScore)).orElse(null);
        String msg = "🏆 요트다이스 종료! 우승자: " + (winner != null ? winner.getName() : "없음");
        
        broadcaster.broadcastToRoom(roomId, "/game_chat " + roomId + " System " + msg);
        broadcaster.broadcastToRoom(roomId, "/game_ended " + roomId);
    }

    @Override public void end() { isRunning = false; }
    @Override public boolean isRunning() { return isRunning; }
    @Override public int getRoomId() { return roomId; }
    @Override public void setMessageBroadcaster(MessageBroadcaster broadcaster) { this.broadcaster = broadcaster; }
}