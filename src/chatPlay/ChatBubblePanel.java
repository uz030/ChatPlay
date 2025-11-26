package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ChatBubblePanel extends JPanel {
  
    private static final long serialVersionUID = 1L;
    private static final int MAX_W = 220; 

    public ChatBubblePanel(ChatMessage msg) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // 1. 시스템 메시지
        if (msg.getSender().equals("System")) {
            JPanel sysBubble = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220, 230, 240));  
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                }
            };
            sysBubble.setLayout(new BorderLayout());
            sysBubble.setOpaque(false);
            sysBubble.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel label = new JLabel(msg.getContent(), SwingConstants.CENTER);
            label.setForeground(new Color(70, 70, 70));
            label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

            sysBubble.add(label, BorderLayout.CENTER);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
            wrapper.setOpaque(false);
            wrapper.add(sysBubble);

            add(wrapper, BorderLayout.CENTER);
            return;  
        }

        // 2. 챗봇 메시지
        if (msg.isBotMessage()) {
            add(createBotPanel(msg), BorderLayout.WEST);
        } 
        
        // 3. 일반 유저 메시지
        else {
            if (msg.isMine()) {

                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                rightPanel.setOpaque(false);
                rightPanel.add(createBubble(msg, true));
                add(rightPanel, BorderLayout.EAST);
            } else {
                
                JPanel container = new JPanel(new BorderLayout(8, 0)); // 수평 간격 8
                container.setOpaque(false);

                // (1) 프로필 사진 (왼쪽 상단)
                JLabel iconLabel = new JLabel(getScaledIcon(msg.getSenderIcon(), 40, 40));
                JPanel iconPanel = new JPanel(new BorderLayout());
                iconPanel.setOpaque(false);
                iconPanel.add(iconLabel, BorderLayout.NORTH);
                
                container.add(iconPanel, BorderLayout.WEST);

                // (2) 텍스트 내용 (이름 + 말풍선)
                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.setOpaque(false);
                
                JLabel name = new JLabel(msg.getSender());
                name.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
                name.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 0));
                
                JPanel bubbleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                bubbleWrapper.setOpaque(false);
                bubbleWrapper.add(createBubble(msg, false));

                contentPanel.add(name, BorderLayout.NORTH);
                contentPanel.add(bubbleWrapper, BorderLayout.CENTER);

                container.add(contentPanel, BorderLayout.CENTER);
                
                add(container, BorderLayout.WEST);
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
        
        JLabel name = new JLabel("ChatBot");
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

    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) {
            try {
                src = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
            } catch(Exception e) {
                return new ImageIcon();
            }
        }
        if (src.getImage() == null) return new ImageIcon();

        Image img = src.getImage();
        Image newImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}