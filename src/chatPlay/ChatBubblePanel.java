package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChatBubblePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private static final int MAX_W = 220; 

    // 생성자 (ActionListener 포함 버전 - 챗봇 기능용)
    public ChatBubblePanel(ChatMessage msg, ActionListener botMenuListener) {
        setLayout(new BorderLayout(10, 5));
        setOpaque(false);

        boolean isMe = msg.isMine();
        String sender = msg.getSender();
        String content = msg.getContent();

        // === BOT 메뉴 처리 ===
        if (content.startsWith("BOT_MENU:")) {
            String menuStr = content.substring(9);
            String[] items = menuStr.split(",");

            JPanel menuPanel = new JPanel();
            menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
            menuPanel.setOpaque(false);

            for (String item : items) {
                JButton btn = new JButton(item.trim());
                btn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setMaximumSize(new Dimension(200, 35));
                btn.setBackground(new Color(100, 149, 237));
                btn.setForeground(Color.WHITE);
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);

                btn.addActionListener(botMenuListener);
                menuPanel.add(btn);
                menuPanel.add(Box.createVerticalStrut(8));
            }

            add(menuPanel, BorderLayout.CENTER);
            return;
        }

        // === 일반 메시지 ===
        JPanel contentPanel = new JPanel(new BorderLayout(8, 0));
        contentPanel.setOpaque(false);

        // ✅ 프로필 이미지 - 원형으로 표시
        if (!isMe) {
            JLabel iconLabel = new JLabel();
            ImageIcon icon = msg.getSenderIcon();
            if (icon != null) {
                iconLabel.setIcon(CircularProfileIcon.createCircularIcon(icon, 35));
            } else {
                iconLabel.setIcon(CircularProfileIcon.createDefaultCircularIcon(35));
            }
            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            contentPanel.add(iconLabel, BorderLayout.WEST);
        }

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        if (!isMe) {
            JLabel nameLabel = new JLabel(sender);
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            nameLabel.setForeground(Color.GRAY);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPanel.add(nameLabel);
            textPanel.add(Box.createVerticalStrut(3));
        }

        // 이미지 메시지
        if (msg.getType() == ChatMessage.MessageType.IMAGE) {
            JLabel imgLabel = new JLabel(msg.getImageIcon());
            imgLabel.setAlignmentX(isMe ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            textPanel.add(imgLabel);
        } 
        // 텍스트 메시지
        else {
            JTextArea textArea = new JTextArea(content);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            textArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            if (isMe) {
                textArea.setBackground(new Color(255, 235, 59));
                textArea.setAlignmentX(Component.RIGHT_ALIGNMENT);
            } else {
                textArea.setBackground(Color.WHITE);
                textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            }

            textArea.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
            textPanel.add(textArea);
        }

        if (isMe) {
            contentPanel.add(textPanel, BorderLayout.EAST);
            add(contentPanel, BorderLayout.EAST);
        } else {
            contentPanel.add(textPanel, BorderLayout.CENTER);
            add(contentPanel, BorderLayout.WEST);
        }
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

    // 말풍선 생성
    private JPanel createBubble(ChatMessage msg, boolean isMine) {
    	
    	if (msg.getType() == ChatMessage.MessageType.IMAGE) {
            return createImagePanel(msg, isMine);
        }

    	
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
    
    
    private JPanel createImagePanel(ChatMessage msg, boolean isMine) {
        ImageIcon icon = msg.getImageIcon();
        
        // 이미 리사이징된 아이콘 사용
        if (icon == null) {
            icon = new ImageIcon(); // 빈 아이콘
        }

        JLabel imgLabel = new JLabel(icon);
        imgLabel.setPreferredSize(new Dimension(180, 180));

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
        bubble.add(imgLabel, BorderLayout.CENTER);
        bubble.setMaximumSize(new Dimension(200, 200));

        return bubble;
    }
    
    // 봇 패널 생성
    private JPanel createBotPanel(ChatMessage msg, ActionListener onBotMenuClick) {
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
            btn.addActionListener(e -> {
                if (onBotMenuClick != null) {
                    onBotMenuClick.actionPerformed(
                        new java.awt.event.ActionEvent(btn, 0, m)
                    );
                }
            });

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