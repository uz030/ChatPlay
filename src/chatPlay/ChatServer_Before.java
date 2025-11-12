package chatPlay;


import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer_Before {
    private static final int PORT = 9999;
    
    // Key: Nickname, Value: BufferedWriter for sending messages
    private static final ConcurrentHashMap<String, BufferedWriter> clientWriters = new ConcurrentHashMap<>();

    // 서버 시작 및 클라이언트 연결 대기
    public static void main(String[] args) {
        System.out.println("--- 채팅 서버가 포트 " + PORT + "에서 시작되었습니다. ---");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start(); // 각 클라이언트를 별도 스레드로 처리
            }
        } catch (IOException e) { System.err.println("서버 소켓 오류 발생: " + e.getMessage());}
    }

    // 일반 메시지를 모든 클라이언트에게 전송 ([sender] message 형태로 포맷)
    public static void broadcast(String sender, String message) {
        String fullMessage = "[" + sender + "] " + message;
        clientWriters.forEach((name, writer) -> {
            try {
                writer.write(fullMessage + "\n");
                writer.flush();
            } catch (IOException e) {}});
    }

    // TYPING과 같은 특수 신호를 발신자를 제외한 모든 클라이언트에게 전송
    public static void broadcastSignal(String sender, String signal) {
        String fullSignal = "[Signal:" + sender + "]" + signal;
        clientWriters.forEach((name, writer) -> {
            if (!name.equals(sender)) {
                try {
                    writer.write(fullSignal + "\n");
                    writer.flush();
                } catch (IOException e) {}}});
    }

    // 현재 접속 중인 모든 클라이언트에게 사용자 목록을 전송 ([Server] OnlineUsers:list)
    public static void sendUserList() {
        String userListString = String.join(",", clientWriters.keySet());
        String fullMessage = "[Server] OnlineUsers:" + userListString;
        
        clientWriters.forEach((name, writer) -> {
            try {
                writer.write(fullMessage + "\n");
                writer.flush();
            } catch (IOException e) {}});
    }

    // 클라이언트와의 통신 및 메시지 처리를 담당하는 내부 클래스
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String nickname;
        private BufferedReader in;
        private BufferedWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // 1. 닉네임 등록 루프 (중복 확인 및 OK/FAIL 응답)
                boolean nickAccepted = false;
                while (!nickAccepted) {
                    out.write("닉네임을 입력하세요: \n");
                    out.flush();
                    
                    String inputNick = in.readLine();
                    if (inputNick == null || inputNick.trim().isEmpty()) break;
                    
                    String desiredNickname = inputNick.trim();

                    if (clientWriters.containsKey(desiredNickname)) {
                        out.write("FAIL_NICK\n");
                        out.flush();
                    } else {
                        out.write("OK_NICK\n");
                        out.flush();
                        this.nickname = desiredNickname;
                        clientWriters.put(this.nickname, out);
                        nickAccepted = true;
                        sendUserList(); // 등록 성공 시 목록 업데이트
                    }
                }
                
                if (nickname == null) return; // 닉네임 등록 실패/연결 끊김

                broadcast("Server", this.nickname + " 님이 채팅에 참여했습니다.");

                // 2. 메인 메시지 수신 루프
                String message;
                while ((message = in.readLine()) != null) {
                    String trimmedMessage = message.trim();

                    if ("bye".equalsIgnoreCase(trimmedMessage)) {
                        break;
                    } 
                    
                    // TYPING 신호 처리
                    else if (trimmedMessage.startsWith("TYPING:")) {
                        broadcastSignal(this.nickname, trimmedMessage);
                        continue; 
                    }
                    
                    broadcast(this.nickname, trimmedMessage);
                }

            } catch (IOException e) {
                // 연결 오류 발생 시 처리
            } finally {
                // 3. 연결 정리 및 퇴장 방송
                if (nickname != null) {
                    clientWriters.remove(nickname);
                    broadcast("Server", nickname + " 님이 채팅에서 나갔습니다.");
                    sendUserList(); // 퇴장 후 목록 업데이트
                }
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }
}