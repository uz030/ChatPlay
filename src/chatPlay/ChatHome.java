package chatPlay;


import javax.swing.*;

import java.awt.*;

public class ChatHome extends JPanel {

    private ChatClientMain parentFrame;
    private String username;
    private JPanel centerPanel; // 🔹 중앙 패널 참조

    public ChatHome(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 🔹 왼쪽 메뉴 패널
        MenuPanel menu = new MenuPanel();
        add(menu, BorderLayout.WEST);

        // 🔹 중앙 패널 (기본: 홈화면)
        centerPanel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("여기가 ChatHome 화면입니다.", SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        centerPanel.add(lbl, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // 🔹 내부 메뉴 클래스
    class MenuPanel extends JPanel {
        public MenuPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(230, 235, 255));
            setPreferredSize(new Dimension(65, 200)); // 왼쪽 고정 폭

            // 🔸 버튼들
            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.setLayout(new GridLayout(3, 1, 0, 0));
            btnPanel.setPreferredSize(new Dimension(65, 130));
            // 버튼 생성
            JButton btnProfile = createImageButton("/images/friend.png");
            JButton btnChat = createImageButton("/images/chat.png");
            // 액션 리스너
            btnProfile.addActionListener(e -> switchPanel(new ProfilePanel(username)));
            btnChat.addActionListener(e -> switchPanel(new ChatPanel()));

            btnPanel.add(btnProfile);
            btnPanel.add(btnChat);

            add(btnPanel, BorderLayout.CENTER);
        }

        private JButton createImageButton(String imagePath) {
            JButton btn = new JButton();

            // 이미지 로드
            ImageIcon icon = new ImageIcon(getClass().getResource(imagePath));
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(img));

            // 버튼 모양 지우기
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            

            

            // 마우스 커서 변경
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return btn;
        }

        // 🔹 중앙 패널 교체 메서드
        private void switchPanel(JPanel newPanel) {
            centerPanel.removeAll();
            centerPanel.add(newPanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(226, 232, 255),   // #C8D4FF
                    getWidth(), 0, new Color(227, 224, 250) // #C7C2F4
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    
}
