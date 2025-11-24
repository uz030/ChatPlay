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

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        JButton btnCreate = new JButton("채팅방 생성 +");
        btnCreate.addActionListener(e -> createRoom());
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
        add(new JScrollPane(list), BorderLayout.CENTER);
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