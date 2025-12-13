package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.net.URL; 

/**
 * 채팅 홈 화면 패널
 * 프로필, 채팅 목록 등을 관리하는 메인 컨테이너
 */
public class ChatHome extends JPanel {

    private static final long serialVersionUID = 1L;
    private ChatClientMain parentFrame;
    private JPanel centerPanel;
    private ChatPanel chatListPanel;
    private JPanel homeWelcomePanel;

    /**
     * 채팅 홈 생성자
     * @param parentFrame 부모 클라이언트 프레임
     */
    public ChatHome(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 채팅 목록 패널 생성
        chatListPanel = new ChatPanel(parentFrame);
        // 환영 패널 생성
        createWelcomePanel();

        // 중앙 패널 설정
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(homeWelcomePanel, BorderLayout.CENTER);

        // 좌측 메뉴 패널과 중앙 패널 추가
        add(new MenuPanel(), BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 환영 패널 생성
     * 초기 화면에 표시되는 환영 메시지 패널
     */
    private void createWelcomePanel() {
        homeWelcomePanel = new JPanel();
        homeWelcomePanel.setLayout(new BoxLayout(homeWelcomePanel, BoxLayout.Y_AXIS));
        homeWelcomePanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("ChatPlay 홈");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 수직 중앙 정렬을 위한 여백 추가
        homeWelcomePanel.add(Box.createVerticalGlue());
        homeWelcomePanel.add(title);
        homeWelcomePanel.add(Box.createVerticalGlue());
    }

    /**
     * 채팅방 패널 표시
     * @param roomData 채팅방 데이터
     */
    public void showChatRoom(ChatRoomData roomData) {
        centerPanel.removeAll();
        centerPanel.add(new ChatRoomPanel(parentFrame, roomData), BorderLayout.CENTER);
        refresh();
    }

    /**
     * 채팅 목록 패널 복원
     */
    public void restoreChatList() {
        centerPanel.removeAll();
        centerPanel.add(chatListPanel, BorderLayout.CENTER);
        refresh();
    }

    /**
     * 프로필 패널 표시
     * 상태메시지 변경 등 프로필 정보가 업데이트될 때 호출됨
     */
    public void showProfilePanel() {
        centerPanel.removeAll();
        centerPanel.add(new ProfilePanel(parentFrame), BorderLayout.CENTER);
        refresh();
    }

    /**
     * 특정 방에 메시지 추가
     * @param roomId 방 ID
     * @param msg 채팅 메시지
     */
    public void appendMessageToRoom(int roomId, ChatMessage msg) {
        if (centerPanel.getComponentCount() > 0) {
            Component current = centerPanel.getComponent(0);
            // 현재 표시 중인 패널이 해당 방의 채팅방 패널이면 메시지 추가
            if (current instanceof ChatRoomPanel) {
                ChatRoomPanel panel = (ChatRoomPanel) current;
                if (panel.getRoomId() == roomId) {
                    panel.addBubble(msg);
                }
            }
        }
    }

    /**
     * 중앙 패널 갱신
     */
    private void refresh() {
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    /**
     * 프로필 패널 갱신
     * 상태메시지나 프로필 이미지 변경 시 호출되어 최신 정보 반영
     */
    public void refreshProfile() {
        showProfilePanel();
    }


    class MenuPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        /**
         * 메뉴 패널 생성자
         */
        public MenuPanel() {
            setLayout(new BorderLayout());
            setOpaque(false); // paintComponent로 배경 직접 칠할 거라 false

            setPreferredSize(new Dimension(80, 0));

            // ========== 메뉴 버튼 패널 ==========
            JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 10));
            btnPanel.setOpaque(false);
            btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 5, 0, 5));

            // 프로필 아이콘 로드
            ImageIcon iconProfile = loadIcon("/images/friend.png", 40, 40);
            ImageIcon iconProfileHover = loadIcon("/images/friend_hover.png", 40, 40);

            // 채팅 아이콘 로드
            ImageIcon iconChat = loadIcon("/images/chat.png", 40, 40);
            ImageIcon iconChatHover = loadIcon("/images/chat_hover.png", 40, 40);

            // 버튼 생성 및 스타일 적용
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

            // 프로필 버튼 클릭 시 프로필 패널 표시
            btnProfile.addActionListener(e -> {
                centerPanel.removeAll();
                centerPanel.add(new ProfilePanel(parentFrame), BorderLayout.CENTER);
                refresh();
            });

            // 채팅 버튼 클릭 시 채팅 목록 복원
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