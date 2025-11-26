package chatPlay;

import javax.swing.*;

public class ChatRoomFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private ChatRoomPanel panel;

    public ChatRoomFrame(ChatClientMain parent, ChatRoomData roomData) {
        setTitle(roomData.getRoomName());
        setSize(392, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panel = new ChatRoomPanel(parent, roomData);
        setContentPane(panel);
    }

    public void appendMessage(ChatMessage msg) {
        ChatRoomPanel panel = (ChatRoomPanel) getContentPane();
        panel.addBubble(msg);
    }
    public ChatRoomPanel getChatRoomPanel() {
        return panel;
    }
}
