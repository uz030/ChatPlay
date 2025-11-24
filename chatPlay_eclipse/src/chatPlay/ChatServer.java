package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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
        txtPortNumber.setColumns(10);

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

    // 클라이언트 접속 수락 스레드
    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting clients ...");
                    Socket clientSocket = socket.accept();
                    AppendText("새로운 참가자 연결됨: " + clientSocket);
                    
                    // 1. 유저 스레드 생성 (입장 처리는 내부에서)
                    UserService newUser = new UserService(clientSocket, ChatServer.this);
                    UserVec.add(newUser);
                    newUser.start();
                    
                    // 2. 전체 목록 갱신 방송
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

    public synchronized void WriteAll(String str) {
        for (UserService user : UserVec) user.WriteOne(str);
    }

    public synchronized void createRoom(String roomName, Vector<UserService> members) {
        int roomId = ++roomNumCounter;
        Room room = new Room(roomId, roomName, members);
        roomMap.put(roomId, room);
        AppendText("방 생성: [" + roomId + "] " + roomName);
        room.broadcast("/roomCreated " + roomId + " " + roomName);
    }

    public synchronized void sendMsgToRoom(int roomId, String sender, String msg) {
        Room room = roomMap.get(roomId);
        if (room != null) room.broadcast("/roommsg " + roomId + " " + sender + " " + msg);
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
            for (UserService user : participants) user.WriteOne(msg);
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
                
                // 로그인 메시지 수신
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
            try { dos.writeUTF(msg); } 
            catch (IOException e) { removeClient(); }
        }

        private void removeClient() {
            server.UserVec.remove(this);
            server.broadcastUserList();
            try { clientSocket.close(); } catch (IOException e) {}
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
                                        if (u.userName.equals(args[i])) { members.add(u); break; }
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
                                        if (u.userName.equals(targetName) && !room.participants.contains(u)) {
                                            room.participants.add(u);
                                            u.WriteOne("/roomCreated " + rId + " " + room.roomName);
                                            server.sendMsgToRoom(rId, "System", userName + "님이 " + targetName + "님을 초대했습니다.");
                                            break;
                                        }
                                    }
                                }
                            }
                            break;

                        case "/roommsg":
                            if (args.length >= 3) {
                                int rId = Integer.parseInt(args[1]);
                                String content = msg.substring(msg.indexOf(args[1]) + args[1].length()).trim();
                                server.sendMsgToRoom(rId, userName, content);
                                if (content.equals("@채팅봇")) {
                                    server.sendMsgToRoom(rId, "ChatBot", "BOT_MENU:뉴스,날씨,게임");
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