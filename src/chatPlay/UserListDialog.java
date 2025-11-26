package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserListDialog extends JDialog {


	private static final long serialVersionUID = 1L;

	public UserListDialog(Window owner,String roomName, List<String> users, Runnable onInvite, Runnable onExitRoom) {
		
		super(owner, "👥 참여자 목록", ModalityType.APPLICATION_MODAL);
		
		setSize(300, 400);
		setLocationRelativeTo(owner);   // 🔥 이제 owner 기준으로 중앙!
		setLayout(new BorderLayout());
		setResizable(false);


        // ─────────────── USER LIST PANEL ───────────────
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        
        for (String u : users) {

            JPanel userRow = new JPanel(new BorderLayout());
            userRow.setBackground(Color.WHITE);

            int rowHeight = 45;

            userRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
            userRow.setPreferredSize(new Dimension(0, rowHeight));
            userRow.setMinimumSize(new Dimension(0, rowHeight));

            userRow.setAlignmentX(Component.LEFT_ALIGNMENT); 

            userRow.setBorder(BorderFactory.createLineBorder(new Color(180,180,180), 1));

            JLabel nameLabel = new JLabel("  " + u);
            nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

            userRow.add(nameLabel, BorderLayout.CENTER);

            listPanel.add(userRow);
        }
        
        JScrollPane scroll = new JScrollPane(listPanel);
        ScrollUtil.applyCustomScrollBar(scroll);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

     // ─────────────── BOTTOM BUTTONS ───────────────
        JPanel bottom = new JPanel(new GridLayout(1, 2));

        Color baseColor = new Color(200, 210, 255);
        Color hoverColor = new Color(170, 185, 255);
        Color textColor = new Color(40, 50, 80);

        // ====================== 초대 버튼 ======================
        JButton inviteBtn = new JButton(new ImageIcon("src/images/addUser.png"));
        inviteBtn.setFocusPainted(false);
        inviteBtn.setBorderPainted(false);
        inviteBtn.setOpaque(true);
        inviteBtn.setBackground(baseColor);
        inviteBtn.setForeground(textColor);

        // hover 효과
        inviteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                inviteBtn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                inviteBtn.setBackground(baseColor);
            }
        });

        // 클릭 액션
        inviteBtn.addActionListener(e -> {
            dispose();
            onInvite.run();
        });

        // ====================== 나가기 버튼 ======================
        JButton exitBtn = new JButton(new ImageIcon("src/images/exit.png"));
        exitBtn.setFocusPainted(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setOpaque(true);
        exitBtn.setBackground(baseColor);
        exitBtn.setForeground(textColor);

        // hover 효과
        exitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                exitBtn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                exitBtn.setBackground(baseColor);
            }
        });

        // 클릭 액션
        exitBtn.addActionListener(e -> {
            dispose();
            onExitRoom.run();
        });

        bottom.add(inviteBtn);
        bottom.add(exitBtn);

        add(bottom, BorderLayout.SOUTH);

    }
}
