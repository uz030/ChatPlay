package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
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

    // 열린 채팅창 관리 (멀티 윈도우)
    private Map<Integer, ChatRoomFrame> openedRoomFrames = new HashMap<>();

    // 기본 프로필 이미지 
    private ImageIcon defaultProfileIcon;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ChatClientMain().setVisible(true));
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ChatPlay");
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

            // 홈 화면으로 전환 후 프로필 패널 표시
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

                // --- 유저 목록 갱신 ---
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

                // --- 방 생성 알림 ---
                else if (msg.startsWith("/roomCreated ")) {
                    String[] parts = msg.split(" ", 3);
                    int rId = Integer.parseInt(parts[1]);
                    String rName = parts[2];

                    ChatRoomData newRoom = new ChatRoomData(rId, rName);
                    roomMap.put(rId, newRoom);

                    SwingUtilities.invokeLater(() -> roomListModel.addElement(newRoom));
                }
                else if (msg.startsWith("/room_exited ")) {
                    int roomId = Integer.parseInt(msg.split(" ")[1]);

                    SwingUtilities.invokeLater(() -> {
                        // 1. roomMap에서 제거
                        roomMap.remove(roomId);

                        // 2. UI 리스트에서 제거
                        for (int i = 0; i < roomListModel.size(); i++) {
                            if (roomListModel.get(i).getRoomId() == roomId) {
                                roomListModel.remove(i);
                                break;
                            }
                        }

                        // 3. 만약 방 창이 열려 있으면 닫기
                        ChatRoomFrame frame = openedRoomFrames.get(roomId);
                        if (frame != null) {
                            frame.dispose();
                        }
                    });
                }


                // --- 채팅 메시지 수신 ---
                else if (msg.startsWith("/roommsg ")) {

                    String[] parts = msg.split(" ", 4);
                    int rId = Integer.parseInt(parts[1]);
                    String sender = parts[2];
                    String text = parts[3];

                    ChatRoomData room = roomMap.get(rId);
                    if (room != null) {

                        boolean isMine = sender.equals(myProfile.getUsername());
                        
                        // 1. 보낸 사람의 아이콘 찾기
                        ImageIcon senderIcon;
                        if (isMine) {
                            senderIcon = myProfile.getIcon();
                        } else {
                            senderIcon = findUserIcon(sender);
                        }

                        // 2. 아이콘 정보를 포함하여 메시지 객체 생성
                        ChatMessage chatMsg = new ChatMessage(sender, text, isMine, senderIcon);

                        // 3. 기록 저장
                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {
                            // 열린 채팅창이 있으면 창에 표시, 없으면 홈 리스트에만 표시(추후 구현)
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });
                    }
                }
                
                // --- 참여자 목록 확인 결과 (UserListDialog 호출) ---
                else if (msg.startsWith("/roomusers_result ")) {
                    String[] parts = msg.split(" ", 3);
                    int rId = Integer.parseInt(parts[1]);
                    String listStr = parts[2];
                    
                    String[] userNames = listStr.split(",");
                    
                    // 1. 이름 목록을 UserProfile 리스트로 변환
                    List<UserProfile> profiles = new ArrayList<>();
                    
                    for (String name : userNames) {
                        name = name.trim();
                        if (name.isEmpty()) continue;

                        // 내 프로필
                        if (name.equals(myProfile.getUsername())) {
                            profiles.add(myProfile);
                        } else {
                            // 친구 목록에서 검색
                            boolean found = false;
                            for (int i = 0; i < userListModel.getSize(); i++) {
                                UserProfile p = userListModel.getElementAt(i);
                                if (p.getUsername().equals(name)) {
                                    profiles.add(p);
                                    found = true;
                                    break;
                                }
                            }
                            // 목록에 없으면(접속 종료 등) 이름만 가진 임시 프로필 생성
                            if (!found) {
                                profiles.add(new UserProfile(name)); 
                            }
                        }
                    }

                    SwingUtilities.invokeLater(() -> {
                        Window owner = openedRoomFrames.get(rId);
                        if (owner == null) owner = this;

                        // 2. UserListDialog 생성 및 표시
                        new UserListDialog(
                        	    owner,
                        	    "참여자 목록",
                        	    profiles,

                        	    // ★ 초대 버튼 동작 연결
                        	    () -> {
                        	        ChatRoomData room = roomMap.get(rId);
                        	        if (room != null) {
                        	            ChatRoomPanel panel = openedRoomFrames.get(rId).getChatRoomPanel();
                        	            panel.openInviteDialog(); // 기존 초대 UI 호출
                        	        }
                        	    },

                        	    // ★ 나가기 버튼 동작 연결
                        	    () -> {
                        	        try {
                        	            dos.writeUTF("/exitroom " + rId);
                        	        } catch (Exception ex) {
                        	            ex.printStackTrace();
                        	        }
                        	    }
                        	).setVisible(true);

                    });
                }
            }

        } catch (IOException e) {
            System.out.println("서버 연결 끊김");
        }
    }

    // 유저 이름으로 프로필 아이콘 찾는 헬퍼
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