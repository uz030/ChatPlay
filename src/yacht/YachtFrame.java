package yacht;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.DataOutputStream;
import java.util.*;
import java.util.List;

public class YachtFrame extends JFrame {
    
    private static Map<Integer, YachtFrame> openFrames = new HashMap<>();

    private int roomId;
    private String myName;
    private DataOutputStream dos;
    private List<String> participants;

    private YachtPanel gamePanel;
    private JTextArea chatArea;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    
    private int[] currentDice = {1, 1, 1, 1, 1};
    private boolean isMyTurn = false;
    private int myPlayerIndex = -1;
    private int currentRollsLeft = 3;

    private final String[] CATEGORIES = {
        "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes", 
        "Choice", "4 of Kind", "Full House", "S. Straight", "L. Straight", "Yacht", 
        "Bonus", "Total"
    };
    
    private final String[] CATEGORY_DESCRIPTIONS = {
        "1의 개수 × 1",
        "2의 개수 × 2",
        "3의 개수 × 3",
        "4의 개수 × 4",
        "5의 개수 × 5",
        "6의 개수 × 6",
        "모든 주사위 합계",
        "같은 숫자 4개 이상 → 합계",
        "트리플 + 페어 → 합계",
        "4연속 숫자 → 15점",
        "5연속 숫자 → 30점",
        "같은 숫자 5개 → 50점",
        "상단 합계 63점 이상 → 35점",
        "전체 합계"
    };

