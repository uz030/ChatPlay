package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatClientMain extends JFrame {

	private static final long serialVersionUID = 1L;
	private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainPanel mainPanel;
    private ChatHome homePanel;

    // 네트워크
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 30000;

    // 데이터 모델
    private UserProfile myProfile;
    private DefaultListModel<UserProfile> userListModel;
    private DefaultListModel<ChatRoomData> roomListModel;
    private Map<Integer, ChatRoomData> roomMap;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ChatClientMain().setVisible(true));
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Program");
        setSize(392, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // 데이터 초기화
        userListModel = new DefaultListModel<>();
        roomListModel = new DefaultListModel<>();
        roomMap = new HashMap<>();

        // 화면 구성
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        setContentPane(mainContainer);

        mainPanel = new MainPanel(this);
        homePanel = new ChatHome(this);

        mainContainer.add(mainPanel, "Main");
        mainContainer.add(homePanel, "Home");
        cardLayout.show(mainContainer, "Main");
    }

    // 서버 연결 및 로그인
    public void connectToServer(String username, ImageIcon icon) {
        try {
            System.out.println("서버 연결 시도 중... " + SERVER_IP + ":" + SERVER_PORT);
            socket = new Socket(SERVER_IP, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            System.out.println("로그인 정보 전송 중: " + username);
            dos.writeUTF("/login " + username);
            dos.flush();

            // 내 프로필 생성
            this.myProfile = new UserProfile(username, icon);

            // 리스너 스레드 시작
            new Thread(this::listenToServer).start(); 
            
            System.out.println("홈 화면으로 전환합니다.");
            switchToHome(); 

        } catch (Exception ex) {
            ex.printStackTrace(); 
            JOptionPane.showMessageDialog(this, 
                "서버 연결에 실패했습니다.\n" + ex.getMessage(), 
                "연결 오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // 서버 메시지 수신 루프
    private void listenToServer() {
        try {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("Server: " + msg);

                if (msg.startsWith("/userlist ")) { 
                    String[] users = msg.substring(10).split(",");
                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear();
                        for (String u : users) {
                            String name = u.trim();
                            if (!name.isEmpty() && !name.equals(myProfile.getUsername())) {
                                userListModel.addElement(new UserProfile(name));
                            }
                        }
                    });
                } else if (msg.startsWith("/roomCreated ")) {
                    String[] parts = msg.split(" ", 3);
                    int rId = Integer.parseInt(parts[1]);
                    String rName = parts[2];

                    ChatRoomData newRoom = new ChatRoomData(rId, rName);
                    roomMap.put(rId, newRoom);
                    SwingUtilities.invokeLater(() -> roomListModel.addElement(newRoom));
                } else if (msg.startsWith("/roommsg ")) {
                    String[] parts = msg.split(" ", 4);
                    int rId = Integer.parseInt(parts[1]);
                    String sender = parts[2];
                    String text = parts[3];

                    ChatRoomData room = roomMap.get(rId);
                    if (room != null) {
                        boolean isMine = sender.equals(myProfile.getUsername());
                        ChatMessage chatMsg = new ChatMessage(sender, text, isMine);
                        room.getMessageLog().addElement(chatMsg); 
                        
                        SwingUtilities.invokeLater(() -> homePanel.appendMessageToRoom(rId, chatMsg));
                    }
                }
            }
        } catch (IOException e) { 
            System.out.println("서버 연결 끊김");
        }
    }

    public void enterChatRoom(ChatRoomData roomData) {
        homePanel.showChatRoom(roomData);
    }

    public void switchToHome() {
        if(homePanel != null) {
            homePanel.goHome(); 
            cardLayout.show(mainContainer, "Home");
        }
    }

    public DataOutputStream getDos() { return dos; }
    public UserProfile getMyProfile() { return myProfile; }
    public DefaultListModel<UserProfile> getUserListModel() { return userListModel; }
    public DefaultListModel<ChatRoomData> getRoomListModel() { return roomListModel; }
}