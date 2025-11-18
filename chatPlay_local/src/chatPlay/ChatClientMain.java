package chatPlay;

import java.awt.EventQueue;
import java.awt.CardLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ChatClientMain extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainPanel mainPanel;
    private ChatHome homePanel;

    // 네트워크 및 사용자 정보 (UserProfile 객체로 관리)
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    // '나'의 정보를 UserProfile 객체로 관리
    private UserProfile myProfile;

    // 친구 목록을 <UserProfile>로 관리
    private DefaultListModel<UserProfile> userListModel;

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 30000;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ChatClientMain frame = new ChatClientMain();
            frame.setVisible(true);
        });
    }

    public ChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat Program");
        setSize(392, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        setContentPane(mainContainer);

        // 🔹 [변경] UserProfile 모델로 초기화
        userListModel = new DefaultListModel<>();

        mainPanel = new MainPanel(this);
        homePanel = new ChatHome(this);

        mainContainer.add(mainPanel, "Main");
        mainContainer.add(homePanel, "Home");

        cardLayout.show(mainContainer, "Main");
    }

    // MainPanel 호출할 서버 연결 메서드
    public void connectToServer(String username, ImageIcon icon) {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("/login " + username);
            dos.flush();

            // '나'의 정보를 UserProfile 객체로 생성
            this.myProfile = new UserProfile(username, icon);

            System.out.println("✅ 서버 연결 성공: " + username);
            JOptionPane.showMessageDialog(this, "서버 연결 성공!", "Connected", JOptionPane.INFORMATION_MESSAGE);

            new Thread(this::listenToServer).start();

            switchToHome();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "서버 연결 실패!\n(" + ex.getMessage() + ")",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //서버 리스너 스레드
    private void listenToServer() {
        try {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("From Server: " + msg);

                if (msg.startsWith("/userlist ")) {
                    String[] users = msg.substring(10).split(",");

                    SwingUtilities.invokeLater(() -> {
                        userListModel.clear(); // 🔹 목록 초기화

                        // (TODO: [2단계] 현재는 서버가 이름만 보내주지만,
                        // 나중엔 "이름;상태;아이콘URL" 등을 파싱해야 함)

                        for (String user : users) {
                            String trimmedUser = user.trim();
                            if (!trimmedUser.isEmpty()) {

                                // 본인 이름(myProfile.getUsername())은 목록에 추가하지 않음
                                if (!trimmedUser.equals(myProfile.getUsername())) {

                                    userListModel.addElement(new UserProfile(trimmedUser));
                                }
                            }
                        }
                    });
                } else {
                    // (TODO) 채팅 메시지 처리
                }
            }
        } catch (IOException e) {
            System.out.println("서버와 연결이 끊겼습니다.");
        }
    }

    public void switchToHome() {
        cardLayout.show(mainContainer, "Home");
    }

    public void switchToMain() {
        cardLayout.show(mainContainer, "Main");
        try { if (socket != null) socket.close(); } catch (IOException _) {}
    }

    // Getter 메서드
    public DataOutputStream getDos() { return dos; }

    // '나'의 정보를 객체로 반환
    public UserProfile getMyProfile() { return myProfile; }

    // UserProfile 모델 반환
    public DefaultListModel<UserProfile> getUserListModel() { return userListModel; }
}