    public YachtFrame(int roomId, String myName, DataOutputStream dos, List<String> participants) {
        this.roomId = roomId;
        this.myName = myName;
        this.dos = dos;
        this.participants = participants;
        
        // 내 플레이어 인덱스 찾기
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).equals(myName)) {
                myPlayerIndex = i;
                break;
            }
        }

        openFrames.put(roomId, this);

        setTitle("Yacht Dice - " + myName);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. 왼쪽: 점수판
        String[] cols = new String[participants.size() + 1];
        cols[0] = "Category";
        for (int i=0; i<participants.size(); i++) cols[i+1] = participants.get(i);

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        
        for (String cat : CATEGORIES) {
            Vector<Object> row = new Vector<>();
            row.add(cat);
            for(int i=0; i<participants.size(); i++) row.add("");
            tableModel.addRow(row);
        }

        scoreTable = new JTable(tableModel);
        scoreTable.setRowHeight(28);
        scoreTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        // 카테고리 클릭 이벤트 (점수 선택)
        scoreTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = scoreTable.getSelectedRow();
                // 보너스와 토탈은 선택 불가 (index 12, 13)
                // 한 번 이상 굴려야 선택 가능 (rollsLeft < 3)
                if (row >= 0 && row < 12 && isMyTurn && currentRollsLeft < 3) {
                    sendSelect(row);
                } else if (row >= 0 && row < 12 && isMyTurn && currentRollsLeft == 3) {
                    // 아직 주사위를 굴리지 않았을 때 경고 메시지
                    JOptionPane.showMessageDialog(
                        YachtFrame.this,
                        "주사위를 한 번 이상 굴린 후 점수를 선택할 수 있습니다.",
                        "알림",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        
        // 족보 가이드 버튼
        JButton guideBtn = new JButton("족보 가이드");
        guideBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        guideBtn.addActionListener(e -> showGuideDialog());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);
        JPanel guidePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        guidePanel.add(guideBtn);
        leftPanel.add(guidePanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(320, 0));
        add(leftPanel, BorderLayout.WEST);

        // 2. 중앙: 게임 패널
        gamePanel = new YachtPanel(this);
        add(gamePanel, BorderLayout.CENTER);

        // 3. 오른쪽: 채팅 (간소화)
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.EAST);
        
        // 윈도우 닫힐 때 정리
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                openFrames.remove(roomId);
            }
        });
    }

    // 서버로 ROLL 명령 전송
    public void sendRoll(boolean[] keep) {
        StringBuilder sb = new StringBuilder();
        for (boolean k : keep) sb.append(k ? "1" : "0");
        
        try {
            dos.writeUTF("/game_yacht " + roomId + " roll " + sb.toString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 서버로 SELECT 명령 전송
    public void sendSelect(int categoryIdx) {
        // 한 번 이상 굴려야 선택 가능
        if (currentRollsLeft == 3) {
            JOptionPane.showMessageDialog(
                this,
                "주사위를 한 번 이상 굴린 후 점수를 선택할 수 있습니다.",
                "알림",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        
        try {
            dos.writeUTF("/game_yacht " + roomId + " select " + categoryIdx);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 서버로부터 상태 업데이트 수신
    public static void updateGameInstance(int roomId, String data) {
        YachtFrame frame = openFrames.get(roomId);
        if (frame != null) frame.parseAndUpdate(data);
    }

    private void parseAndUpdate(String data) {
        try {
            String[] sections = data.split("/");
            int currentIdx = Integer.parseInt(sections[0]);
            int rollsLeft = Integer.parseInt(sections[1]);
            this.currentRollsLeft = rollsLeft; // 현재 남은 롤 횟수 저장
            
            // Dice
            String[] diceStr = sections[2].split(",");
            int[] dice = new int[5];
            for(int i=0; i<5; i++) dice[i] = Integer.parseInt(diceStr[i]);
            this.currentDice = dice;
            
            String currentTurnName = participants.get(currentIdx);
            this.isMyTurn = currentTurnName.equals(myName);
            
            // GamePanel Update
            gamePanel.updateDice(dice, rollsLeft, isMyTurn);

            // ScoreTable Update
            String[] pScores = sections[3].split(";");
            for (int i=0; i<pScores.length; i++) {
                String[] scores = pScores[i].split(",");
                // j: 0~13 (rows)
                for (int j=0; j<14; j++) {
                    if (j < scores.length) {
                        String scoreValue = scores[j];
                        // 이미 점수가 있으면 그대로 표시
                        if (!scoreValue.isEmpty() && !scoreValue.equals("0")) {
                            tableModel.setValueAt(scoreValue, j, i + 1);
                        } else {
                            // 내 턴이고, 내 열이고, 아직 점수가 없으면 미리보기 표시
                            // 단, 한 번 이상 굴린 후에만 미리보기 표시 (rollsLeft < 3)
                            if (isMyTurn && i == myPlayerIndex && j < 12 && rollsLeft < 3) {
                                int previewScore = calculateScore(j, dice);
                                if (previewScore > 0) {
                                    tableModel.setValueAt("(" + previewScore + ")", j, i + 1);
                                } else {
                                    tableModel.setValueAt("", j, i + 1);
                                }
                            } else {
                                tableModel.setValueAt("", j, i + 1);
                            }
                        }
                    }
                }
            }
            
            // 턴 표시 (테이블 헤더 색상 변경 등은 복잡하므로 제목바에 표시)
            setTitle("Yacht Dice - " + myName + (isMyTurn ? " [당신의 턴]" : " [" + currentTurnName + "의 턴]"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 점수 계산 로직 (YachtGame과 동일)
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
    
    // 족보 가이드 다이얼로그
    private void showGuideDialog() {
        JDialog dialog = new JDialog(this, "족보 가이드", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JTextArea guideArea = new JTextArea();
        guideArea.setEditable(false);
        guideArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        guideArea.setBackground(Color.WHITE);
        
        StringBuilder guide = new StringBuilder();
        guide.append("═══════════════════════════════════\n");
        guide.append("         요트다이스 족보 가이드\n");
        guide.append("═══════════════════════════════════\n\n");
        
        for (int i = 0; i < 12; i++) {
            guide.append("[").append(CATEGORIES[i]).append("]\n");
            guide.append("  ").append(CATEGORY_DESCRIPTIONS[i]).append("\n\n");
        }
        
        guide.append("═══════════════════════════════════\n");
        guide.append("게임 방법:\n");
        guide.append("1. 주사위를 굴립니다 (최대 3회)\n");
        guide.append("2. 원하는 주사위를 고정할 수 있습니다\n");
        guide.append("3. 12개 카테고리 중 하나를 선택합니다\n");
        guide.append("4. 각 카테고리는 한 번만 사용 가능합니다\n");
        guide.append("5. 상단 합계 63점 이상 시 보너스 35점\n");
        guide.append("6. 총 12라운드 후 총점이 높은 사람 승리\n");
        
        guideArea.setText(guide.toString());
        
        JScrollPane scrollPane = new JScrollPane(guideArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        dialog.add(scrollPane);
        dialog.setVisible(true);
    }
}