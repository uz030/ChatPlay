package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        btnBack.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        });

        JLabel title = new JLabel(roomData.getRoomName(), SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
      
        JButton btnUsers = new JButton("👥");
        btnUsers.setContentAreaFilled(false);
        btnUsers.setBorderPainted(false);
        btnUsers.addActionListener(e -> {
            try {
                parent.getDos().writeUTF("/roomusers " + roomData.getRoomId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightBtns.setOpaque(false);
        rightBtns.add(btnUsers);
        top.add(btnBack, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);
        top.add(rightBtns, BorderLayout.EAST);
        
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
        ScrollUtil.applyCustomScrollBar(scrollPane);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

     // 3. 하단 입력창
        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        /* ─────────────── 파일 버튼 ─────────────── */
        JButton btnFile = new JButton(new ImageIcon("src/images/file.png"));
        btnFile.setBorderPainted(false);
        btnFile.setContentAreaFilled(false);
        btnFile.setFocusPainted(false);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filePanel.setOpaque(false);
        filePanel.add(btnFile);

        bottom.add(filePanel);
        bottom.add(Box.createHorizontalStrut(8));  

        /* ─────────────── 입력 박스 ─────────────── */
        JPanel inputBox = new JPanel(new BorderLayout());
        inputBox.setBackground(Color.WHITE);
        inputBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        inputBox.setPreferredSize(new Dimension(200, 40));

        inputField = new JTextField();
        inputField.setBorder(null);
        inputField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnEmoji = new JButton(new ImageIcon("src/images/emoticon.png"));
        btnEmoji.setBorderPainted(false);
        btnEmoji.setContentAreaFilled(false);
        btnEmoji.setFocusPainted(false);
        btnEmoji.setPreferredSize(new Dimension(40, 30));

        inputBox.add(inputField, BorderLayout.CENTER);
        inputBox.add(btnEmoji, BorderLayout.EAST);


        /* ─────────────── 전송 버튼 ─────────────── */
        JButton btnSend = new RoundedButton("전송", new Color(0, 149, 246), new Color(0, 120, 200), Color.WHITE);
        btnSend.setPreferredSize(new Dimension(70, 40));
        btnSend.setFocusPainted(false);
        
        inputField.addActionListener(e -> sendMsg());
        btnSend.addActionListener(e -> sendMsg());
        
        bottom.add(inputBox);
        bottom.add(Box.createHorizontalStrut(8)); 
        bottom.add(btnSend);

        add(bottom, BorderLayout.SOUTH);


        
        scrollToBottom();
    }

    public void addBubble(ChatMessage msg) {
    	ChatBubblePanel bubble = new ChatBubblePanel(msg, e -> onBotMenuClicked(e));


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
        if (txt.isEmpty()) return;

        try {
            // 1) 서버로 전송
            parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " " + txt);

            // 2) 입력창 초기화
            inputField.setText("");

            // ❌ 삭제해야 함 — 서버가 다시 보내기 때문에 두 번 뜸
            // ChatMessage selfMsg = new ChatMessage(parent.getMyProfile().getUsername(), txt, true);
            // addBubble(selfMsg);
            // roomData.getMessageLog().addElement(selfMsg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void inviteFriend() {
        Window owner = SwingUtilities.getWindowAncestor(this);

        UserSelectDialog dialog = new UserSelectDialog(owner, parent, false);
        dialog.setVisible(true);

        if (dialog.isOk()) {
            java.util.List<String> users = dialog.getSelectedUsers();
            for (String u : users) {
                try {
                    parent.getDos().writeUTF("/invite " + roomData.getRoomId() + " " + u);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    public void openInviteDialog() {
        inviteFriend();
    }

    private void onBotMenuClicked(ActionEvent e) {
        String menu = e.getActionCommand();
        int roomId = roomData.getRoomId();

        try {
            if (menu.equals("날씨")) {
            	parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " " + "날씨");
                parent.getDos().writeUTF("/bot weather " + roomId);
            }
            else if (menu.equals("오늘의 날씨")) {
                ChatRoomFrame frame = (ChatRoomFrame) SwingUtilities.getWindowAncestor(this);
                frame.switchToPanel(
                    new weather.TodayWeatherPage(() ->
                        frame.switchToPanel(frame.getChatRoomPanel())
                    )
                );
            }
            else if (menu.equals("7일 예보")) {
                ChatRoomFrame frame = (ChatRoomFrame) SwingUtilities.getWindowAncestor(this);
                frame.switchToPanel(
                    new weather.WeekWeatherPage(() ->
                        frame.switchToPanel(frame.getChatRoomPanel())
                    )
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    
    // 외부에서 룸 ID 확인용
    public int getRoomId() { return roomData.getRoomId(); }


    
}