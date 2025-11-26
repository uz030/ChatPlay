package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserListDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public UserListDialog(Window owner, String roomName, List<UserProfile> users, Runnable onInvite, Runnable onExitRoom) {
        
        super(owner, "👥 참여자 목록", ModalityType.APPLICATION_MODAL);
        
        setSize(300, 450); 
        setLocationRelativeTo(owner);   
        setLayout(new BorderLayout());
        setResizable(false);

        // ─────────────── USER LIST PANEL ───────────────
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (UserProfile u : users) {

            JPanel userRow = new JPanel(new BorderLayout(10, 0));
            userRow.setBackground(Color.WHITE);

            int rowHeight = 50;

            userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
            userRow.setPreferredSize(new Dimension(0, rowHeight));
            userRow.setMinimumSize(new Dimension(0, rowHeight));
            userRow.setAlignmentX(Component.LEFT_ALIGNMENT); 
            
            // 프로필 사진
            ImageIcon icon = u.getIcon();
            if (icon == null) {
                try {
                    icon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
                } catch(Exception e) {}
            }
            
            JLabel iconLabel = new JLabel();
            if (icon != null) {
                Image img = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
            
            JLabel nameLabel = new JLabel(u.getUsername());
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            nameLabel.setForeground(new Color(50, 50, 50));

            userRow.add(iconLabel, BorderLayout.WEST);
            userRow.add(nameLabel, BorderLayout.CENTER);
            
            // 구분선
            userRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

            listPanel.add(userRow);
            listPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        ScrollUtil.applyCustomScrollBar(scrollPane);
        
        add(scrollPane, BorderLayout.CENTER);

        // ─────────────── BOTTOM BUTTON PANEL ───────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottom.setBackground(new Color(245, 245, 255));
        bottom.setPreferredSize(new Dimension(0, 70)); 

        Color baseColor = new Color(220, 230, 255);
        Color hoverColor = new Color(200, 215, 255);
        Color textColor = new Color(50, 60, 90);

        // ====================== [수정] 초대 버튼 (addUser.png) ======================
        JButton inviteBtn = new JButton("초대");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/addUser.png"));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            inviteBtn.setIcon(new ImageIcon(img));
        } catch(Exception e) {
            // 이미지 없으면 텍스트만 표시
        }
        
        inviteBtn.setFocusPainted(false);
        inviteBtn.setBorderPainted(false);
        inviteBtn.setOpaque(true);
        inviteBtn.setBackground(new Color(100, 150, 255)); 
        inviteBtn.setForeground(Color.WHITE);
        inviteBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        inviteBtn.setPreferredSize(new Dimension(110, 40));
        inviteBtn.setIconTextGap(8); 

        inviteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { inviteBtn.setBackground(new Color(80, 130, 240)); }
            public void mouseExited(java.awt.event.MouseEvent e) { inviteBtn.setBackground(new Color(100, 150, 255)); }
        });

        inviteBtn.addActionListener(e -> {
            dispose();
            onInvite.run();
        });

        // ====================== [수정] 나가기 버튼 (exit.png) ======================
        JButton exitBtn = new JButton("나가기");
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/exit.png"));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            exitBtn.setIcon(new ImageIcon(img));
        } catch(Exception e) {
            // 이미지 없으면 텍스트만 표시
        }
        
        exitBtn.setFocusPainted(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setOpaque(true);
        exitBtn.setBackground(baseColor);
        exitBtn.setForeground(textColor);
        exitBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        exitBtn.setPreferredSize(new Dimension(110, 40));
        exitBtn.setIconTextGap(8);

        exitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { exitBtn.setBackground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent e) { exitBtn.setBackground(baseColor); }
        });

        exitBtn.addActionListener(e -> {
            dispose();
            onExitRoom.run();
        });

        bottom.add(inviteBtn);
        bottom.add(exitBtn);

        add(bottom, BorderLayout.SOUTH);
    }
}