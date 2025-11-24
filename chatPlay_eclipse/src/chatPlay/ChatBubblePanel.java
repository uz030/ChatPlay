package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ChatBubblePanel extends JPanel {
  
	private static final long serialVersionUID = 1L;
	private static final int MAX_W = 220; 

    public ChatBubblePanel(ChatMessage msg) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        if (msg.isBotMessage()) {
            add(createBotPanel(msg), BorderLayout.WEST);
        } else {
            if (msg.isMine()) {
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                rightPanel.setOpaque(false);
                rightPanel.add(createBubble(msg, true));
                add(rightPanel, BorderLayout.EAST);
            } else {
                JPanel leftPanel = new JPanel(new BorderLayout());
                leftPanel.setOpaque(false);
                
                // 상대방 이름은 첫 메시지에만 표시하는 게 예쁘지만, 일단 간격만 조정
                JLabel name = new JLabel(msg.getSender());
                name.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
                name.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                
                JPanel bubbleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                bubbleWrapper.setOpaque(false);
                bubbleWrapper.add(createBubble(msg, false));

                leftPanel.add(name, BorderLayout.NORTH);
                leftPanel.add(bubbleWrapper, BorderLayout.CENTER);
                
                add(leftPanel, BorderLayout.WEST);
            }
        }
    }

    private JPanel createBubble(ChatMessage msg, boolean isMine) {
        JTextArea area = new JTextArea(msg.getContent());
        Font font = new Font("맑은 고딕", Font.PLAIN, 14);
        area.setFont(font);
        area.setOpaque(false);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(isMine ? Color.WHITE : Color.BLACK);

        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(font);
        int textWidth = fm.stringWidth(msg.getContent());
        
        int bubbleWidth = Math.min(textWidth, MAX_W) + 24; 
        
        area.setSize(new Dimension(bubbleWidth, Short.MAX_VALUE));
        Dimension prefSize = area.getPreferredSize();
        area.setPreferredSize(new Dimension(bubbleWidth, prefSize.height));

        JPanel bubble = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMine ? new Color(0, 149, 246) : new Color(239, 239, 239));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18); 
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10)); 
        bubble.add(area);
        
        return bubble;
    }

    private JPanel createBotPanel(ChatMessage msg) {
        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.setOpaque(false);
        
        JLabel name = new JLabel("ChatBot 🤖");
        name.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        name.setForeground(new Color(0, 120, 200));
        botPanel.add(name, BorderLayout.NORTH);

        JPanel btnContainer = new JPanel(new GridLayout(0, 1, 5, 5));
        btnContainer.setOpaque(false);
        
        String[] menus = msg.getContent().substring("BOT_MENU:".length()).split(",");
        for (String m : menus) {
            JButton btn = new JButton(m);
            btn.setBackground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> JOptionPane.showMessageDialog(this, "[" + m + "] 기능 준비 중!"));
            btnContainer.add(btn);
        }

        JPanel bgPanel = new JPanel(new BorderLayout());
        bgPanel.setOpaque(false);
        bgPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        bgPanel.add(btnContainer);
        botPanel.add(bgPanel, BorderLayout.CENTER);
        return botPanel;
    }
}