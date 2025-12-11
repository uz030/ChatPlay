package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public class ChatRoomPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parent;
    private ChatRoomData roomData;
    private JPanel chatContentPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    
    private JLayeredPane layeredChatPanel;
    private Emoji emoji;
    private boolean emojiOpen = false;
    
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

        // 2. 중앙 채팅 영역 - JLayeredPane 사용
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
        
        layeredChatPanel = new JLayeredPane();
        add(layeredChatPanel, BorderLayout.CENTER);
        
        layeredChatPanel.add(scrollPane);
        layeredChatPanel.setLayer(scrollPane, JLayeredPane.DEFAULT_LAYER); 
        
        layeredChatPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                
                scrollPane.setBounds(0, 0, layeredChatPanel.getWidth(), layeredChatPanel.getHeight());
               
                if (emoji != null && emoji.isVisible()) {
                    updateEmojiPanelLocation();
                }
            }
        });


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

        btnFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "이미지 파일", "jpg", "jpeg", "png", "gif", "bmp"
            ));
            
            int result = chooser.showOpenDialog(ChatRoomPanel.this);
            if (result != JFileChooser.APPROVE_OPTION) return;
            
            File file = chooser.getSelectedFile();
            if (file == null || !file.exists()) {
                JOptionPane.showMessageDialog(ChatRoomPanel.this,
                    "파일을 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 파일 크기 제한 (5MB)
            long maxSize = 5 * 1024 * 1024;
            if (file.length() > maxSize) {
                JOptionPane.showMessageDialog(ChatRoomPanel.this,
                    "파일 크기가 너무 큽니다. (최대 5MB)", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // ✅ 고유한 파일명 생성 (시간 + 원본파일명)
                String uniqueFileName = System.currentTimeMillis() + "_" + file.getName();
                
                // ✅ 파일 전송 (파일 데이터 + 메타정보)
                byte[] fileData = java.nio.file.Files.readAllBytes(file.toPath());
                
                // 서버로 파일 업로드 명령
                parent.getDos().writeUTF("/upload_image " + roomData.getRoomId() + " " + uniqueFileName);
                parent.getDos().writeInt(fileData.length);
                parent.getDos().write(fileData);
                parent.getDos().flush();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ChatRoomPanel.this,
                    "이미지 전송에 실패했습니다.\n" + ex.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

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
        
        btnEmoji.addActionListener(e -> {
        	
        	if (emoji == null) {
        		emoji = new Emoji(
                        this,
                        parent,
                        roomData
                );
                // JLayeredPane의 POPUP_LAYER에 추가
                layeredChatPanel.add(emoji);
                layeredChatPanel.setLayer(emoji, JLayeredPane.POPUP_LAYER); // 👈 수정됨
                
                emoji.setVisible(false);
                emoji.setSize(emoji.getPreferredSize());
            }
        	
        	emojiOpen = !emojiOpen;

            if (emojiOpen) {
                updateEmojiPanelLocation();
                emoji.setVisible(true);

            } else {
            	emoji.setVisible(false);
            }
            
            layeredChatPanel.revalidate();
            layeredChatPanel.repaint();
        });
        
        
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
    
    
    private void updateEmojiPanelLocation() {
        if (emoji == null) return;
        
        Dimension dlgSize = emoji.getPreferredSize();
        
       
        int targetX = layeredChatPanel.getWidth() - dlgSize.width - 10; 
        
      
        int targetY = layeredChatPanel.getHeight() - dlgSize.height; 
        
        emoji.setBounds(targetX, targetY, dlgSize.width, dlgSize.height);
    }
    

    public void addBubble(ChatMessage msg) {
        
    	if (msg.getContent().startsWith("@imagefile ")) {
            String fileName = msg.getContent().substring(11).trim();
            File imageFile = new File("shared_images/" + fileName);
            
            if (imageFile.exists()) {
                try {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    
                    // 이미지 리사이징
                    Image scaledImage = icon.getImage().getScaledInstance(
                        300, 300, Image.SCALE_SMOOTH
                    );
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    
                    msg.setType(ChatMessage.MessageType.IMAGE);
                    msg.setFileName(fileName);
                    msg.setImageIcon(scaledIcon);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JLabel errorLabel = new JLabel("[이미지 로드 실패: " + fileName + "]");
                    errorLabel.setForeground(Color.RED);
                    chatContentPanel.add(errorLabel);
                    chatContentPanel.revalidate();
                    chatContentPanel.repaint();
                    scrollToBottom();
                    return;
                }
            }
        }
    	
        // 게임 참여 UI 처리 (GAME_JOIN)
        if (msg.getContent().startsWith("GAME_JOIN:")) {
            final String gameType = msg.getContent().substring(10).trim();
            
            JPanel joinPanel = new JPanel();
            joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.Y_AXIS));
            joinPanel.setBackground(new Color(255, 250, 240));
            joinPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 100), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            joinPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            
            // 게임 타입에 따라 제목과 색상 변경
            String gameTitle;
            Color borderColor;
            if ("yacht".equals(gameType)) {
                gameTitle = "🎲 요트다이스 게임";
                borderColor = new Color(100, 200, 255);
            } else {
                gameTitle = "🎮 캐치마인드 게임";
                borderColor = new Color(255, 200, 100);
            }
            
            joinPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            JLabel titleLabel = new JLabel(gameTitle);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel infoLabel = new JLabel("참여하시려면 아래 버튼을 눌러주세요!");
            infoLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // 참여하기 버튼
            JButton joinBtn = new JButton("게임 참여하기");
            joinBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            joinBtn.setBackground(new Color(100, 200, 255));
            joinBtn.setForeground(Color.WHITE);
            joinBtn.setFocusPainted(false);
            joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            joinBtn.setMaximumSize(new Dimension(200, 40));
            
            joinBtn.addActionListener(e -> {
                try {
                    if ("yacht".equals(gameType)) {
                        parent.getDos().writeUTF("/yacht_join " + roomData.getRoomId());
                        parent.openYachtFrame(roomData.getRoomId());
                    } else {
                        parent.getDos().writeUTF("/catchmind_join " + roomData.getRoomId());
                        parent.openCatchMindFrame(roomData.getRoomId());
                    }
                    joinBtn.setEnabled(false);
                    joinBtn.setText("참여 완료!");
                    joinBtn.setBackground(Color.GRAY);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            
            // 게임 시작 버튼 (한 번만 누르면 모두에게 게임 창 열림)
            JButton startBtn = new JButton("게임 시작 (2명 이상)");
            startBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            startBtn.setBackground(new Color(255, 100, 100));
            startBtn.setForeground(Color.WHITE);
            startBtn.setFocusPainted(false);
            startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            startBtn.setMaximumSize(new Dimension(200, 40));
            
            startBtn.addActionListener(e -> {
                try {
                    if ("yacht".equals(gameType)) {
                        parent.getDos().writeUTF("/yacht_start " + roomData.getRoomId());
                    } else {
                        parent.getDos().writeUTF("/catchmind_start " + roomData.getRoomId());
                    }
                    // ✅ 버튼 비활성화
                    startBtn.setEnabled(false);
                    joinBtn.setEnabled(false);
                    startBtn.setText("게임 시작됨");
                    startBtn.setBackground(Color.GRAY);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            
            joinPanel.add(titleLabel);
            joinPanel.add(Box.createVerticalStrut(10));
            joinPanel.add(infoLabel);
            joinPanel.add(Box.createVerticalStrut(15));
            joinPanel.add(joinBtn);
            joinPanel.add(Box.createVerticalStrut(10));
            joinPanel.add(startBtn);
            
            chatContentPanel.add(joinPanel);
            chatContentPanel.add(Box.createVerticalStrut(10));
            
            chatContentPanel.revalidate();
            chatContentPanel.repaint();
            scrollToBottom();
            return;
        }
        
        // 게임 시작 알림 처리 (버튼 비활성화용)
        if (msg.getContent().startsWith("GAME_STARTED:")) {
            JLabel startedLabel = new JLabel("🎮 게임이 시작되었습니다!");
            startedLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            startedLabel.setForeground(new Color(0, 150, 0));
            startedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JPanel startedPanel = new JPanel();
            startedPanel.setBackground(new Color(240, 255, 240));
            startedPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            startedPanel.add(startedLabel);
            
            chatContentPanel.add(startedPanel);
            chatContentPanel.add(Box.createVerticalStrut(10));
            
            chatContentPanel.revalidate();
            chatContentPanel.repaint();
            scrollToBottom();
            return;
        }
        
        // === 기존 버블 처리 (일반 메시지) ===
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
        String menu = e.getActionCommand(); // 클릭된 버튼의 텍스트
        int roomId = roomData.getRoomId();

        try {
            // 1. 날씨 메인 버튼
            if (menu.equals("날씨")) {
                parent.getDos().writeUTF("/roommsg " + roomId + " " + "날씨");
                parent.getDos().writeUTF("/bot weather " + roomId);
            }
            // 2. 뉴스 메인 버튼 
            else if (menu.equals("뉴스")) {
                parent.getDos().writeUTF("/roommsg " + roomId + " " + "뉴스");
                parent.getDos().writeUTF("/bot news " + roomId);
            }
            // 3. 날씨 상세 메뉴 처리
            else if (menu.equals("오늘의 날씨")) {
                ChatRoomFrame frame = (ChatRoomFrame) SwingUtilities.getWindowAncestor(this);
                frame.switchToPanel(
                    new weather.TodayWeatherPage(() -> frame.switchToPanel(frame.getChatRoomPanel()))
                );
            }
            
            else if (menu.equals("7일 예보")) {
                ChatRoomFrame frame = (ChatRoomFrame) SwingUtilities.getWindowAncestor(this);
                frame.switchToPanel(
                    new weather.WeekWeatherPage(() -> frame.switchToPanel(frame.getChatRoomPanel()))
                );
            }
            else if (isNewsCategory(menu)) {
                ChatRoomFrame frame = (ChatRoomFrame) SwingUtilities.getWindowAncestor(this);
                
                frame.switchToPanel(
                    new news.NewsPage(menu, () -> frame.switchToPanel(frame.getChatRoomPanel()))
                );
            }
            
            else if (menu.equals("게임")) {
                parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " 게임");
                parent.getDos().writeUTF("/bot game " + roomData.getRoomId());
            }
            else if (menu.equals("캐치마인드")) {
                // 캐치마인드 실행 로직
                parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " 캐치마인드");
                parent.getDos().writeUTF("/bot catchmind " + roomId);
            }
            else if (menu.equals("캐치마인드 참여하기")) {
            	parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " 캐치마인드 참여하기");
            	parent.getDos().writeUTF("/catchmind_join " + roomId);
            	parent.openCatchMindFrame(roomId);
                
            }
            else if (menu.equals("요트다이스")) {
                // 요트다이스 실행 로직
                parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " 요트다이스");
                parent.getDos().writeUTF("/bot yacht " + roomId);
            }
            else if (menu.equals("요트다이스 참여하기")) {
                parent.getDos().writeUTF("/roommsg " + roomData.getRoomId() + " 요트다이스 참여하기");
                parent.getDos().writeUTF("/yacht_join " + roomId);
                parent.openYachtFrame(roomId);
            }



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isNewsCategory(String menu) {
        String[] categories = {"속보", "정치", "경제", "사회", "세계", "IT/과학"};
        for (String c : categories) {
            if (c.equals(menu)) return true;
        }
        return false;
    }
    
    public void closeEmoji() {
        if (emoji != null && emoji.isVisible()) {
        	emoji.setVisible(false);
            emojiOpen = false;
        }
    }


    public int getRoomId() { return roomData.getRoomId(); }

    
}