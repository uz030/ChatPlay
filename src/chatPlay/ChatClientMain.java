package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List; 

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

    // 열린 채팅창 관리
    private Map<Integer, ChatRoomFrame> openedRoomFrames = new HashMap<>();

    // 기본 프로필 이미지 
    private ImageIcon defaultProfileIcon;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ChatClientMain().setVisible(true));
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Program");
        setSize(392, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // 기본 이미지 로드 
        try {
            defaultProfileIcon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
        } catch (Exception e) {
            defaultProfileIcon = new ImageIcon();
        }

        userListModel = new DefaultListModel<>();
        roomListModel = new DefaultListModel<>();
        roomMap = new HashMap<>();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        setContentPane(mainContainer);

        mainPanel = new MainPanel(this);
        homePanel = new ChatHome(this);

        mainContainer.add(mainPanel, "Main");
        mainContainer.add(homePanel, "Home");

        cardLayout.show(mainContainer, "Main");
    }

    public void connectToServer(String username, ImageIcon icon) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("/login " + username);
            dos.flush();

            this.myProfile = new UserProfile(username, icon);

            new Thread(this::listenToServer).start();

            cardLayout.show(mainContainer, "Home");
            homePanel.showProfilePanel();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "서버 연결 실패\n" + ex.getMessage(),
                    "오류",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void listenToServer() {
        try {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("Server: " + msg);

                // --- 유저 목록 ---
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
                }

                // --- 방 생성됨 ---
                else if (msg.startsWith("/roomCreated ")) {
                    String[] parts = msg.split(" ", 3);
                    int rId = Integer.parseInt(parts[1]);
                    String rName = parts[2];

                    ChatRoomData newRoom = new ChatRoomData(rId, rName);
                    roomMap.put(rId, newRoom);

                    SwingUtilities.invokeLater(() -> roomListModel.addElement(newRoom));
                }

                // --- 채팅 메시지 ---
                else if (msg.startsWith("/roommsg ")) {

                    String[] parts = msg.split(" ", 4);
                    int rId = Integer.parseInt(parts[1]);
                    String sender = parts[2];
                    String text = parts[3];

                    ChatRoomData room = roomMap.get(rId);
                    if (room != null) {

                        boolean isMine = sender.equals(myProfile.getUsername());
                        
                        ImageIcon icon = isMine ? myProfile.getIcon() : findUserIcon(sender);

                        // 아이콘 정보를 포함하여 ChatMessage 생성
                        ChatMessage chatMsg = new ChatMessage(sender, text, isMine, icon);

                        // 기록 저장
                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });
                    }
                }
                
                else if (msg.startsWith("/roomusers_result ")) {
                    String[] parts = msg.split(" ", 3);
                    int rId = Integer.parseInt(parts[1]);
                    String listStr = parts[2];
                    
                    // 콤마로 구분된 문자열을 리스트로 변환
                    List<String> users = Arrays.asList(listStr.split(","));

                    SwingUtilities.invokeLater(() -> {
                        // 해당 채팅방의 Frame 찾기 
                        Window owner = openedRoomFrames.get(rId);
                        if (owner == null) owner = this; // 창이 안 열려있으면 메인 프레임이 주인

                        new UserListDialog(owner, "참여자 목록", users, 
                            () -> {}, // 초대 콜백 
                            () -> {}  // 나가기 콜백 
                        ).setVisible(true);
                    });
                }
            }

        } catch (IOException e) {
            System.out.println("서버 연결 끊김");
        }
    }

    // 유저 이름으로 아이콘 찾는 헬퍼
    private ImageIcon findUserIcon(String username) {
        if (username.equals("ChatBot") || username.equals("System")) {
            return defaultProfileIcon;
        }
        
        for (int i = 0; i < userListModel.getSize(); i++) {
            UserProfile user = userListModel.getElementAt(i);
            if (user.getUsername().equals(username)) {
                return user.getIcon();
            }
        }
        return defaultProfileIcon;
    }

    public void enterChatRoom(ChatRoomData roomData) {
        int roomId = roomData.getRoomId();

        if (openedRoomFrames.containsKey(roomId)) {
            openedRoomFrames.get(roomId).toFront();
            return;
        }

        ChatRoomFrame frame = new ChatRoomFrame(this, roomData);
        openedRoomFrames.put(roomId, frame);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                openedRoomFrames.remove(roomId);
            }
        });

        frame.setVisible(true);
    }

    public ChatHome getHomePanel() { return homePanel; }
    public DataOutputStream getDos() { return dos; }
    public UserProfile getMyProfile() { return myProfile; }
    public DefaultListModel<UserProfile> getUserListModel() { return userListModel; }
    public DefaultListModel<ChatRoomData> getRoomListModel() { return roomListModel; }
}