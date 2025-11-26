package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public ProfilePanel(ChatClientMain parent) {
        setLayout(new BorderLayout());
        setOpaque(false);
        UserProfile myProfile = parent.getMyProfile();

        // --- 상단: 내 프로필 ---
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileBox.setOpaque(false);
        profileBox.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        profileBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        profileBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new ProfileDetailDialog(parent, myProfile, true).setVisible(true);
                removeAll();
                add(new ProfilePanel(parent));
                revalidate();
            }
        });

        // 내 프로필: 사진 + 이름
        JLabel imgLabel = new JLabel(getScaledIcon(myProfile.getIcon(), 60, 60));
        
        // 이름 + 상태메시지를 수직으로 쌓기 위한 패널
        JPanel myInfoPanel = new JPanel(new GridLayout(2, 1));
        myInfoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(myProfile.getUsername());
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        JLabel statusLabel = new JLabel(myProfile.getStatusMessage());
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);

        myInfoPanel.add(nameLabel);
        myInfoPanel.add(statusLabel);

        profileBox.add(imgLabel);
        profileBox.add(myInfoPanel);

        add(profileBox, BorderLayout.NORTH);

        // --- 중앙: 친구 목록 (JList) ---
        JList<UserProfile> friendList = new JList<>(parent.getUserListModel());
        
        // 커스텀 렌더러 장착 (사진 + 이름 + 한줄소개)
        friendList.setCellRenderer(new ProfileListRenderer());
        
        friendList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    UserProfile friend = friendList.getSelectedValue();
                    if (friend != null)
                        new ProfileDetailDialog(parent, friend, false).setVisible(true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(friendList);
        ScrollUtil.applyCustomScrollBar(scroll);

        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }

    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) return new ImageIcon();
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(230, 225, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    // 친구 목록 아이템을 그리는 렌더러 클래스
    class ProfileListRenderer extends JPanel implements ListCellRenderer<UserProfile> {
        
		private static final long serialVersionUID = 1L;
		private JLabel iconLabel;
        private JLabel nameLabel;
        private JLabel statusLabel;

        public ProfileListRenderer() {
            setLayout(new BorderLayout(15, 0)); 
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); 

            iconLabel = new JLabel();
            
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4)); 
            textPanel.setOpaque(false);
            
            nameLabel = new JLabel();
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            
            statusLabel = new JLabel();
            statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            statusLabel.setForeground(Color.GRAY);

            textPanel.add(nameLabel);
            textPanel.add(statusLabel);

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends UserProfile> list, UserProfile value, int index, boolean isSelected, boolean cellHasFocus) {
            // 데이터 설정
            iconLabel.setIcon(getScaledIcon(value.getIcon(), 45, 45)); 
            nameLabel.setText(value.getUsername());
            
            String status = value.getStatusMessage();
            if (status == null || status.isEmpty()) status = " "; 
            statusLabel.setText(status);

            // 선택 효과
            if (isSelected) {
                setBackground(new Color(230, 240, 255));
                setOpaque(true);
            } else {
                setOpaque(false);
            }
            
            return this;
        }
    }
}