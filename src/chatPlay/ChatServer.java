package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class ChatServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket socket;
    private Vector<UserService> UserVec = new Vector<>();
    private Map<Integer, Room> roomMap = new HashMap<>();
    private int roomNumCounter = 0;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ChatServer frame = new ChatServer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ChatServer() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 386);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        ScrollUtil.applyCustomScrollBar(scrollPane);
        scrollPane.setBounds(12, 10, 300, 244);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblPort = new JLabel("Port Number");
        lblPort.setBounds(12, 264, 87, 26);
        contentPane.add(lblPort);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(e -> {
            try {
                socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                AppendText("Chat Server Running..");
                btnServerStart.setEnabled(false);
                txtPortNumber.setEnabled(false);
                new AcceptServer().start();
            } catch (Exception ex) {
                ex.printStackTrace();
                AppendText("Server Start Error: " + ex.getMessage());
            }
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting clients ...");
                    Socket clientSocket = socket.accept();
                    AppendText("새 클라이언트 연결됨: " + clientSocket);

                    UserService newUser = new UserService(clientSocket, ChatServer.this);
                    UserVec.add(newUser);
                    newUser.start();

                    broadcastUserList();

                } catch (IOException e) {
                    AppendText("Accept Error");
                }
            }
        }
    }

    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    public synchronized void broadcastUserList() {
        String userListStr = UserVec.stream()
                .map(UserService::getUserName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(","));

        WriteAll("/userlist " + userListStr);
    }

    public synchronized void WriteAll(String msg) {
        for (UserService user : UserVec) {
            user.WriteOne(msg);
        }
    }

    public synchronized void createRoom(String roomName, Vector<UserService> members) {
        int roomId = ++roomNumCounter;

        Room room = new Room(roomId, roomName, members);
        roomMap.put(roomId, room);

        AppendText("방 생성: " + roomName);

        room.broadcast("/roomCreated " + roomId + " " + roomName);
    }

    public synchronized void sendMsgToRoom(int roomId, String sender, String msg) {
        Room room = roomMap.get(roomId);
        if (room != null) {
            room.broadcast("/roommsg " + roomId + " " + sender + " " + msg);
        }
    }

    class Room {
        int roomId;
        String roomName;
        Vector<UserService> participants;

        public Room(int roomId, String roomName, Vector<UserService> participants) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.participants = participants;
        }

        public void broadcast(String msg) {
            for (UserService u : participants) {
                u.WriteOne(msg);
            }
        }
    }

    class UserService extends Thread {
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket clientSocket;
        private String userName = "";
        private ChatServer server;

        public UserService(Socket clientSocket, ChatServer server) {
            this.clientSocket = clientSocket;
            this.server = server;

            try {
                dis = new DataInputStream(clientSocket.getInputStream());
                dos = new DataOutputStream(clientSocket.getOutputStream());

                String loginMsg = dis.readUTF();
                this.userName = loginMsg.split(" ")[1].trim();

                AppendText("유저 입장: " + userName);
                WriteOne(userName + "님 환영합니다.");

            } catch (Exception e) {
                AppendText("UserService Init Error");
            }
        }

        public String getUserName() { return userName; }

        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                removeClient();
            }
        }

        /** 클라이언트 연결 종료 */
        private void removeClient() {

            server.UserVec.remove(this);

            // 참여 중이던 방에서 제거 + 퇴장 메시지 방송
            for (Room room : server.roomMap.values()) {
                if (room.participants.contains(this)) {
                    room.participants.remove(this);

                    room.broadcast(
                            "/roommsg " + room.roomId + " System " + userName + "님이 퇴장했습니다."
                    );
                }
            }

            server.broadcastUserList();

            try { clientSocket.close(); } catch (IOException e) { }
        }

        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF().trim();
                    AppendText(msg);

                    String[] args = msg.split(" ");
                    if (args.length < 1) continue;

                    switch (args[0]) {

                        case "/makeroom":
                            if (args.length >= 2) {
                                String rName = args[1];
                                Vector<UserService> members = new Vector<>();
                                members.add(this);

                                for (int i = 2; i < args.length; i++) {
                                    for (UserService u : server.UserVec) {
                                        if (u.userName.equals(args[i])) {
                                            members.add(u);
                                            break;
                                        }
                                    }
                                }
                                server.createRoom(rName, members);
                            }
                            break;

                        case "/invite":
                            if (args.length >= 3) {
                                int rId = Integer.parseInt(args[1]);
                                String targetName = args[2];

                                Room room = server.roomMap.get(rId);
                                if (room != null) {

                                    for (UserService u : server.UserVec) {
                                        if (u.userName.equals(targetName)
                                                && !room.participants.contains(u)) {

                                            room.participants.add(u);

                                            u.WriteOne("/roomCreated " + rId + " " + room.roomName);

                                            server.sendMsgToRoom(
                                                    rId,
                                                    "System",
                                                    userName + "님이 " + targetName + "님을 초대했습니다."
                                            );
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                            
                        case "/exitroom":
                            if (args.length >= 2) {
                                int rId = Integer.parseInt(args[1]);
                                Room room = server.roomMap.get(rId);

                                if (room != null) {
                                    // 방에서 해당 유저 제거
                                    room.participants.remove(this);

                                    // 나간 사실 다른 유저들에게 알림
                                    room.broadcast("/roommsg " + rId + " System " + userName + "님이 방을 나갔습니다.");

                                    // 만약 방이 텅 비면 방 삭제(optional)
                                    if (room.participants.isEmpty()) {
                                        server.roomMap.remove(rId);
                                    }
                                }

                                // 유저의 클라이언트에게 해당 방은 삭제되었다고 알려야 함
                                WriteOne("/room_exited " + rId);
                            }
                            break;


                        case "/roomusers": {
                            if (args.length < 2) break;

                            int rId = Integer.parseInt(args[1]);
                            Room room = server.roomMap.get(rId);
                            if (room != null) {

                                // 참가자 이름만 뽑기
                                String list = room.participants.stream()
                                        .map(u -> u.userName)
                                        .collect(Collectors.joining(","));

                                // 요청한 유저에게만 전송
                                WriteOne("/roomusers_result " + rId + " " + list);
                            }
                            break;
                        }
                        
                        case "/roommsg":
                            if (args.length >= 3) {
                                int rId = Integer.parseInt(args[1]);

                                String content =
                                        msg.substring(msg.indexOf(args[2])).trim();

                                server.sendMsgToRoom(rId, userName, content);

                                if (content.equals("@채팅봇")) {
                                    server.sendMsgToRoom(
                                            rId,
                                            "ChatBot",
                                            "BOT_MENU:뉴스,날씨,게임"
                                    );
                                }
                            }
                            break;
                    }

                } catch (IOException e) {
                    removeClient();
                    break;
                }
            }
        }
    }
}
