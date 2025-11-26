package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserListDialog extends JDialog {

	public UserListDialog(Window owner,String roomName, List<String> users, Runnable onInvite, Runnable onExitRoom) {
		
		super(owner, "참여자 목록", ModalityType.APPLICATION_MODAL);
		
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
            userRow.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));

            JLabel nameLabel = new JLabel("   " + u);
            nameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

            userRow.add(nameLabel, BorderLayout.CENTER);
            listPanel.add(userRow);
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // ─────────────── BOTTOM BUTTONS ───────────────
        JPanel bottom = new JPanel(new GridLayout(1, 2));

        JButton inviteBtn = new JButton(new ImageIcon("src/images/addUser.png"));
        inviteBtn.setFocusPainted(false);
        inviteBtn.addActionListener(e -> {
            dispose();
            onInvite.run();
        });

        JButton exitBtn = new JButton(new ImageIcon("src/images/exit.png"));
        exitBtn.setFocusPainted(false);
        exitBtn.addActionListener(e -> {
            dispose();
            onExitRoom.run();
        });

        bottom.add(inviteBtn);
        bottom.add(exitBtn);

        add(bottom, BorderLayout.SOUTH);
    }
}
