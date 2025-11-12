package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ProfilePanel extends JPanel {

    private JLabel nameLabel; // 🔹 이름 라벨 참조 보관

    public ProfilePanel(String username) { // 🔹 외부에서 이름 받기
        setLayout(new BorderLayout());
        setOpaque(false); // 배경은 직접 그림으로 처리

        // ============================
        // 🔹 1. 프로필 영역
        // ============================
        JPanel profileBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        profileBox.setOpaque(false);
        profileBox.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); // 위·아래 여백 동일 (30px)

        // 프로필 이미지 로드
        ImageIcon profileIcon = null;
        try {
            profileIcon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
        } catch (Exception e) {
            System.err.println("⚠️ 이미지 불러오기 실패: " + e.getMessage());
        }

        JLabel imgLabel;
        if (profileIcon != null) {
            Image scaledImg = profileIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            imgLabel = new JLabel(new ImageIcon(scaledImg));
        } else {
            imgLabel = new JLabel();
            imgLabel.setPreferredSize(new Dimension(60, 60));
            imgLabel.setOpaque(true);
            imgLabel.setBackground(new Color(200, 210, 230));
        }

        // 이름 라벨
        nameLabel = new JLabel(username);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        nameLabel.setForeground(new Color(40, 40, 40));

        profileBox.add(imgLabel);
        profileBox.add(nameLabel);

        // 구분선
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

        // ============================
        // 🔹 2. 중앙 스크롤 영역 (친구)
        // ============================
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        for (int i = 1; i <= 100; i++) {
            JLabel item = new JLabel("친구 " + i);
            item.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            item.setForeground(new Color(60, 60, 80));
            item.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
            scrollContent.add(item);
        }

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0)); // 구분선 아래 동일 여백
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);
    }

    // 🔹 나중에 이름 변경하고 싶을 때 쓸 수 있음
    public void setUsername(String username) {
        nameLabel.setText(username);
    }

    // 🔹 배경 그라데이션
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
