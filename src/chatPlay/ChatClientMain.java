package chatPlay;

import javax.swing.*;

import catchmind.CatchMindFrame;
import yacht.YachtFrame;

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
    
    // 열린 게임창 관리
    private Map<Integer, CatchMindFrame> openedGameFrames = new HashMap<>();
    private Map<Integer, YachtFrame> openedYachtFrames = new HashMap<>();
    // 게임 메시지 핸들러
    private GameMessageHandler gameMessageHandler;

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
        
        gameMessageHandler = new GameMessageHandler(this, openedGameFrames);
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
                
                if (gameMessageHandler.handleMessage(msg)) {
                    continue; // 게임 메시지면 처리 완료
                }

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
                    if (room == null) continue;

                    boolean isMine = sender.equals(myProfile.getUsername());

                    ImageIcon senderIcon;
                    if (isMine) {
                        senderIcon = myProfile.getIcon();
                    } else {
                        senderIcon = findUserIcon(sender);
                    }

                    // ✅ GAME_STARTED 메시지 처리 (버튼 비활성화용)
                    if (text.startsWith("GAME_STARTED:")) {
                        ChatMessage chatMsg = new ChatMessage(sender, text, false, senderIcon);
                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });
                        continue;
                    }

                    // GAME_JOIN 메시지 처리 (참여 UI)
                    if (text.startsWith("GAME_JOIN:")) {
                        ChatMessage chatMsg = new ChatMessage(sender, text, false, senderIcon);
                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });
                        continue;
                    }

                    // 이미지 메시지
                    if (text.startsWith("@images")) {
                        String fileName = text.substring(8).trim();
                        ImageIcon img = loadEmojiImage(fileName);

                        ChatMessage chatMsg = new ChatMessage(sender, "", isMine, senderIcon);
                        chatMsg.setType(ChatMessage.MessageType.IMAGE);
                        chatMsg.setImageIcon(img);
                        chatMsg.setFileName(fileName);

                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });

                        continue;
                    }

                    // 일반 텍스트 메시지
                    ChatMessage chatMsg = new ChatMessage(sender, text, isMine, senderIcon);
                    room.getMessageLog().addElement(chatMsg);

                    SwingUtilities.invokeLater(() -> {
                        if (openedRoomFrames.containsKey(rId)) {
                            openedRoomFrames.get(rId).appendMessage(chatMsg);
                        } else {
                            homePanel.appendMessageToRoom(rId, chatMsg);
                        }
                    });
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

                        	    // 초대 버튼 동작 연결
                        	    () -> {
                        	        ChatRoomData room = roomMap.get(rId);
                        	        if (room != null) {
                        	            ChatRoomPanel panel = openedRoomFrames.get(rId).getChatRoomPanel();
                        	            panel.openInviteDialog(); // 기존 초대 UI 호출
                        	        }
                        	    },

                        	    // 나가기 버튼 동작 연결
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
    
    public void openCatchMindFrame(int roomId) {
        try {
            // 서버에 참여 요청
            dos.writeUTF("/catchmind_join " + roomId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void openYachtFrame(int roomId) {
        try {
            // 서버에 참여 요청
            dos.writeUTF("/yacht_join " + roomId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // 캐치마인드 게임 창 생성 (GameMessageHandler에서 호출)
    public void createCatchMindFrame(int roomId, List<String> participants) {
        SwingUtilities.invokeLater(() -> {
            if (openedGameFrames.containsKey(roomId)) {
                openedGameFrames.get(roomId).toFront();
                return;
            }
            
            CatchMindFrame frame = new CatchMindFrame(
                roomId, 
                myProfile.getUsername(), 
                dos, 
                participants
            );
            
            openedGameFrames.put(roomId, frame);
            
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openedGameFrames.remove(roomId);
                }
            });
        });
    }
    
    // 요트다이스 게임 창 생성 (GameMessageHandler에서 호출)
    public void createYachtFrame(int roomId, List<String> participants) {
        SwingUtilities.invokeLater(() -> {
            if (openedYachtFrames.containsKey(roomId)) {
                openedYachtFrames.get(roomId).toFront();
                return;
            }
            
            YachtFrame frame = new YachtFrame(
                roomId, 
                myProfile.getUsername(), 
                dos, 
                participants
            );
            
            openedYachtFrames.put(roomId, frame);
            
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    openedYachtFrames.remove(roomId);
                }
            });
            
            frame.setVisible(true);
        });
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

    private ImageIcon loadEmojiImage(String fileName) {
        try {
            return new ImageIcon("src/images/icon/" + fileName);
        } catch (Exception e) {
            System.out.println("이미지 로딩 실패: " + fileName);
            return defaultProfileIcon;
        }
    }

    
    public ChatHome getHomePanel() { return homePanel; }
    public DataOutputStream getDos() { return dos; }
    public UserProfile getMyProfile() { return myProfile; }
    public DefaultListModel<UserProfile> getUserListModel() { return userListModel; }
    public DefaultListModel<ChatRoomData> getRoomListModel() { return roomListModel; }
}