package chatPlay;


import java.awt.*;
import javax.swing.*;

public class ChatClientMain extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainPanel mainPanel;
    private ChatHome homePanel; // ChatHome으로 전환

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ChatClientMain frame = new ChatClientMain();
            frame.setVisible(true);
        });
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Program");
        setSize(392, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // 카드 레이아웃 기반 컨테이너
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        setContentPane(mainContainer);

        // 두 개의 패널 생성
        mainPanel = new MainPanel(this);
        homePanel = new ChatHome(this); // ChatHome 연결

        mainContainer.add(mainPanel, "Main");
        mainContainer.add(homePanel, "Home");

        cardLayout.show(mainContainer, "Main");
    }

    // 화면 전환 메서드
    public void switchToHome(String username) {
        homePanel.setUsername(username);
        cardLayout.show(mainContainer, "Home");
    }

    public void switchToMain() {
        cardLayout.show(mainContainer, "Main");
    }
}
