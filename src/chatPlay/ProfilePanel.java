package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 프로필 화면 패널
 * 내 프로필과 친구 목록을 표시하며, 상태메시지 변경 시 자동 갱신됨
 */
public class ProfilePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * 프로필 패널 생성
     * @param parent ChatClientMain 부모 프레임
     */
    public ProfilePanel(ChatClientMain parent) {
        setLayout(new BorderLayout());
        setOpaque(false);
        UserProfile myProfile = parent.getMyProfile();

        // 1. 상단: 내 프로필 영역 (사진 + 이름 + 상태메시지)
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileBox.setOpaque(false);
        profileBox.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        profileBox.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 내 프로필 클릭 시 수정 다이얼로그 열기
        profileBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ProfileDetailDialog dialog = new ProfileDetailDialog(parent, myProfile, true);
                dialog.setVisible(true);
                // 다이얼로그 닫힌 후 변경된 정보 반영을 위해 패널 재생성
                removeAll();
                add(new ProfilePanel(parent));
                revalidate();
            }
        });

        // 내 프로필 사진
        JLabel imgLabel = new JLabel(getScaledIcon(myProfile.getIcon(), 60, 60));
        
        // 이름과 상태메시지를 수직으로 배치하기 위한 패널
        JPanel myInfoPanel = new JPanel(new GridLayout(2, 1));
        myInfoPanel.setOpaque(false);
        
        // 사용자명 표시
        JLabel nameLabel = new JLabel(myProfile.getUsername());
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        // 상태메시지 표시 (null 체크 후 빈 문자열로 처리)
        String myStatus = myProfile.getStatusMessage();
        if (myStatus == null) myStatus = "";
        JLabel statusLabel = new JLabel(myStatus);
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);

        myInfoPanel.add(nameLabel);
        myInfoPanel.add(statusLabel);

        profileBox.add(imgLabel);
        profileBox.add(myInfoPanel);

        add(profileBox, BorderLayout.NORTH);


        // 2. 중앙: 친구 목록 (JList + 커스텀 렌더러)
        JList<UserProfile> friendList = new JList<>(parent.getUserListModel());
        
        // 사진까지 보여주는 커스텀 렌더러 장착
        friendList.setCellRenderer(new ProfileListRenderer());
        
        // 친구 더블클릭 시 상세 프로필 보기
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

    /**
     * 이미지 크기 조절 헬퍼 메서드
     * @param src 원본 이미지 아이콘
     * @param w 목표 너비
     * @param h 목표 높이
     * @return 크기 조절된 이미지 아이콘
     */
    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) {
            return new ImageIcon(); 
        }
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(230, 225, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * 친구 목록의 각 항목을 렌더링하는 커스텀 셀 렌더러
     * 프로필 사진, 이름, 상태메시지를 표시
     */
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
            
            // 이름과 상태메시지 패널
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
            // 1. 프로필 사진 설정 (없으면 기본 이미지 사용)
            ImageIcon icon = value.getIcon();
            if (icon == null) {
                try {
                    icon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
                } catch (Exception e) {
                    // 기본 이미지 로드 실패 시 빈 아이콘 유지
                }
            }
            iconLabel.setIcon(icon != null ? getScaledIcon(icon, 45, 45) : null);

            // 2. 사용자명과 상태메시지 설정
            nameLabel.setText(value.getUsername());
            String status = value.getStatusMessage();
            statusLabel.setText((status == null || status.isEmpty()) ? " " : status);

            // 3. 선택 상태에 따른 배경색 변경
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