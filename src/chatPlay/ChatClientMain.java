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

    // 열린 채팅창 관리
    private Map<Integer, ChatRoomFrame> openedRoomFrames = new HashMap<>();


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new ChatClientMain().setVisible(true));
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Program");
        setSize(392, 600);
        setLocationRelativeTo(null);
        setResizable(false);

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

            // ✔ 홈 화면으로 카드 전환 (필수)
            cardLayout.show(mainContainer, "Home");

            // ✔ 그 다음 ProfilePanel 띄우기
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
                        ChatMessage chatMsg = new ChatMessage(sender, text, isMine);

                        // 기록 저장
                        room.getMessageLog().addElement(chatMsg);

                        SwingUtilities.invokeLater(() -> {

                            // 열린 채팅창이 있으면 → 창에 표시
                            if (openedRoomFrames.containsKey(rId)) {
                                openedRoomFrames.get(rId).appendMessage(chatMsg);
                            } else {
                                // 창이 닫혀있다면 → 홈 리스트에만 표시
                                homePanel.appendMessageToRoom(rId, chatMsg);
                            }
                        });
                    }
                }
                else if (msg.startsWith("/roomusers_result ")) {
                    String[] parts = msg.split(" ", 3);
                    String list = parts[2];

                    String display = list.replace(",", "\n");

                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(
                                    null,
                                    display,
                                    "참여자 목록",
                                    JOptionPane.INFORMATION_MESSAGE
                            )
                    );
                }

            }

        } catch (IOException e) {
            System.out.println("서버 연결 끊김");
        }
    }

    /** 채팅방 열기 (중복 방지) */
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
