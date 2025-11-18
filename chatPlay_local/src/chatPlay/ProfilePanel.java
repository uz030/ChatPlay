package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilePanel extends JPanel {

    private JLabel nameLabel;
    private JLabel imgLabel; // 내 프로필 이미지 (클릭용)
    private JPanel profileBox; // 내 프로필 영역 (클릭용)
    private JList<UserProfile> friendList;

    public ProfilePanel(ChatClientMain parentFrame) {
        setLayout(new BorderLayout());
        setOpaque(false);

        // 내 프로필 정보 가져오기
        UserProfile myProfile = parentFrame.getMyProfile();

        // 1. 프로필 영역 (내 정보)
        profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileBox.setOpaque(false);
        profileBox.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        profileBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileBox.setToolTipText("클릭하여 내 프로필 수정하기");

        // 내 프로필 이미지
        imgLabel = new JLabel();
        updateMyProfileIcon(myProfile.getIcon()); // 🔹 아이콘 설정 헬퍼
        profileBox.add(imgLabel);

        // 내 이름
        nameLabel = new JLabel(myProfile.getUsername());
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        nameLabel.setForeground(new Color(40, 40, 40));
        profileBox.add(nameLabel);

        // 내 프로필 클릭 리스너
        MouseAdapter myProfileListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // '내 프로필' (수정 모드) 열기
                ProfileDetailDialog dialog = new ProfileDetailDialog(parentFrame, myProfile, true);
                dialog.setVisible(true);

                // 다이얼로그가 닫힌 후 만약 정보가 변경되었다면, 이 화면에도 반영
                updateMyProfileIcon(myProfile.getIcon());
                nameLabel.setText(myProfile.getUsername()); // 이름 변경 기능은 아직 없지만 추후 대비
            }
        };
        profileBox.addMouseListener(myProfileListener);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Color.BLACK);
        separator.setPreferredSize(new Dimension(0, 2));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.add(profileBox);
        topPanel.add(separator);
        add(topPanel, BorderLayout.NORTH);


        // 2. 중앙 스크롤 영역 (친구 목록)
        DefaultListModel<UserProfile> userListModel = parentFrame.getUserListModel();

        // JList<UserProfile>로 생성
        friendList = new JList<>(userListModel);
        friendList.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        friendList.setForeground(new Color(60, 60, 80));
        friendList.setOpaque(false);
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // (TODO: [2단계] CellRenderer를 만들면 친구 목록에 아이콘도 표시 가능)

        // 친구 목록 클릭 리스너
        friendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 🔹 더블 클릭 시
                    int index = friendList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        UserProfile selectedFriend = userListModel.getElementAt(index);

                        // '친구 프로필' (보기 모드) 열기
                        ProfileDetailDialog dialog = new ProfileDetailDialog(parentFrame, selectedFriend, false);
                        dialog.setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(friendList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);
    }

    // (Helper) 내 프로필 아이콘 업데이트
    private void updateMyProfileIcon(ImageIcon icon) {
        if (icon != null) {
            Image scaledImg = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledImg));
        } else {
            imgLabel.setPreferredSize(new Dimension(60, 60));
            imgLabel.setOpaque(true);
            imgLabel.setBackground(new Color(200, 210, 230));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color topColor = new Color(230, 225, 255);
        Color bottomColor = new Color(255, 255, 255);
        GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}