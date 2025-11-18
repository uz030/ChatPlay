package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ChatHome extends JPanel {

    private ChatClientMain parentFrame;
    private JPanel centerPanel;

    public ChatHome(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        MenuPanel menu = new MenuPanel();
        add(menu, BorderLayout.WEST);

        centerPanel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("여기가 ChatHome 화면입니다.", SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        centerPanel.add(lbl, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    // ChatClientMain의 리스너 스레드가 호출할 메서드 (현재 활성화된 패널이 ChatPanel일 경우 메시지 전달)

    public void dispatchChatMessage(String msg) {
        if (centerPanel.getComponentCount() > 0) {
            Component currentPanel = centerPanel.getComponent(0);
            if (currentPanel instanceof ChatPanel) {
                // ((ChatPanel) currentPanel).appendMessage(msg);
            }
        }
    }


    class MenuPanel extends JPanel {
        public MenuPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(230, 235, 255));
            setPreferredSize(new Dimension(100, 0));

            JPanel btnPanel = new JPanel();
            btnPanel.setOpaque(false);
            btnPanel.setLayout(new GridLayout(3, 1, 0, 0));

            JButton btnProfile = createButton("프로필");
            JButton btnChat = createButton("채팅");

            // ProfilePanel 생성 시 parentFrame(ChatClientMain) 참조 전달
            btnProfile.addActionListener(e -> switchPanel(new ProfilePanel(parentFrame)));

            btnChat.addActionListener(e -> switchPanel(new ChatPanel())); // (일단 기존 코드 유지)

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

        private void switchPanel(JPanel newPanel) {
            centerPanel.removeAll();
            centerPanel.add(newPanel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        }
    }
}