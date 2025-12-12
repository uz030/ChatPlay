package catchmind;

import game.GameInstance;
import game.MessageBroadcaster;
import java.util.*;

public class CatchMindGame implements GameInstance {

    private int roomId;
    private List<String> players;
    private String currentDrawer;
    private String currentWord;
    private boolean isRunning = false;
    private MessageBroadcaster broadcaster;
    private int currentRound = 0;
    private Map<String, Integer> scores = new HashMap<>();
    private Timer roundTimer;
    private static final int ROUND_TIME = 180; // 3분
    private static final int TOTAL_ROUNDS = 3;
    private boolean roundInProgress = false;

    private List<String> wordList = Arrays.asList(
        "사과", "컴퓨터", "자동차", "비행기", "고양이",
        "햄버거", "책", "축구공", "피자", "텔레비전",
        "커피", "강아지", "선풍기", "의자", "냉장고",
        "카레", "잠실", "김경호", "스타벅스", "퇴학",
        "원빈", "아이패드", "노래방", "크리스마스", "종강",
        "닭발", "물먹는하마", "오리발", "바위섬", "가로수"
    );

    public CatchMindGame(int roomId, List<String> players) {
        this.roomId = roomId;
        this.players = new ArrayList<>(players);
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
        if (players.isEmpty()) return;

        isRunning = true;
        currentRound = 1;

        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 캐치마인드 게임을 시작합니다!");
        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 총 " + (players.size() * TOTAL_ROUNDS) + "라운드 진행됩니다.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                startNewRound();
            }
        }, 1000);
    }

    private void startNewRound() {
        if (!isRunning) return;

        roundInProgress = true;

        int drawerIndex = (currentRound - 1) % players.size();
        currentDrawer = players.get(drawerIndex);
        currentWord = wordList.get(new Random().nextInt(wordList.size()));

        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System Round " + currentRound + "/" +
            (players.size() * TOTAL_ROUNDS) + " 시작!");
        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 출제자: " + currentDrawer + " (제한시간: 3분)");

        broadcaster.broadcastToRoom(roomId, "/cleardraw " + roomId);
        broadcaster.sendToUser(currentDrawer, "/catchmind_word " + roomId + " " + currentWord);
        broadcaster.broadcastToRoom(roomId, "/catchmind_drawer " + roomId + " " + currentDrawer);
        broadcaster.broadcastToRoom(roomId, "/start_timer " + roomId + " " + ROUND_TIME);

        startTimeoutTimer();
    }

    private void startTimeoutTimer() {
        if (roundTimer != null) roundTimer.cancel();

        roundTimer = new Timer();
        roundTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (roundInProgress && isRunning) {
                    onTimeOut();
                }
            }
        }, ROUND_TIME * 1000);
    }

    private void onTimeOut() {
        roundInProgress = false;

        int timeoutScore = 5;
        scores.put(currentDrawer, scores.get(currentDrawer) + timeoutScore);

        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 시간 초과! 정답은 \"" + currentWord + "\" 였습니다.");
        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 출제자 " + currentDrawer + " +" + timeoutScore + "점");
        broadcaster.broadcastToRoom(roomId,
            "/score_update " + roomId + " " + currentDrawer + " " + scores.get(currentDrawer));
        broadcaster.broadcastToRoom(roomId,
            "/reveal_answer " + roomId + " " + currentWord);

        currentRound++;
        scheduleNextRoundOrEnd();
    }

    @Override
    public void handleMessage(String userName, String msg) {
        if (!isRunning || !roundInProgress) return;

        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " " + userName + " " + msg);

        if (!userName.equals(currentDrawer) &&
            msg.trim().replaceAll("\\s+", "")
               .equalsIgnoreCase(currentWord.replaceAll("\\s+", ""))) {

            roundInProgress = false;
            if (roundTimer != null) roundTimer.cancel();

            int correctScore = 10;
            scores.put(userName, scores.get(userName) + correctScore);

            broadcaster.broadcastToRoom(roomId,
                "/game_chat " + roomId + " System " + userName + "님이 정답을 맞췄습니다! +" + correctScore + "점");
            broadcaster.broadcastToRoom(roomId,
                "/correct_answer " + roomId + " " + userName + " " + currentWord + " " + correctScore);

            currentRound++;
            scheduleNextRoundOrEnd();
        }
    }

    private void scheduleNextRoundOrEnd() {
        if (currentRound > players.size() * TOTAL_ROUNDS) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    endGame();
                }
            }, 3000);
        } else {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startNewRound();
                }
            }, 3000);
        }
    }

    private void endGame() {
        isRunning = false;
        roundInProgress = false;
        if (roundTimer != null) roundTimer.cancel();

        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(scores.entrySet());
        ranking.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System 🏆 게임 종료!");
        broadcaster.broadcastToRoom(roomId,
            "/game_chat " + roomId + " System === 최종 순위 ===");

        for (int i = 0; i < ranking.size(); i++) {
            Map.Entry<String, Integer> e = ranking.get(i);
            broadcaster.broadcastToRoom(roomId,
                "/game_chat " + roomId + " System " + (i + 1) + "위: " +
                e.getKey() + " (" + e.getValue() + "점)");
        }

        broadcaster.broadcastToRoom(roomId,
            "/game_ended " + roomId + " " + ranking.get(0).getKey());
    }

    @Override
    public void end() {
        isRunning = false;
        roundInProgress = false;
        if (roundTimer != null) roundTimer.cancel();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getRoomId() {
        return roomId;
    }
}
