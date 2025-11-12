package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class MainPanel extends JPanel {

    private JTextField txtUserName;
    private ChatClientMain parentFrame;

    // 고정 서버 주소
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 30000;

    public MainPanel(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(null);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        // ============================
        // 🔹 프로필 이미지
        // ============================
        ImageIcon icon = new ImageIcon(ChatClientMain.class.getResource("/images/basic_profile.png"));
        Image scaled = icon.getImage().getScaledInstance(115, 115, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        imgLabel.setBounds(135, 95, 115, 115);
        add(imgLabel);

        // ============================
        // 🔹 안내 문구
        // ============================
        JLabel lbl = new JLabel("이름을 입력해주세요.");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lbl.setBounds(100, 235, 200, 33);
        add(lbl);

        // ============================
        // 🔹 이름 입력창
        // ============================
        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(120, 275, 150, 33);
        txtUserName.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 220)));
        add(txtUserName);

        // ============================
        // 🔹 회원가입 버튼
        // ============================
        RoundedButton btnConnect = new RoundedButton("회원가입",
                new Color(200, 210, 255),
                new Color(170, 185, 255),
                new Color(40, 50, 80));
        btnConnect.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnConnect.setBounds(93, 340, 205, 50);
        add(btnConnect);

        // ============================
        // 🔹 버튼 클릭 이벤트
        // ============================
        btnConnect.addActionListener(e -> {
            String username = txtUserName.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름을 입력해주세요!");
                return;
            }

            // 서버 연결 시도
            try {
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // ✅ 서버가 기대하는 프로토콜 형식에 맞게 전송
                out.writeUTF("/login " + username);
                out.flush();

                System.out.println("✅ 서버 연결 성공: " + username);
                JOptionPane.showMessageDialog(this, "서버 연결 성공!", "Connected",
                        JOptionPane.INFORMATION_MESSAGE);

                // ✅ 로그인 후 홈화면으로 전환
                parentFrame.switchToHome(username);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "서버 연결 실패!\n(" + ex.getMessage() + ")",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ============================
    // 🔹 그라데이션 배경
    // ============================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(226, 233, 255),
                getWidth(), getHeight(), new Color(241, 245, 255)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}
