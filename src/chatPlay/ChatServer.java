package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import game.GameManager;
import game.GameType;
import game.MessageBroadcaster;

/**
 * 채팅 서버 메인 클래스
 * 클라이언트 연결 관리, 채팅방 관리, 게임 관리 담당
 */
public class ChatServer extends JFrame {
	// 파일 저장 경로
	private static final String IMAGE_FOLDER = "shared_images/";
	private static final String PROFILE_FOLDER = "profile_images/";
	
    private static final long serialVersionUID = 1L;
    
    // UI 컴포넌트
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    // 서버 소켓 및 클라이언트 관리
    private ServerSocket socket;
    private Vector<UserService> UserVec = new Vector<>();
    private Map<Integer, Room> roomMap = new HashMap<>();
    private int roomNumCounter = 0;
    
    // 게임 관리자
    private GameManager gameManager = new GameManager();

    /**
     * 프로그램 진입점
     */
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

    /**
     * 서버 프레임 생성자
     * UI 초기화 및 필요한 디렉토리 생성
     */
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
        
        // 필요한 디렉토리 생성
        File imageDir = new File(IMAGE_FOLDER);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        
        File profileDir = new File(PROFILE_FOLDER);
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }

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

    /**
     * 클라이언트 연결을 받는 스레드
     */
    class AcceptServer extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting clients ...");
                    Socket clientSocket = socket.accept();
                    AppendText("새 클라이언트 연결됨: " + clientSocket);

                    // 새 클라이언트 서비스 생성 및 시작
                    UserService newUser = new UserService(clientSocket, ChatServer.this);
                    UserVec.add(newUser);
                    newUser.start();

                    // 모든 클라이언트에게 사용자 목록 갱신
                    broadcastUserList();

                } catch (IOException e) {
                    AppendText("Accept Error");
                }
            }
        }
    }

    /**
     * 서버 로그 텍스트 영역에 메시지 추가
     */
    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    /**
     * 모든 클라이언트에게 사용자 목록 브로드캐스트
     */
    public synchronized void broadcastUserList() {
        String userListStr = UserVec.stream()
                .map(UserService::getUserName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(","));

        WriteAll("/userlist " + userListStr);
    }

    /**
     * 모든 클라이언트에게 메시지 전송
     */
    public synchronized void WriteAll(String msg) {
        for (UserService user : UserVec) {
            user.WriteOne(msg);
        }
    }

    /**
     * 새 채팅방 생성
     * @param roomName 방 이름
     * @param members 참여자 목록
     */
    public synchronized void createRoom(String roomName, Vector<UserService> members) {
        int roomId = ++roomNumCounter;

        Room room = new Room(roomId, roomName, members);
        roomMap.put(roomId, room);

        AppendText("방 생성: " + roomName);

        // 참여자들에게 방 생성 알림
        room.broadcast("/roomCreated " + roomId + " " + roomName);
    }

    /**
     * 특정 방에 메시지 전송
     * @param roomId 방 ID
     * @param sender 발신자
     * @param msg 메시지 내용
     */
    public synchronized void sendMsgToRoom(int roomId, String sender, String msg) {
        Room room = roomMap.get(roomId);
        if (room != null) {
            room.broadcast("/roommsg " + roomId + " " + sender + " " + msg);
        }
    }

    /**
     * 채팅방 정보를 담는 클래스
     */
    class Room {
        int roomId;
        String roomName;
        Vector<UserService> participants;

        public Room(int roomId, String roomName, Vector<UserService> participants) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.participants = participants;
        }

        /**
         * 방의 모든 참여자에게 메시지 브로드캐스트
         */
        public void broadcast(String msg) {
            for (UserService u : participants) {
                u.WriteOne(msg);
            }
        }
        
        /**
         * 게임 참가자에게만 메시지 전송
         * @param msg 메시지
         * @param gameParticipants 게임 참가자 목록
         */
        public void broadcastToGameParticipants(String msg, Set<String> gameParticipants) {
            for (UserService u : participants) {
                if (gameParticipants.contains(u.getUserName())) {
                    u.WriteOne(msg);
                }
            }
        }
    }

    /**
     * 개별 클라이언트와의 통신을 담당하는 스레드
     */
    class UserService extends Thread {
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket clientSocket;
        private String userName = "";
        private ChatServer server;

        /**
         * 클라이언트 서비스 생성자
         * 로그인 메시지를 받아 사용자명 설정
         */
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

        /**
         * 클라이언트에게 메시지 전송
         */
        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                removeClient();
            }
        }

        /**
         * 클라이언트 연결 종료 처리
         * 참여 중인 방에서 제거하고 사용자 목록 갱신
         */
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

        /**
         * 클라이언트로부터 메시지를 수신하고 처리하는 메인 루프
         */
        @Override
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF().trim();
                    AppendText(msg);

                    String[] args = msg.split(" ");
                    if (args.length < 1) continue;

                    // 프로토콜에 따른 명령 처리
                    switch (args[0]) {

                        case "/makeroom": // 채팅방 생성
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

                        case "/invite": // 사용자 초대
                            if (args.length >= 3) {
                                int rId = Integer.parseInt(args[1]);
                                String targetName = args[2];

                                Room room = server.roomMap.get(rId);
                                if (room != null) {
                                    // 대상 사용자 찾아서 방에 추가
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
                            
                        case "/exitroom": // 방 나가기
                            if (args.length >= 2) {
                                int rId = Integer.parseInt(args[1]);
                                Room room = server.roomMap.get(rId);

                                if (room != null) {
                                    room.participants.remove(this);
                                    room.broadcast("/roommsg " + rId + " System " + userName + "님이 방을 나갔습니다.");

                                    if (room.participants.isEmpty()) {
                                        server.roomMap.remove(rId);
                                    }
                                }

                                WriteOne("/room_exited " + rId);
                            }
                            break;

                        case "/roomusers": // 참여자 목록 조회
                            if (args.length < 2) break;

                            int rId = Integer.parseInt(args[1]);
                            Room room = server.roomMap.get(rId);
                            if (room != null) {
                                String list = room.participants.stream()
                                        .map(u -> u.userName)
                                        .collect(Collectors.joining(","));

                                WriteOne("/roomusers_result " + rId + " " + list);
                            }
                            break;
                        
                        case "/roommsg": // 채팅 메시지 전송
                            if (args.length >= 3) {
                                int roomMsgId = Integer.parseInt(args[1]);
                                String content = msg.substring(msg.indexOf(args[2])).trim();

                                server.sendMsgToRoom(roomMsgId, userName, content);

                               if (content.equals("@채팅봇")) {
                            	   server.sendMsgToRoom(roomMsgId, "ChatBot", "BOT_MENU:뉴스,날씨,게임");
                               	}
                            }
                            break;
                            
                        case "/upload_image": // 이미지 파일 업로드
                            if (args.length >= 3) {
                                int roomId = Integer.parseInt(args[1]);
                                String fileName = args[2];
                                
                                try {
                                    // 파일 크기 읽기
                                    int fileSize = dis.readInt();
                                    
                                    // 파일 데이터 읽기
                                    byte[] fileData = new byte[fileSize];
                                    dis.readFully(fileData);
                                    
                                    // 서버의 shared_images 폴더에 저장
                                    File destFile = new File(IMAGE_FOLDER + fileName);
                                    java.nio.file.Files.write(destFile.toPath(), fileData);
                                    
                                    AppendText("이미지 저장: " + fileName + " (" + fileSize + " bytes)");
                                    
                                    // 방 참여자들에게 파일명만 브로드캐스트
                                    server.sendMsgToRoom(roomId, userName, "@imagefile " + fileName);
                                    
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    AppendText("이미지 저장 실패: " + ex.getMessage());
                                }
                            }
                            break;
                        
                        case "/upload_profile": // 프로필 이미지 업로드
                            if (args.length >= 3) {
                                String imageType = args[1]; // "icon" 또는 "background"
                                String fileName = args[2];
                                
                                try {
                                    // 파일 크기 읽기
                                    int fileSize = dis.readInt();
                                    
                                    // 파일 데이터 읽기
                                    byte[] fileData = new byte[fileSize];
                                    dis.readFully(fileData);
                                    
                                    // profile_images 폴더에 저장
                                    // 형식: username_icon.png 또는 username_background.png
                                    String savedFileName = userName + "_" + imageType + "_" + fileName;
                                    File destFile = new File(PROFILE_FOLDER + savedFileName);
                                    java.nio.file.Files.write(destFile.toPath(), fileData);
                                    
                                    AppendText("프로필 이미지 저장: " + savedFileName + " (" + fileSize + " bytes)");
                                    
                                    // 저장된 파일명을 클라이언트에게 회신
                                    WriteOne("/profile_saved " + imageType + " " + savedFileName);
                                    
                                    // 모든 사용자에게 프로필 변경 알림 브로드캐스트
                                    server.WriteAll("/profile_updated " + userName + " " + imageType + " " + savedFileName);
                                    
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    AppendText("프로필 이미지 저장 실패: " + ex.getMessage());
                                }
                            }
                            break;

                        case "/request_profile": // 프로필 이미지 요청
                            if (args.length >= 2) {
                                String targetUser = args[1];
                                
                                // profile_images 폴더에서 해당 사용자의 이미지 찾기
                                File profileDir = new File(PROFILE_FOLDER);
                                File[] files = profileDir.listFiles((dir, name) -> 
                                    name.startsWith(targetUser + "_")
                                );
                                
                                if (files != null) {
                                    for (File f : files) {
                                        String fileName = f.getName();
                                        String imageType = fileName.contains("_icon_") ? "icon" : "background";
                                        WriteOne("/profile_data " + targetUser + " " + imageType + " " + fileName);
                                    }
                                }
                            }
                            break;
                        
                        /**
                         * 상태메시지 업데이트 요청 처리
                         * 클라이언트로부터 받은 상태메시지 변경을 모든 클라이언트에게 브로드캐스트
                         */
                        case "/update_status":
                            if (args.length >= 2) {
                                try {
                                    // URL 디코딩하여 원본 상태메시지 복원
                                    String statusMessage = java.net.URLDecoder.decode(args[1], "UTF-8");
                                    
                                    // 모든 연결된 클라이언트에게 상태메시지 변경 알림 브로드캐스트
                                    // 형식: /status_updated [사용자명] [인코딩된 상태메시지]
                                    server.WriteAll("/status_updated " + userName + " " + 
                                        java.net.URLEncoder.encode(statusMessage, "UTF-8"));
                                    
                                    AppendText("상태메시지 업데이트: " + userName + " - " + statusMessage);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    AppendText("상태메시지 업데이트 실패: " + ex.getMessage());
                                }
                            }
                            break;
                            
                        case "/bot": // 챗봇 명령 처리
                            if (args.length >= 3) {
                                String command = args[1];
                                int botRoomId = Integer.parseInt(args[2]);

                                if (command.equals("weather")) {
                                    server.sendMsgToRoom(botRoomId, "ChatBot", "BOT_MENU:오늘의 날씨,7일 예보");
                                }
                                else if (command.equals("news")) {
                                    server.sendMsgToRoom(
                                        botRoomId,
                                        "ChatBot",
                                        "BOT_MENU:속보,정치,경제,사회,세계,IT/과학"
                                    );
                                }
                                else if (command.equals("game")) {
                                    server.sendMsgToRoom(botRoomId, "ChatBot", "BOT_MENU:캐치마인드,요트다이스");
                                }
                                else if (command.equals("catchmind")) {
                                    server.sendMsgToRoom(botRoomId, "ChatBot", "GAME_JOIN:catchmind");
                                }
                                else if (command.equals("yacht")) {
                                    server.sendMsgToRoom(botRoomId, "ChatBot", "GAME_JOIN:yacht");
                                }
                             }
                             break;
 
                        case "/gamemsg": // 게임 메시지 전송
                            if (args.length >= 3) {
                                int roomId = Integer.parseInt(args[1]);
                                String content = msg.substring(msg.indexOf(args[2])).trim();
                                
                                server.gameManager.handleGameMessage(roomId, userName, content);
                            }
                            break;

                        case "/game_yacht":
                            if (args.length >= 3) {
                                int roomId = Integer.parseInt(args[1]);
                                String content = msg.substring(msg.indexOf(args[2])).trim();
                                
                                server.gameManager.handleGameMessage(roomId, userName, content);
                            }
                            break;

                        case "/yacht_join": // 요트다이스 게임 참여
                        	if (args.length < 2) break;
                        
                        	int yachtJoinId = Integer.parseInt(args[1]);
                        	Room yachtRoom = server.roomMap.get(yachtJoinId);
                        
                        	if (yachtRoom != null) {
                        		// GameManager에 참여자 추가
                        		boolean added = server.gameManager.addParticipant(yachtJoinId, userName, GameType.YACHT);
                            
                        		if (added) {
                        		    int count = server.gameManager.getParticipantCount(yachtJoinId);
                        			server.sendMsgToRoom(
                        				yachtJoinId,
                        				"System",
                        				userName + "님이 요트다이스에 참여했습니다. (" + count + "명)"
                        			);
                        		}
                        	}
                        	break;

                        case "/yacht_start": // 요트다이스 게임 시작
                            if (args.length < 2) break;

                            int yachtStartId = Integer.parseInt(args[1]);
                            Room yachtStartRoom = server.roomMap.get(yachtStartId);

                            // GameManager로 게임 시작 가능 확인
                            if (!server.gameManager.canStartGame(yachtStartId, GameType.YACHT)) {
                                server.sendMsgToRoom(yachtStartId, "System",
                                    "최소 2명 이상 참여해야 게임을 시작할 수 있습니다.");
                                break;
                            }

                            if (yachtStartRoom != null) {
                                // GameManager에서 참여자 목록 가져오기
                                Set<String> participants = server.gameManager.getWaitingParticipants(yachtStartId);
                                if (participants == null) break;

                                List<String> participantList = new ArrayList<>(participants);

                                // 1. 게임 창 열기
                                String participantStr = String.join(",", participantList);
                                for (String participant : participantList) {
                                    for (UserService u : server.UserVec) {
                                        if (u.getUserName().equals(participant)) {
                                            u.WriteOne("/game_participants " + yachtStartId + " yacht " + participantStr);
                                            break;
                                        }
                                    }
                                }

                                server.AppendText("게임 창 열기: " + participantList);

                                // 2. 버튼 비활성화
                                server.sendMsgToRoom(yachtStartId, "ChatBot", "GAME_STARTED:yacht");

                                // 3. MessageBroadcaster 구현
                                MessageBroadcaster broadcaster = new MessageBroadcaster() {
                                    @Override
                                    public void broadcastToRoom(int roomId, String message) {
                                        Room r = server.roomMap.get(roomId);
                                        if (r == null) return;
                                        
                                        // GameManager에서 활성 참여자 가져오기
                                        Set<String> activeParticipants = server.gameManager.getActiveParticipants(roomId);
                                        if (activeParticipants != null) {
                                            r.broadcastToGameParticipants(message, activeParticipants);
                                        }
                                    }
                                    
                                    @Override
                                    public void sendToUser(String userName, String message) {
                                        for (UserService u : server.UserVec) {
                                            if (u.getUserName().equals(userName)) {
                                                u.WriteOne(message);
                                                break;
                                            }
                                        }
                                    }
                                };

                                // 4. 게임 시작
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(500);
                                        
                                        // GameManager로 게임 시작
                                        server.gameManager.startGame(yachtStartId, GameType.YACHT, broadcaster);
                                        server.AppendText("요트다이스 시작: Room " + yachtStartId + " (" + participantList.size() + "명)");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                            break;

                        case "/catchmind_join": // 캐치마인드 게임 참여
                            if (args.length < 2) break;
                            
                            int catchJoinId = Integer.parseInt(args[1]);
                            Room catchJoinRoom = server.roomMap.get(catchJoinId);
                            
                            if (catchJoinRoom != null) {
                                // GameManager에 참여자 추가
                                boolean added = server.gameManager.addParticipant(catchJoinId, userName, GameType.CATCH_MIND);
                                
                                if (added) {
                                    int count = server.gameManager.getParticipantCount(catchJoinId);
                                    server.sendMsgToRoom(
                                        catchJoinId,
                                        "System",
                                        userName + "님이 캐치마인드에 참여했습니다. (" + count + "명)"
                                    );
                                }
                            }
                            break;

                        case "/catchmind_start": // 캐치마인드 게임 시작
                            if (args.length < 2) break;
                            
                            int catchStartId = Integer.parseInt(args[1]);
                            Room catchStartRoom = server.roomMap.get(catchStartId);
                            
                            // GameManager로 게임 시작 가능 확인
                            if (!server.gameManager.canStartGame(catchStartId, GameType.CATCH_MIND)) {
                                server.sendMsgToRoom(catchStartId, "System", 
                                    "최소 2명 이상 참여해야 게임을 시작할 수 있습니다.");
                                break;
                            }
                            
                            if (catchStartRoom != null) {
                                // GameManager에서 참여자 목록 가져오기
                                Set<String> participants = server.gameManager.getWaitingParticipants(catchStartId);
                                if (participants == null) break;
                                
                                List<String> participantList = new ArrayList<>(participants);
                                
                                // 1. 게임 창 열기
                                String participantStr = String.join(",", participantList);
                                for (String participant : participantList) {
                                    for (UserService u : server.UserVec) {
                                        if (u.getUserName().equals(participant)) {
                                            u.WriteOne("/game_participants " + catchStartId + " catchmind " + participantStr);
                                            break;
                                        }
                                    }
                                }
                                
                                server.AppendText("게임 창 열기: " + participantList);
                                
                                // 2. 버튼 비활성화
                                server.sendMsgToRoom(catchStartId, "ChatBot", "GAME_STARTED:catchmind");
                                
                                // 3. MessageBroadcaster 구현
                                MessageBroadcaster broadcaster = new MessageBroadcaster() {
                                    @Override
                                    public void broadcastToRoom(int roomId, String message) {
                                        Room r = server.roomMap.get(roomId);
                                        if (r == null) return;
                                        
                                        // ✅ GameManager에서 활성 참여자 가져오기
                                        Set<String> activeParticipants = server.gameManager.getActiveParticipants(roomId);
                                        if (activeParticipants != null) {
                                            r.broadcastToGameParticipants(message, activeParticipants);
                                        }
                                    }
                                    
                                    @Override
                                    public void sendToUser(String userName, String message) {
                                        for (UserService u : server.UserVec) {
                                            if (u.getUserName().equals(userName)) {
                                                u.WriteOne(message);
                                                break;
                                            }
                                        }
                                    }
                                };
                                
                                // 4. 게임 시작
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(500);
                                        
                                        // GameManager로 게임 시작
                                        server.gameManager.startGame(catchStartId, GameType.CATCH_MIND, broadcaster);
                                        server.AppendText("캐치마인드 게임 시작: Room " + catchStartId + " (" + participantList.size() + "명)");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                            }
                            break;

                        case "/draw": // 그림 그리기 데이터 전송
                            if (args.length >= 10) {
                                int drawRoomId = Integer.parseInt(args[1]);
                                Room drawRoom = server.roomMap.get(drawRoomId);
                                
                                if (drawRoom != null) {
                                    // 캐치마인드 게임인지 확인 
                                    if (!server.gameManager.isCatchMindGame(drawRoomId)) {
                                        break;
                                    }
                                    
                                    // GameManager에서 활성 참여자 가져오기
                                    Set<String> gameParticipants = server.gameManager.getActiveParticipants(drawRoomId);
                                    
                                    if (gameParticipants != null && !gameParticipants.isEmpty()) {
                                        drawRoom.broadcastToGameParticipants(msg, gameParticipants);
                                    }
                                }
                            }
                            break;

                        case "/cleardraw": // 캔버스 지우기
                            if (args.length >= 2) {
                                int clearRoomId = Integer.parseInt(args[1]);
                                Room clearRoom = server.roomMap.get(clearRoomId);
                                
                                if (clearRoom != null) {
                                    // 캐치마인드 게임인지 확인 
                                    if (!server.gameManager.isCatchMindGame(clearRoomId)) {
                                        break;
                                    }
                                    
                                    // GameManager에서 활성 참여자 가져오기
                                    Set<String> gameParticipants = server.gameManager.getActiveParticipants(clearRoomId);
                                    
                                    if (gameParticipants != null && !gameParticipants.isEmpty()) {
                                        clearRoom.broadcastToGameParticipants(msg, gameParticipants);
                                    }
                                }
                            }
                            break;

                        case "/endgame": // 게임 종료
                            if (args.length >= 2) {
                                int endGameRoomId = Integer.parseInt(args[1]);
                                
                                // GameManager가 모든 정리 담당
                                server.gameManager.endGame(endGameRoomId);
                                
                                Room endGameRoom = server.roomMap.get(endGameRoomId);
                                if (endGameRoom != null) {
                                    endGameRoom.broadcast("/game_ended " + endGameRoomId);
                                }
                                
                                server.AppendText("게임 종료: Room " + endGameRoomId);
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