package chatPlay;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.net.ConnectException;

public class ChatClient_Before extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9999;

    // 네트워킹 및 상태 관리
    private Socket socket;
    private BufferedReader serverIn;
    private BufferedWriter serverOut;
    private volatile boolean running = true;
    private String clientNickname = "";

    // GUI 컴포넌트
    private JList<Message> chatList;
    private DefaultListModel<Message> chatModel; // 채팅 메시지 데이터 모델
    private JTextField inputField;
    private JButton sendButton;
    private DefaultListModel<String> userListModel; // 접속자 목록 데이터
    private JLabel statusLabel;
    private JLabel typingStatusLabel;

    // 작성 중 상태 관리 타이머
    private Timer typingTimer;
    private static final int TYPING_TIMEOUT_MS = 1000;

    public ChatClient_Before() {
        super("ChatPlay - 채팅 클라이언트");
        userListModel = new DefaultListModel<>();
        initializeGUI();
        new Thread(this::initNetworking, "Net-Init-Thread").start();
    }

    private void initializeGUI() {
        // CENTER: 채팅, NORTH: 상태바, SOUTH: 입력창
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
        
        chatModel = new DefaultListModel<>();
        chatList = new JList<>(chatModel);
        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.setCellRenderer(new MessageCellRenderer("")); // 닉네임 설정 전 임시
        add(new JScrollPane(chatList), BorderLayout.CENTER);

        // 상단 상태바 및 멤버 보기 버튼
        JPanel northPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("연결 대기 중...", SwingConstants.CENTER);
        JButton memberButton = new JButton("멤버 보기");
        memberButton.addActionListener(e -> showMemberListPopup());
        northPanel.add(statusLabel, BorderLayout.CENTER);
        northPanel.add(memberButton, BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        // 하단 입력창 및 작성 중 레이블
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        inputField.addActionListener(new SendMessageListener());
        inputField.addKeyListener(new TypingListener());
        sendButton = new JButton("전송");
        sendButton.addActionListener(new SendMessageListener());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        typingStatusLabel = new JLabel("", SwingConstants.LEFT);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(typingStatusLabel, BorderLayout.NORTH);
        southPanel.add(inputPanel, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);

        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        
        setVisible(true);
    }

    // 서버 연결 및 닉네임 등록
    private void initNetworking() {
        try {
            statusLabel.setText("서버 (" + SERVER_ADDRESS + ":" + SERVER_PORT + ") 에 연결 시도 중...");
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            statusLabel.setText("서버 연결 성공! 닉네임을 등록하세요.");

            // 닉네임 등록 (루프)
            boolean nickAccepted = false;
            while (!nickAccepted && running) {
                String prompt = serverIn.readLine();
                if (prompt == null) break;
                
                String inputNick = (String) JOptionPane.showInputDialog(this, prompt.trim() + "\n(종료하려면 취소)", "닉네임 설정", JOptionPane.PLAIN_MESSAGE, null, null, clientNickname.isEmpty() ? "Guest" : clientNickname);
                
                if (inputNick == null) { running = false; break; }
                clientNickname = inputNick.trim();
                
                serverOut.write(clientNickname + "\n");
                serverOut.flush();

                String response = serverIn.readLine();
                if (response == null) break;

                if ("OK_NICK".equals(response)) {
                    nickAccepted = true;
                    setTitle("ChatPlay - " + clientNickname);
                    statusLabel.setText(clientNickname + " 님, 채팅에 참여했습니다!");
                    
                    chatList.setCellRenderer(new MessageCellRenderer(clientNickname)); // 최종 닉네임으 설정
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocusInWindow();
                    
                    new Thread(new ServerReader(), "Server-Reader-Thread").start(); // 메시지 수신 스레드 시작
                    
                } else if ("FAIL_NICK".equals(response)) {
                    JOptionPane.showMessageDialog(this, "에러: 닉네임 [" + clientNickname + "]은 이미 사용 중입니다.", "닉네임 중복", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: 서버가 실행 중인지 확인하세요.", "연결 오류", JOptionPane.ERROR_MESSAGE);
            running = false;
            statusLabel.setText("연결 실패");
        } catch (Exception e) {
            if (running) running = false;
        } finally {
            if (!running) {
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    // 서버로부터 메시지를 읽고 처리하는 스레드
    private class ServerReader implements Runnable {
        @Override
        public void run() {
            try {
                String line;
                while (running && (line = serverIn.readLine()) != null) {
                    final String receivedLine = line;
                    
                    SwingUtilities.invokeLater(() -> {
                        if (receivedLine.startsWith("[Server] OnlineUsers:")) {
                            updateUserList(receivedLine); // 접속자 목록 업데이트
                        } 
                        else if (receivedLine.startsWith("[Signal:")) {
                            handleSignal(receivedLine); // TYPING 신호 처리
                        }
                        else {
                            // 일반 메시지 처리 및 JList에 추가
                            String sender = "Server";
                            String content = receivedLine;
                            
                            if (receivedLine.startsWith("[") && receivedLine.indexOf("]") > 0) {
                                try {
                                    int endBracket = receivedLine.indexOf("]");
                                    sender = receivedLine.substring(1, endBracket);
                                    content = receivedLine.substring(endBracket + 2).trim();
                                } catch (Exception ignored) { /* 파싱 오류 무시 */ }
                            }
                            
                            chatModel.addElement(new Message(sender, content, sender.equals(clientNickname)));
                            chatList.ensureIndexIsVisible(chatModel.getSize() - 1);
                        }
                    });
                }
            } catch (IOException ignored) {
                // 연결이 끊겼을 때 발생
            } finally {
                running = false;
                SwingUtilities.invokeLater(() -> {
                    chatModel.addElement(new Message("System", "--- 서버와의 연결이 끊겼습니다. ---", false));
                    statusLabel.setText("연결 끊김");
                    inputField.setEnabled(false);
                    sendButton.setEnabled(false);
                });
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
    
    // 작성 중 신호 처리
    private void handleSignal(String signal) {
        try {
            int nickEnd = signal.indexOf("]");
            int typeStart = signal.indexOf("TYPING:");
            
            if (nickEnd == -1 || typeStart == -1) return;

            String senderNickname = signal.substring("[Signal:".length(), nickEnd);
            String status = signal.substring(typeStart);
            
            if (status.equals("TYPING:START")) {
                typingStatusLabel.setText(senderNickname + " 님이 작성 중입니다...");
            } else if (status.equals("TYPING:END")) {
                typingStatusLabel.setText("");
            }
            
        } catch (Exception ignored) {}
    }

    // 사용자 목록 업데이트
    private void updateUserList(String line) {
        String userString = line.substring("[Server] OnlineUsers:".length()).trim();
        String[] users = userString.split(",");
        
        userListModel.clear();
        for (String user : users) {
            if (!user.trim().isEmpty()) {
                userListModel.addElement(user.trim());
            }
        }
    }
    
    // 멤버 목록 팝업 표시
    private void showMemberListPopup() {
        JDialog dialog = new JDialog(this, "접속 멤버 목록", true);
        dialog.setSize(200, 300);
        dialog.setLocationRelativeTo(this);
        
        JList<String> popupList = new JList<>(userListModel);
        dialog.add(new JScrollPane(popupList), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // 메시지 전송 리스너
    private class SendMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                if ("bye".equalsIgnoreCase(message)) {
                    sendMessageToServer(message);
                    running = false;
                    dispose();
                    return;
                }
                
                sendMessageToServer(message);
                inputField.setText("");
            }
            inputField.requestFocusInWindow();
            
            // 전송 후 작성 중 상태 종료 신호 전송
            if (typingTimer != null) {
                typingTimer.stop();
            }
            sendMessageToServer("TYPING:END");
        }
    }
    
    // 키 입력 및 타이머 관리 (TYPING 신호 전송)
    private class TypingListener extends KeyAdapter {
        private boolean isTyping = false;
        
        @Override
        public void keyTyped(KeyEvent e) {
            if (!isTyping) {
                sendMessageToServer("TYPING:START");
                isTyping = true;
            }
            
            if (typingTimer != null && typingTimer.isRunning()) {
                typingTimer.restart();
            } else {
                typingTimer = new Timer(TYPING_TIMEOUT_MS, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessageToServer("TYPING:END");
                        isTyping = false;
                        typingTimer.stop();
                    }
                });
                typingTimer.setRepeats(false);
                typingTimer.start();
            }
        }
    }
    
    // 서버로 메시지를 전송하는 실제 로직
    private void sendMessageToServer(String message) {
        try {
            if (serverOut != null && running) {
                serverOut.write(message + "\n");
                serverOut.flush();
            }
        } catch (IOException ex) {running = false;}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient_Before::new);
    }

    // 1. 메시지 데이터
    static class Message {
        String sender;
        String content;
        boolean isMine;

        public Message(String sender, String content, boolean isMine) {
            this.sender = sender;
            this.content = content;
            this.isMine = isMine;
        }
    }

    // 2. 메시지 창
    private class MessageCellRenderer extends JPanel implements ListCellRenderer<Message> {
        
		private static final long serialVersionUID = 1L;
		public MessageCellRenderer(String myNickname) {
            setLayout(new BorderLayout(5, 5));
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Message> list,
                Message message,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            
            removeAll();
            setBackground(list.getBackground());

            // 메시지 내용 자동 줄바꿈
            JTextArea messageContent = new JTextArea(message.content);
            messageContent.setLineWrap(true);
            messageContent.setWrapStyleWord(true);
            messageContent.setEditable(false);
            
            // 메세지 색상 설정
            Color myBubble = new Color(254, 229, 0);
            Color otherBubble = Color.WHITE;
            
            JPanel bubblePanel = new JPanel(new BorderLayout());
            messageContent.setBackground(message.isMine ? myBubble : otherBubble);
            bubblePanel.setBackground(message.isMine ? myBubble : otherBubble);
            bubblePanel.add(messageContent);
            
            // 테두리 
            bubblePanel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(message.isMine ? myBubble : Color.LIGHT_GRAY, 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
            );
            
            // 닉네임 라벨 
            JLabel nicknameLabel = new JLabel(message.isMine || message.sender.equals("Server") ? "" : message.sender, SwingConstants.LEFT);
            JPanel contentContainer = new JPanel(new BorderLayout());
            contentContainer.setBackground(list.getBackground());
            
            if (message.sender.equals("Server") || message.sender.equals("System")) {
                // 시스템 메시지는 중앙 정렬 (일단 CENTER에 배치)
                contentContainer.add(bubblePanel, BorderLayout.CENTER);
                add(contentContainer, BorderLayout.CENTER);
            } else {
                // 일반/내 메시지
                contentContainer.add(nicknameLabel, BorderLayout.NORTH);
                contentContainer.add(bubblePanel, BorderLayout.CENTER);
                
                if (message.isMine) {
                    // 내 메시지: 오른쪽 정렬
                    add(Box.createHorizontalStrut(getWidth() / 3), BorderLayout.WEST); // 왼쪽에 여백
                    add(contentContainer, BorderLayout.EAST);
                } else {
                    // 상대방 메시지: 왼쪽 정렬
                    add(contentContainer, BorderLayout.WEST);
                    add(Box.createHorizontalStrut(getWidth() / 3), BorderLayout.EAST); // 오른쪽에 여백
                }
            }
            
            // 높이 자동 조절
            int preferredHeight = (int) contentContainer.getPreferredSize().getHeight() + 10;
            setPreferredSize(new Dimension(list.getWidth(), Math.max(40, preferredHeight)));
            
            return this;
        }
    }
}