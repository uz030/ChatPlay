package chatPlay;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ChatServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket socket;
    private Socket client_socket;
    private Vector<UserService> UserVec = new Vector<>();


    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ChatServer frame = new ChatServer();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 264, 87, 26);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (NumberFormatException | IOException e1) {
                    e1.printStackTrace();
                }
                AppendText("Chat Server Running..");
                btnServerStart.setText("Chat Server Running..");
                btnServerStart.setEnabled(false);
                txtPortNumber.setEnabled(false);
                AcceptServer accept_server = new AcceptServer();
                accept_server.start();
            }
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    // AcceptServer 스레드
    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    AppendText("Waiting clients ...");
                    client_socket = socket.accept();
                    AppendText("새로운 참가자 from " + client_socket);

                    // 1. UserService 객체 생성
                    UserService new_user = new UserService(client_socket, ChatServer.this);

                    // 2. 사용자를 목록(Vec)에 먼저 추가
                    UserVec.add(new_user);
                    AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());

                    // 3. 스레드 시작 (메시지 수신 대기 시작)
                    new_user.start();

                    // 4. 사용자가 목록에 '추가된 후'에 입장 및 목록 방송
                    String br_msg = "[" + new_user.getUserName() + "]님이 입장 하였습니다.\n";
                    WriteAll(br_msg); // 입장 메시지 방송
                    broadcastUserList(); // 갱신된 친구 목록 방송

                } catch (IOException e) {
                    AppendText("!!!! accept 에러 발생... !!!!");
                }
            }
        }
    }

    public void AppendText(String str) {
        textArea.append(str + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    public synchronized void broadcastUserList() {
        String userListString = UserVec.stream()
                .map(UserService::getUserName)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.joining(","));

        String msg = "/userlist " + userListString;
        AppendText("Broadcasting: " + msg);
        WriteAll(msg + "\n");
    }

    public synchronized void WriteAll(String str) {
        for (int i = 0; i < UserVec.size(); i++) {
            UserService user = UserVec.get(i);
            user.WriteOne(str);
        }
    }


    class UserService extends Thread {
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;

        private String UserName = "";
        private ChatServer serverFrame;

        //UserService 생성자
        public UserService(Socket client_socket, ChatServer serverFrame) {
            this.client_socket = client_socket;
            this.serverFrame = serverFrame;

            try {
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);

                // 1. 로그인 메시지 수신 및 이름 설정
                String line1 = dis.readUTF(); // "/login UserName"
                String[] msg = line1.split(" ");
                UserName = msg[1].trim();
                AppendText("새로운 참가자 " + UserName + " 입장.");

                // 2. 본인에게만 환영 메시지 전송 (WriteOne)
                WriteOne("Welcome to Java chat server\n");
                WriteOne(UserName + "님 환영합니다.\n");

            } catch (Exception e) {
                AppendText("userService error: " + e.getMessage());
            }
        }

        public String getUserName() {
            return UserName;
        }

        public void logout() {
            UserVec.removeElement(this);
            String br_msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
            serverFrame.WriteAll(br_msg);
            AppendText("사용자 퇴장. 현재 참가자 수 " + UserVec.size());

            // 퇴장 시에도 목록 갱신 방송
            serverFrame.broadcastUserList();
        }

        public void WriteOne(String msg) {

            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                AppendText("dos.write() error");
                try {
                    dos.close();
                    dis.close();
                    client_socket.close();
                    logout();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        public void run() {

            while (true) {
                try {
                    String msg = dis.readUTF();
                    msg = msg.trim();
                    AppendText(msg);

                    String[] args = msg.split(" ");

                    if(args.length < 2) {
                        serverFrame.WriteAll(msg + "\n");
                        continue;
                    }

                    switch (args[1]) {
                        case "/exit":
                            logout();
                            return;
                        case "/list":
                            WriteOne("**현재 사용자 목록**\n");
                            for (int i = 0; i < serverFrame.UserVec.size(); i++) {
                                UserService user = serverFrame.UserVec.get(i);
                                WriteOne("- " + user.UserName + "\n");
                            }
                            break;
                        case "/to":
                            if (args.length < 4) {
                                WriteOne("사용법: /to [username] [message]\n");
                                break;
                            }
                            String targetUser = args[2];
                            String privateMessage = "";
                            for (int i = 3; i < args.length; i++) {
                                privateMessage += args[i];
                                if (i < args.length - 1) privateMessage += " ";
                            }
                            boolean found = false;
                            for (int i = 0; i < serverFrame.UserVec.size(); i++) {
                                UserService user = serverFrame.UserVec.get(i);
                                if (user.UserName.equals(targetUser)) {
                                    user.WriteOne("[" + UserName + "님의 귓속말] " + privateMessage + "\n");
                                    WriteOne("[" + UserName + "님의 귓속말] " + privateMessage + "\n");
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                WriteOne("사용자 " + targetUser + "를 찾을 수 없습니다.\n");
                            }
                            break;

                        default: // 일반 메시지
                            serverFrame.WriteAll(msg + "\n");
                            break;
                    }
                } catch (IOException e) {
                    AppendText("dis.readUTF() error");
                    try {
                        dos.close();
                        dis.close();
                        client_socket.close();
                        logout();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }
}