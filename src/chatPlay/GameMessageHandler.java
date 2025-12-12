package chatPlay;

import catchmind.CatchMindFrame;
import yacht.YachtFrame;
import java.awt.Color;
import java.util.*;
import javax.swing.SwingUtilities;

/**
 * 게임 메시지 핸들러
 * 서버로부터 받은 게임 관련 메시지를 처리하여 게임 창에 반영
 */
public class GameMessageHandler {
    
    private ChatClientMain client;
    private Map<Integer, CatchMindFrame> gameFrames;
    
    /**
     * 게임 메시지 핸들러 생성자
     * @param client 클라이언트 메인 프레임
     * @param gameFrames 게임 창 관리 맵
     */
    public GameMessageHandler(ChatClientMain client, Map<Integer, CatchMindFrame> gameFrames) {
        this.client = client;
        this.gameFrames = gameFrames;
    }
    
    /**
     * 게임 관련 메시지 처리
     * @param msg 서버로부터 받은 메시지
     * @return 게임 메시지인 경우 true, 아니면 false
     */
    public boolean handleMessage(String msg) {
        
        // 게임 참가자 목록 (게임 창 생성)
        if (msg.startsWith("/game_participants ")) {
            String[] parts = msg.split(" ", 4);
            int roomId = Integer.parseInt(parts[1]);
            String gameType = parts[2];
            String[] participantNames = parts[3].split(",");
            
            List<String> participants = new ArrayList<>();
            for (String name : participantNames) {
                participants.add(name.trim());
            }
            
            if ("yacht".equals(gameType)) {
                client.createYachtFrame(roomId, participants);
            } else {
                client.createCatchMindFrame(roomId, participants);
            }
            return true;
        }
        
        // 출제자 지정
        else if (msg.startsWith("/catchmind_drawer ")) {
            String[] parts = msg.split(" ", 3);
            int roomId = Integer.parseInt(parts[1]);
            String drawerName = parts[2];
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.setDrawer(drawerName);
                }
            });
            return true;
        }
        
        // 출제자에게 단어 전송
        else if (msg.startsWith("/catchmind_word ")) {
            String[] parts = msg.split(" ", 3);
            int roomId = Integer.parseInt(parts[1]);
            String word = parts[2];
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.setWord(word);
                }
            });
            return true;
        }
        
        // 게임 채팅 메시지
        else if (msg.startsWith("/game_chat ")) {
            String[] parts = msg.split(" ", 4);
            int roomId = Integer.parseInt(parts[1]);
            String sender = parts[2];
            String message = parts[3];
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    if (sender.equals(client.getMyProfile().getUsername())) {
                        frame.appendChat("[나] " + message);
                    } else {
                        frame.appendChat("[" + sender + "] " + message);
                    }
                }
            });
            return true;
        }
        
        // 타이머 시작
        else if (msg.startsWith("/start_timer ")) {
            String[] parts = msg.split(" ");
            int roomId = Integer.parseInt(parts[1]);
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.startTimer();
                }
            });
            return true;
        }
        
        // 정답 공개
        else if (msg.startsWith("/reveal_answer ")) {
            String[] parts = msg.split(" ", 3);
            int roomId = Integer.parseInt(parts[1]);
            String word = parts[2];
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.revealAnswer(word);
                }
            });
            return true;
        }
        
        // 정답 맞춤 처리
        else if (msg.startsWith("/correct_answer ")) {
            String[] parts = msg.split(" ", 5);
            int roomId = Integer.parseInt(parts[1]);
            String userName = parts[2];
            String word = parts[3];
            int score = Integer.parseInt(parts[4]);
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.onCorrectAnswer(userName, score);
                }
            });
            return true;
        }
        
        // 점수 업데이트
        else if (msg.startsWith("/score_update ")) {
            String[] parts = msg.split(" ");
            int roomId = Integer.parseInt(parts[1]);
            String userName = parts[2];
            int newScore = Integer.parseInt(parts[3]);
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.updateScore(userName, newScore);
                }
            });
            return true;
        }
        
        // 그림 그리기 데이터 수신
        else if (msg.startsWith("/draw ")) {
            String[] parts = msg.split(" ");
            if (parts.length >= 10) {
                int roomId = Integer.parseInt(parts[1]);
                int x1 = Integer.parseInt(parts[2]);
                int y1 = Integer.parseInt(parts[3]);
                int x2 = Integer.parseInt(parts[4]);
                int y2 = Integer.parseInt(parts[5]);
                int r = Integer.parseInt(parts[6]);
                int g = Integer.parseInt(parts[7]);
                int b = Integer.parseInt(parts[8]);
                int size = Integer.parseInt(parts[9]);
                
                Color color = new Color(r, g, b);
                
                SwingUtilities.invokeLater(() -> {
                    CatchMindFrame frame = gameFrames.get(roomId);
                    if (frame != null) {
                        frame.drawRemoteLine(x1, y1, x2, y2, color, size);
                    }
                });
            }
            return true;
        }
        
        // 캔버스 지우기
        else if (msg.startsWith("/cleardraw ")) {
            int roomId = Integer.parseInt(msg.split(" ")[1]);
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.clearRemoteCanvas();
                }
            });
            return true;
        }
        
        // 요트다이스 게임 상태 업데이트
        else if (msg.startsWith("/yacht_update ")) {
            String[] parts = msg.split(" ", 3);
            int roomId = Integer.parseInt(parts[1]);
            String stateData = parts[2];
            
            SwingUtilities.invokeLater(() -> {
                YachtFrame.updateGameInstance(roomId, stateData);
            });
            return true;
        }
        
        // 게임 종료
        else if (msg.startsWith("/game_ended ")) {
            String[] parts = msg.split(" ", 4);
            int roomId = Integer.parseInt(parts[1]);
            String winner = parts.length > 2 ? parts[2] : "게임 종료";
            
            SwingUtilities.invokeLater(() -> {
                CatchMindFrame frame = gameFrames.get(roomId);
                if (frame != null) {
                    frame.gameEnd(winner);
                }
            });
            return true;
        }
        
        return false;
    }
}
