package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ChatPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parent;

    public ChatPanel(ChatClientMain parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);

     // 상단  패널
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(207, 220, 252), 
                        getWidth(), 0, new Color(199, 212, 247) 
                );

                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        top.setOpaque(false);

        ImageIcon iconDefault = new ImageIcon("src/images/chatPlus.png");
        ImageIcon iconHover   = new ImageIcon("src/images/chatPlus_hover.png");
        ImageIcon iconPressed = new ImageIcon("src/images/chatPlus_pressed.png");

        JButton btnCreate = new JButton();
        btnCreate.setIcon(iconDefault);
        btnCreate.setRolloverIcon(iconHover);
        btnCreate.setPressedIcon(iconPressed);

        btnCreate.setBorderPainted(false);
        btnCreate.setContentAreaFilled(false);
        btnCreate.setFocusPainted(false);
        btnCreate.setOpaque(false);

        btnCreate.setPreferredSize(new Dimension(30, 30));

        btnCreate.setToolTipText("채팅방 생성");
        btnCreate.addActionListener(e -> createRoom());
        btnCreate.setMargin(new Insets(0, 0, 0, 0));
        btnCreate.setPreferredSize(new Dimension(24, 24));
        btnCreate.setMaximumSize(new Dimension(24, 24));
        btnCreate.setMinimumSize(new Dimension(24, 24));
        
        top.add(btnCreate);

        add(top, BorderLayout.NORTH);

        JList<ChatRoomData> list = new JList<>(parent.getRoomListModel());
        list.setFixedCellHeight(50);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ChatRoomData room = list.getSelectedValue();
                    if (room != null) parent.enterChatRoom(room);
                }
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        ScrollUtil.applyCustomScrollBar(scroll);
        add(scroll, BorderLayout.CENTER);
        
    }

    private void createRoom() {
        UserSelectDialog dialog = new UserSelectDialog(parent);
        dialog.setVisible(true);
        if (dialog.isOk()) {
            List<String> selected = dialog.getSelectedUsers();
            String roomName = parent.getMyProfile().getUsername();
            for (String u : selected) roomName += "," + u;
            StringBuilder cmd = new StringBuilder("/makeroom " + roomName);
            for (String u : selected) cmd.append(" ").append(u);
            try { parent.getDos().writeUTF(cmd.toString()); } catch (Exception e) {}
        }
    }

}