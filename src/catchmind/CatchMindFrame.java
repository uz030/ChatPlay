package catchmind;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CatchMindFrame extends JFrame {
    
    private int roomId;
    private String myUsername;
    private DataOutputStream dos;
    
    private DrawingPanel drawingPanel;
    private JTextArea chatArea;
    private JTextField inputField;
    private JLabel answerLabel;
    private JLabel timerLabel;
    private JPanel playerPanel;

    // 도구 패널 참조용
    private JPanel toolPanel;
    
    private List<GamePlayer> players = new ArrayList<>();
    private String currentWord = "";
    
    // 타이머 관련
    private Timer clientTimer;
    private int remainingSeconds = 0;
    private static final int ROUND_TIME = 120; // 2분
    
    public CatchMindFrame(int roomId, String myUsername, DataOutputStream dos, List<String> participants) {
        this.roomId = roomId;
        this.myUsername = myUsername;
        this.dos = dos;
        
        for (String name : participants) {
            players.add(new GamePlayer(name));
        }
        
        setTitle("캐치마인드 - " + myUsername);
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initUI();
        setToolPanelEnabled(false);
        setVisible(true);
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        // 상단 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        answerLabel = new JLabel("???", SwingConstants.CENTER);
        answerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        answerLabel.setForeground(new Color(0, 120, 200));
        
        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        
        topPanel.add(new JLabel("정답: ", SwingConstants.CENTER), BorderLayout.WEST);
        topPanel.add(answerLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // 중앙 패널
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JPanel drawingSection = new JPanel(new BorderLayout());
        drawingPanel = new DrawingPanel();
        drawingPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        toolPanel = createToolPanel(); // [FIX]
        
        drawingSection.add(drawingPanel, BorderLayout.CENTER);
        drawingSection.add(toolPanel, BorderLayout.SOUTH);
        
        JPanel chatSection = new JPanel(new BorderLayout());
        chatSection.setPreferredSize(new Dimension(250, 0));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.addActionListener(e -> sendAnswer());

        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel answerHintLabel = new JLabel("정답 입력:");
        answerHintLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        inputPanel.add(answerHintLabel, BorderLayout.NORTH);
        inputPanel.add(inputField, BorderLayout.CENTER);

        chatSection.add(chatScroll, BorderLayout.CENTER);
        chatSection.add(inputPanel, BorderLayout.SOUTH);

        centerPanel.add(drawingSection, BorderLayout.CENTER);
        centerPanel.add(chatSection, BorderLayout.EAST);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 좌측 패널
        playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setBackground(Color.WHITE);
        playerPanel.setBorder(BorderFactory.createTitledBorder("참가자"));
        playerPanel.setPreferredSize(new Dimension(150, 0));
        
        updatePlayerPanel();
        
        add(new JScrollPane(playerPanel), BorderLayout.WEST);
        
        // 그림 이벤트 리스너
        drawingPanel.setDrawListener(new DrawingPanel.DrawEventListener() {
            @Override
            public void onDraw(int x1, int y1, int x2, int y2, Color color, int size) {
                try {
                    dos.writeUTF(String.format("/draw %d %d %d %d %d %d %d %d %d",
                        roomId, x1, y1, x2, y2, 
                        color.getRed(), color.getGreen(), color.getBlue(), size));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            @Override
            public void onClear() {
                try {
                    dos.writeUTF("/cleardraw " + roomId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private JPanel createToolPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5)); // [FIX]
        panel.setBackground(Color.WHITE);
        
        Color[] colors = {
            Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, 
            Color.YELLOW, Color.ORANGE, Color.PINK
        };
        
        Dimension toolBtnSize = new Dimension(24, 24); // [FIX]

        for (Color c : colors) {
            JButton btn = new JButton();
            btn.setPreferredSize(toolBtnSize);
            btn.setBackground(c);
            btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.addActionListener(e -> drawingPanel.setPenColor(c));
            panel.add(btn);
        }

        // 이미지 지우개 버튼
        ImageIcon icon = new ImageIcon(
            getClass().getResource("/images/eraser.png")
        );
        Image scaled = icon.getImage().getScaledInstance(
            toolBtnSize.width, toolBtnSize.height, Image.SCALE_SMOOTH
        );
        JButton eraserBtn = new JButton(new ImageIcon(scaled));
        eraserBtn.setPreferredSize(toolBtnSize);
        eraserBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        eraserBtn.setContentAreaFilled(false);
        eraserBtn.addActionListener(e -> {
            if (!drawingPanel.canDraw()) return;
            drawingPanel.setPenColor(Color.WHITE);
        });
        panel.add(eraserBtn);

        panel.add(Box.createHorizontalStrut(10));

        JLabel sizeLabel = new JLabel("굵기:");
        JSlider sizeSlider = new JSlider(1, 20, 4);
        sizeSlider.setPreferredSize(new Dimension(100, 24));
        sizeSlider.addChangeListener(e ->
            drawingPanel.setPenSize(sizeSlider.getValue())
        );

        panel.add(sizeLabel);
        panel.add(sizeSlider);

        //  중요
        panel.setMinimumSize(new Dimension(0, 45));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        return panel;
    }
    
    // 도구 패널 활성/비활성
    private void setToolPanelEnabled(boolean enabled) {
        for (Component c : toolPanel.getComponents()) {
            c.setEnabled(enabled);
        }
    }
    
    private void updatePlayerPanel() {
        playerPanel.removeAll();
        
        for (GamePlayer player : players) {
            JPanel playerCard = new JPanel(new BorderLayout(5, 5));
            playerCard.setMaximumSize(new Dimension(140, 60));
            playerCard.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            playerCard.setBackground(player.isDrawer() ? new Color(255, 255, 200) : Color.WHITE);
            
            JLabel nameLabel = new JLabel(player.getUsername());
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            
            JLabel scoreLabel = new JLabel(player.getScore() + "점");
            scoreLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            
            if (player.isDrawer()) {
                nameLabel.setText("🎨 " + player.getUsername());
            }
            
            playerCard.add(nameLabel, BorderLayout.CENTER);
            playerCard.add(scoreLabel, BorderLayout.SOUTH);
            
            playerPanel.add(playerCard);
            playerPanel.add(Box.createVerticalStrut(5));
        }
        
        playerPanel.revalidate();
        playerPanel.repaint();
    }
    
    private void sendAnswer() {
        String answer = inputField.getText().trim();
        if (answer.isEmpty()) return;
        
        try {
            dos.writeUTF("/gamemsg " + roomId + " " + answer);
            inputField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void startTimer() {
        stopTimer();
        
        remainingSeconds = ROUND_TIME;
        updateTimerLabel();
        
        clientTimer = new Timer(1000, e -> {
            remainingSeconds--;
            updateTimerLabel();
            
            if (remainingSeconds <= 0) {
                stopTimer();
            }
        });
        clientTimer.start();
    }
    
    public void stopTimer() {
        if (clientTimer != null) {
            clientTimer.stop();
            clientTimer = null;
        }
    }
    
    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        
        if (remainingSeconds <= 10) {
            timerLabel.setForeground(Color.RED);
        } else if (remainingSeconds <= 30) {
            timerLabel.setForeground(Color.ORANGE);
        } else {
            timerLabel.setForeground(Color.BLACK);
        }
    }
    
    public void setWord(String word) {
        currentWord = word;
        answerLabel.setText(word);
        drawingPanel.setCanDraw(true);
        setToolPanelEnabled(true); // [FIX]
        appendChat("당신이 출제자입니다! 단어: " + word);
    }
    
    public void setDrawer(String drawerName) {
        for (GamePlayer p : players) {
            p.setDrawer(p.getUsername().equals(drawerName));
        }
        
        boolean isMyTurn = drawerName.equals(myUsername);
        drawingPanel.setCanDraw(isMyTurn);
        drawingPanel.clearCanvas();          // [FIX]
        setToolPanelEnabled(isMyTurn);       // [FIX]
        
        if (!isMyTurn) {
            answerLabel.setText("???");
        }
        
        updatePlayerPanel();
        appendChat("출제자: " + drawerName);
    }
    
    public void onCorrectAnswer(String username, int score) {
        for (GamePlayer p : players) {
            if (p.getUsername().equals(username)) {
                p.addScore(score);
                break;
            }
        }
        updatePlayerPanel();
        appendChat("🎉 " + username + "님이 정답을 맞췄습니다! (+" + score + "점)");
    }
    
    public void updateScore(String username, int score) {
        for (GamePlayer p : players) {
            if (p.getUsername().equals(username)) {
                p.addScore(score);
                break;
            }
        }
        updatePlayerPanel();
    }
    
    public void revealAnswer(String word) {
        answerLabel.setText(word);
        appendChat("⏰ 시간 초과! 정답: " + word);
    }
    
    public void drawRemoteLine(int x1, int y1, int x2, int y2, Color color, int size) {
        drawingPanel.drawLine(x1, y1, x2, y2, color, size);
    }
    
    public void clearRemoteCanvas() {
        drawingPanel.clearCanvas();
    }
    
    public void appendChat(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }
    
    public void gameEnd(String winner) {
        stopTimer();
        JOptionPane.showMessageDialog(this,
            "게임 종료!\n우승자: " + winner,
            "게임 종료",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void dispose() {
        stopTimer();
        super.dispose();
    }
}
