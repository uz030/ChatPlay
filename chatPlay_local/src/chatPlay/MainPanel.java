package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder; // 🔹 <<-- 이 줄이 추가되었습니다!
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class MainPanel extends JPanel {

    private JTextField txtUserName;
    private ChatClientMain parentFrame;
    private JLabel selectedProfileImageLabel;
    private ImageIcon selectedIcon;
    private ImageIcon defaultProfileIcon;


    public MainPanel(ChatClientMain parentFrame) {
        this.parentFrame = parentFrame;

        setLayout(null);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setOpaque(false);

        try {
            defaultProfileIcon = new ImageIcon(ChatClientMain.class.getResource("/images/basic_profile.png"));
            selectedIcon = defaultProfileIcon;
        } catch (Exception e) {
            System.err.println("⚠️ 기본 프로필 이미지 로드 실패!");
            defaultProfileIcon = new ImageIcon();
            selectedIcon = defaultProfileIcon;
        }

        selectedProfileImageLabel = new JLabel(getScaledIcon(selectedIcon, 115, 115));
        selectedProfileImageLabel.setBounds(135, 95, 115, 115);
        add(selectedProfileImageLabel);

        JLabel defaultOptionLabel = new JLabel(getScaledIcon(defaultProfileIcon, 60, 60));
        defaultOptionLabel.setBounds(125, 220, 60, 60);
        defaultOptionLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(defaultOptionLabel);

        JLabel addOptionLabel = new JLabel();
        addOptionLabel.setBounds(205, 220, 60, 60);
        addOptionLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        try {
            ImageIcon addIcon = new ImageIcon(ChatClientMain.class.getResource("/images/add.png"));
            addOptionLabel.setIcon(getScaledIcon(addIcon, 60, 60));
        } catch (Exception e) {
            addOptionLabel.setText("[+]");
            addOptionLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        add(addOptionLabel);

        JLabel lbl = new JLabel("이름을 입력해주세요.");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lbl.setBounds(100, 290, 200, 33);
        add(lbl);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(120, 325, 150, 33);
        txtUserName.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 220)));
        add(txtUserName);

        RoundedButton btnConnect = new RoundedButton("회원가입",
                new Color(200, 210, 255),
                new Color(170, 185, 255),
                new Color(40, 50, 80));
        btnConnect.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        btnConnect.setBounds(93, 380, 205, 50);
        add(btnConnect);


        // 마우스 리스너 (기본 프로필)
        defaultOptionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIcon = defaultProfileIcon;
                selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
            }
        });

        // 마우스 리스너 (사진 추가)
        addOptionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("프로필 사진 선택");
                fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (jpg, png, gif)", "jpg", "png", "gif", "bmp"));
                fileChooser.setAcceptAllFileFilterUsed(false);
                int result = fileChooser.showOpenDialog(MainPanel.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIcon customIcon = new ImageIcon(selectedFile.getAbsolutePath());
                    selectedIcon = customIcon;
                    selectedProfileImageLabel.setIcon(getScaledIcon(selectedIcon, 115, 115));
                }
            }
        });

        // 회원가입 버튼 리스너
        btnConnect.addActionListener(e -> {
            String username = txtUserName.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "이름을 입력해주세요!");
                return;
            }
            parentFrame.connectToServer(username, selectedIcon);
        });
    }

    private ImageIcon getScaledIcon(ImageIcon srcIcon, int w, int h) {
        if (srcIcon == null || srcIcon.getImage() == null) {
            return new ImageIcon();
        }
        Image srcImg = srcIcon.getImage();
        Image scaledImg = srcImg.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(226, 230, 255),
                getWidth(), getHeight(), new Color(245, 245, 255)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}