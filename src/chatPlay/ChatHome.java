package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.net.URL; 

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

    public void showProfilePanel() {
        centerPanel.removeAll();
        centerPanel.add(new ProfilePanel(parentFrame), BorderLayout.CENTER);
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
    
    public void refreshProfile() {
        // ProfilePanel 갱신
        showProfilePanel();
    }


    class MenuPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public MenuPanel() {
            setLayout(new BorderLayout());
            setOpaque(false); // paintComponent로 배경 직접 칠할 거라 false

            setPreferredSize(new Dimension(80, 0));

            JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 10));
            btnPanel.setOpaque(false);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 0, 5));

            ImageIcon iconProfile = loadIcon("/images/friend.png", 40, 40);
            ImageIcon iconProfileHover = loadIcon("/images/friend_hover.png", 40, 40);

            ImageIcon iconChat = loadIcon("/images/chat.png", 40, 40);
            ImageIcon iconChatHover = loadIcon("/images/chat_hover.png", 40, 40);

            JButton btnProfile = new JButton(iconProfile);
            JButton btnChat = new JButton(iconChat);

            styleBtn(btnProfile);
            styleBtn(btnChat);

            // 호버 효과 추가 (이미지가 로드되었을 때만 적용)
            if (iconProfile != null && iconProfileHover != null) {
                addHoverIcon(btnProfile, iconProfile, iconProfileHover);
            }
            if (iconChat != null && iconChatHover != null) {
                addHoverIcon(btnChat, iconChat, iconChatHover);
            }

            btnProfile.addActionListener(e -> {
                centerPanel.removeAll();
                centerPanel.add(new ProfilePanel(parentFrame), BorderLayout.CENTER);
                refresh();
            });

            btnChat.addActionListener(e -> restoreChatList());

            btnPanel.add(btnProfile);
            btnPanel.add(btnChat);
            add(btnPanel, BorderLayout.NORTH);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(209, 204, 236, 178), // #D1CCEC + alpha
                    getWidth(), 0, new Color(212, 218, 254, 178) // #D4DAFE + alpha
            );

            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }

        private void styleBtn(JButton b) {
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setMargin(new Insets(0, 0, 0, 0));
        }

        private void addHoverIcon(JButton b, ImageIcon normal, ImageIcon hover) {
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    b.setIcon(hover);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    b.setIcon(normal);
                }
            });
        }

        private ImageIcon loadIcon(String path, int w, int h) {
            URL imgUrl = getClass().getResource(path);
            
            if (imgUrl == null) {
                System.err.println("이미지를 찾을 수 없습니다 (경로 확인 필요): " + path);
                return null; 
            }

            ImageIcon icon = new ImageIcon(imgUrl);
            Image img = icon.getImage();
            Image newImg = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(newImg);
        }
    }
}