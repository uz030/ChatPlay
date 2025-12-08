package yacht;

import javax.swing.*;
import java.awt.*;

public class YachtPanel extends JPanel {

    private YachtFrame parent;
    private int[] diceValues = {1, 1, 1, 1, 1};
    private boolean[] keepDice = new boolean[5];
    private JButton btnRoll;
    private JLabel infoLabel;
    
    // 주사위 영역
    private JPanel diceContainer;
    private JButton[] diceButtons = new JButton[5];

    public YachtPanel(YachtFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 정보
        infoLabel = new JLabel("게임 대기 중...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(infoLabel, BorderLayout.NORTH);

        // 중앙: 주사위 및 롤 버튼
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        diceContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        diceContainer.setOpaque(false);

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            diceButtons[i] = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawDice(g, getWidth(), getHeight(), diceValues[idx], keepDice[idx]);
                }
            };
            diceButtons[i].setPreferredSize(new Dimension(60, 60));
            diceButtons[i].setContentAreaFilled(false);
            diceButtons[i].setBorderPainted(false);
            diceButtons[i].setFocusPainted(false);
            diceButtons[i].addActionListener(e -> {
                keepDice[idx] = !keepDice[idx];
                diceButtons[idx].repaint();
            });
            diceContainer.add(diceButtons[i]);
        }
        centerPanel.add(diceContainer, BorderLayout.CENTER);

        btnRoll = new JButton("ROLL!");
        btnRoll.setFont(new Font("Arial", Font.BOLD, 14));
        btnRoll.setBackground(new Color(100, 150, 255));
        btnRoll.setForeground(Color.WHITE);
        btnRoll.setPreferredSize(new Dimension(100, 40));
        btnRoll.addActionListener(e -> parent.sendRoll(keepDice));
        
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(btnRoll);
        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateDice(int[] values, int rollsLeft, boolean myTurn) {
        this.diceValues = values;
        
        infoLabel.setText(myTurn ? ("남은 횟수: " + rollsLeft) : "상대방 턴입니다.");
        btnRoll.setEnabled(myTurn && rollsLeft > 0);
        
        // 턴이 바뀌거나 첫 롤이면 keep 초기화
        if (rollsLeft == 3) {
            keepDice = new boolean[5];
        }

        for (JButton btn : diceButtons) btn.repaint();
    }

    // 주사위 그리기 헬퍼
    private void drawDice(Graphics g, int w, int h, int value, boolean keep) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 배경
        g2.setColor(keep ? new Color(255, 100, 100) : Color.WHITE);
        g2.fillRoundRect(2, 2, w-4, h-4, 15, 15);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(2, 2, w-4, h-4, 15, 15);

        // 점 찍기
        g2.setColor(keep ? Color.WHITE : Color.BLACK);
        int dotSize = 10;
        int mid = w / 2;
        int q1 = w / 4;
        int q3 = w * 3 / 4;

        if (value % 2 == 1) g2.fillOval(mid - dotSize/2, mid - dotSize/2, dotSize, dotSize); // Center
        if (value >= 2) {
            g2.fillOval(q1 - dotSize/2, q1 - dotSize/2, dotSize, dotSize);
            g2.fillOval(q3 - dotSize/2, q3 - dotSize/2, dotSize, dotSize);
        }
        if (value >= 4) {
            g2.fillOval(q1 - dotSize/2, q3 - dotSize/2, dotSize, dotSize);
            g2.fillOval(q3 - dotSize/2, q1 - dotSize/2, dotSize, dotSize);
        }
        if (value == 6) {
            g2.fillOval(q1 - dotSize/2, mid - dotSize/2, dotSize, dotSize);
            g2.fillOval(q3 - dotSize/2, mid - dotSize/2, dotSize, dotSize);
        }
    }
}