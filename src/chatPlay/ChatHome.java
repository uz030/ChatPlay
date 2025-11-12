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
            setPreferredSize(new Dimension(100, 0)); // 왼쪽 고정 폭

            // 🔸 버튼들
            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.setLayout(new GridLayout(3, 1, 0, 0));

            // 버튼 생성
            JButton btnProfile = createButton("프로필");
            JButton btnChat = createButton("채팅");

            // 액션 리스너
            btnProfile.addActionListener(e -> switchPanel(new ProfilePanel(username)));
            btnChat.addActionListener(e -> switchPanel(new ChatPanel()));

            btnPanel.add(btnProfile);
            btnPanel.add(btnChat);

            add(btnPanel, BorderLayout.CENTER);
        }

        private JButton createButton(String text) {
            JButton btn = new JButton(text);
            btn.setFocusPainted(false);
            btn.setBackground(new Color(200, 210, 245));
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            return btn;
        }

        // 🔹 중앙 패널 교체 메서드
        private void switchPanel(JPanel newPanel) {
            centerPanel.removeAll();
            centerPanel.add(newPanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }

    
}
