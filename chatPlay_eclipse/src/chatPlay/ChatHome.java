package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ChatHome extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parentFrame;
    private JPanel centerPanel;
    private ChatPanel chatListPanel;
    private JPanel homeWelcomePanel;

    public ChatHome(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        chatListPanel = new ChatPanel(parentFrame);
        createWelcomePanel();

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(homeWelcomePanel, BorderLayout.CENTER); 

        add(new MenuPanel(), BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void createWelcomePanel() {
        homeWelcomePanel = new JPanel();
        homeWelcomePanel.setLayout(new BoxLayout(homeWelcomePanel, BoxLayout.Y_AXIS));
        homeWelcomePanel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("ChatPlay 홈");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        homeWelcomePanel.add(Box.createVerticalGlue());
        homeWelcomePanel.add(title);
        homeWelcomePanel.add(Box.createVerticalGlue());
    }

    public void showChatRoom(ChatRoomData roomData) {
        centerPanel.removeAll();
        centerPanel.add(new ChatRoomPanel(parentFrame, roomData), BorderLayout.CENTER);
        refresh();
    }

    public void restoreChatList() {
        centerPanel.removeAll();
        centerPanel.add(chatListPanel, BorderLayout.CENTER);
        refresh();
    }
    
    public void goHome() {
        centerPanel.removeAll();
        centerPanel.add(homeWelcomePanel, BorderLayout.CENTER);
        refresh();
    }

    public void appendMessageToRoom(int roomId, ChatMessage msg) {
        if (centerPanel.getComponentCount() > 0) {
            Component current = centerPanel.getComponent(0);
            if (current instanceof ChatRoomPanel) {
                ChatRoomPanel panel = (ChatRoomPanel) current;
                if (panel.getRoomId() == roomId) {
                    panel.addBubble(msg);
                }
            }
        }
    }

    private void refresh() {
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    class MenuPanel extends JPanel {
  
		private static final long serialVersionUID = 1L;

		public MenuPanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(230, 235, 255));
            setPreferredSize(new Dimension(80, 0));

            JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 10));
            btnPanel.setOpaque(false);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 0, 5));

            JButton btnHome = new JButton("홈");
            JButton btnProfile = new JButton("프로필");
            JButton btnChat = new JButton("채팅");
            
            styleBtn(btnHome); styleBtn(btnProfile); styleBtn(btnChat);

            btnHome.addActionListener(e -> goHome());
            btnProfile.addActionListener(e -> {
                centerPanel.removeAll();
                centerPanel.add(new ProfilePanel(parentFrame), BorderLayout.CENTER);
                refresh();
            });
            btnChat.addActionListener(e -> restoreChatList());

            btnPanel.add(btnHome); btnPanel.add(btnProfile); btnPanel.add(btnChat);
            add(btnPanel, BorderLayout.NORTH);
        }
        
        private void styleBtn(JButton b) {
            b.setFocusPainted(false);
            b.setBackground(new Color(245, 245, 255));
            b.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        }
    }
}