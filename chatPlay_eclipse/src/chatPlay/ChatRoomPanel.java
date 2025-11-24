package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChatRoomPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parent;
    private ChatRoomData roomData;
    private JPanel chatContentPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;

    public ChatRoomPanel(ChatClientMain parent, ChatRoomData roomData) {
        this.parent = parent;
        this.roomData = roomData;
        setLayout(new BorderLayout());

        // 1. 상단 (뒤로가기, 제목, 초대)
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230,230,230)));

        JButton btnBack = new JButton(" < ");
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(e -> parent.switchToHome());

        JLabel title = new JLabel(roomData.getRoomName(), SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        JButton btnInvite = new JButton("초대 +");
        btnInvite.setBackground(new Color(240, 240, 240));
        btnInvite.addActionListener(e -> inviteFriend());

        top.add(btnBack, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(btnInvite, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // 2. 중앙 채팅 영역
        chatContentPanel = new JPanel();
        chatContentPanel.setLayout(new BoxLayout(chatContentPanel, BoxLayout.Y_AXIS));
        chatContentPanel.setBackground(Color.WHITE);

        DefaultListModel<ChatMessage> logs = roomData.getMessageLog();
        for(int i=0; i<logs.getSize(); i++) {
            addBubble(logs.getElementAt(i));
        }

        scrollPane = new JScrollPane(chatContentPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // 3. 하단 입력창
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        inputField = new JTextField();
        JButton btnSend = new RoundedButton("전송", new Color(0, 149, 246), new Color(0, 120, 200), Color.WHITE);
        btnSend.setPreferredSize(new Dimension(70, 40));
        
        inputField.addActionListener(e -> sendMsg());
        btnSend.addActionListener(e -> sendMsg());

        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        bottom.add(btnSend, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        
        scrollToBottom();
    }

    public void addBubble(ChatMessage msg) {
        ChatBubblePanel bubble = new ChatBubblePanel(msg);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(bubble, BorderLayout.CENTER);
        
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));

        chatContentPanel.add(wrapper);
        
        chatContentPanel.add(Box.createVerticalStrut(2)); 

        chatContentPanel.revalidate();
        chatContentPanel.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void sendMsg() {
        String txt = inputField.getText().trim();
        if(txt.isEmpty()) return;
        try {
            parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " " + txt);
            inputField.setText("");
        } catch(Exception e) {}
    }

    private void inviteFriend() {
        UserSelectDialog dialog = new UserSelectDialog(parent);
        dialog.setVisible(true);
        if(dialog.isOk()) {
            List<String> users = dialog.getSelectedUsers();
            for(String u : users) {
                try { parent.getDos().writeUTF("/invite " + roomData.getRoomId() + " " + u); }
                catch(Exception e) {}
            }
        }
    }
    
    // 외부에서 룸 ID 확인용
    public int getRoomId() { return roomData.getRoomId(); }
}