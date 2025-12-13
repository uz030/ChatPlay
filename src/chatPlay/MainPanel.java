package chatPlay;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * 로그인 화면 패널
 * 사용자명 입력 및 프로필 이미지 선택 기능 제공
 */
@SuppressWarnings("unused")
public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatClientMain parentFrame;
    private JTextField txtUserName;
    private JLabel selectedProfileImageLabel;
    private ImageIcon selectedIcon;
    private ImageIcon defaultProfileIcon;

    /**
     * 로그인 패널 생성자
     * @param parentFrame 부모 클라이언트 프레임
     */
    public MainPanel(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        try {
            defaultProfileIcon = new ImageIcon(getClass().getResource("/images/basic_profile.png"));
            selectedIcon = defaultProfileIcon;
        } catch (Exception e) {
            defaultProfileIcon = new ImageIcon();
            selectedIcon = defaultProfileIcon;
        }

        // ========== 프로필 이미지 선택 영역 ==========
        // 선택된 프로필 이미지 표시 (큰 이미지)
        selectedProfileImageLabel = new JLabel(getScaledIcon(selectedIcon, 115, 115));
        selectedProfileImageLabel.setBounds(135, 95, 115, 115);
        add(selectedProfileImageLabel);

        // 기본 프로필 이미지 옵션
        JLabel defaultOption = new JLabel(getScaledIcon(defaultProfileIcon, 60, 60));
        defaultOption.setBounds(125, 220, 60, 60);
        defaultOption.setCursor(new Cursor(Cursor.HAND_CURSOR));
        defaultOption.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 기본 프로필 이미지로 변경
                selectedIcon = defaultProfileIcon;
                selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
            }
        });
        add(defaultOption);

        // 사용자 이미지 추가 옵션
        JLabel addOption = new JLabel("[+]");
        try {
            ImageIcon addIcon = new ImageIcon(getClass().getResource("/images/add.png"));
            addOption.setIcon(getScaledIcon(addIcon, 60, 60));
        } catch (Exception e) {}
        addOption.setBounds(205, 220, 60, 60);
        addOption.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addOption.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 파일 선택 다이얼로그 열기
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
                if (chooser.showOpenDialog(MainPanel.this) == JFileChooser.APPROVE_OPTION) {
                    // 선택한 이미지로 프로필 이미지 변경
                    selectedIcon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
                    selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
                }
            }
        });
        add(addOption);

        // ========== 사용자명 입력 영역 ==========
        JLabel lbl = new JLabel("이름을 입력해주세요.");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lbl.setBounds(100, 290, 200, 33);
        add(lbl);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(120, 325, 150, 33);
        add(txtUserName);

        // ========== 서버 연결 버튼 ==========
        RoundedButton btnConnect = new RoundedButton("회원가입", new Color(200, 210, 255), new Color(170, 185, 255), new Color(40, 50, 80));
        btnConnect.setBounds(93, 380, 205, 50);
        btnConnect.addActionListener(e -> {
            // 사용자명이 입력되었으면 서버에 연결
            String name = txtUserName.getText().trim();
            if (!name.isEmpty()) parentFrame.connectToServer(name, selectedIcon);
        });
        add(btnConnect);
    }

    /**
     * 이미지 크기 조절
     * @param src 원본 이미지
     * @param w 목표 너비
     * @param h 목표 높이
     * @return 크기 조절된 이미지
     */
    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) return new ImageIcon();
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(226, 230, 255), getWidth(), getHeight(), new Color(245, 245, 255)));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}