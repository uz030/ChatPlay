package chatPlay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class ProfileDetailDialog extends JDialog {

    private UserProfile userProfile; // 표시할 대상 유저
    private boolean isMe; // 내 프로필(수정)인지, 친구 프로필(보기)인지

    private JLabel backgroundLabel;
    private JLabel profileImageLabel;
    private JTextField statusField;
    private JButton saveButton;

    public ProfileDetailDialog(Frame owner, UserProfile userProfile, boolean isMe) {
        super(owner, "프로필", true);
        this.userProfile = userProfile;
        this.isMe = isMe;

        setSize(320, 500);
        setLocationRelativeTo(owner);

        // JLayeredPane을 사용하여 컴포넌트 겹치기
        JLayeredPane layeredPane = new JLayeredPane();
        setContentPane(layeredPane); // JDialog의 컨텐츠 패널을 JLayeredPane으로 설정

        int dialogWidth = 320;
        int dialogHeight = 500;

        backgroundLabel = new JLabel();
        updateBackground(userProfile.getBackgroundImage());
        backgroundLabel.setBounds(0, 0, dialogWidth, 220);

        if (isMe) {
            backgroundLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            backgroundLabel.setToolTipText("클릭해서 배경 이미지 변경");
            backgroundLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    changeBackgroundImage();
                }
            });
        }
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // Layer 1: 하얀색 정보 패널
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(null);
        infoPanel.setBackground(Color.WHITE);

        // 배경(Y=220) 바로 아래부터 다이얼로그 끝까지
        infoPanel.setBounds(0, 220, dialogWidth, dialogHeight - 220);
        layeredPane.add(infoPanel, JLayeredPane.DEFAULT_LAYER); // Layer 0 (기본)

        // Layer 2: 프로필 이미지 (맨 앞)
        profileImageLabel = new JLabel(getScaledIcon(userProfile.getIcon(), 80, 80));

        // Y좌표 계산: infoPanel 시작(220) - 40 = 180
        profileImageLabel.setBounds(120, 180, 80, 80);
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        if (isMe) {
            profileImageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            profileImageLabel.setToolTipText("클릭해서 프로필 사진 변경");
            profileImageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    changeProfileImage();
                }
            });
        }
        // profileImageLabel을 layeredPane에 추가
        layeredPane.add(profileImageLabel, JLayeredPane.PALETTE_LAYER);

        // 이름
        JLabel nameLabel = new JLabel(userProfile.getUsername(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        // Y좌표: (프로필 사진의 겹쳐진 부분(40) + 여백(10) = 50)
        nameLabel.setBounds(0, 50, dialogWidth, 30);
        infoPanel.add(nameLabel); // infoPanel에 추가

        // 상태 메시지
        statusField = new JTextField(userProfile.getStatusMessage());
        statusField.setHorizontalAlignment(SwingConstants.CENTER);
        statusField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        // Y좌표: 이름(50) + 높이(30) + 여백(20) = 100
        statusField.setBounds(40, 100, 240, 30);
        statusField.setEditable(isMe);
        statusField.setBackground(isMe ? Color.WHITE : Color.LIGHT_GRAY);
        statusField.setBorder(isMe ? BorderFactory.createLineBorder(Color.GRAY) : BorderFactory.createEmptyBorder());
        infoPanel.add(statusField); // inPanel에 추가fo

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        if (isMe) {
            saveButton = new JButton("저장");
            saveButton.addActionListener(e -> saveChanges());
            buttonPanel.add(saveButton);
        }

        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // buttonPanel을 infoPanel 내부에 추가
        // Y좌표: 상태메시지(100) + 높이(30) + 여백(20) = 150
        // 높이: infoPanel의 나머지 공간
        int buttonPanelY = 150;
        buttonPanel.setBounds(0, buttonPanelY, dialogWidth, (dialogHeight - 220) - buttonPanelY);
        infoPanel.add(buttonPanel);
    }

    // 프로필 사진 변경 로직
    private void changeProfileImage() {
        ImageIcon newIcon = openImageChooser("프로필 사진 선택");
        if (newIcon != null) {
            profileImageLabel.setIcon(getScaledIcon(newIcon, 80, 80));
            profileImageLabel.putClientProperty("newIcon", newIcon);
        }
    }

    // 배경 사진 변경 로직
    private void changeBackgroundImage() {
        ImageIcon newBg = openImageChooser("배경 사진 선택");
        if (newBg != null) {
            updateBackground(newBg);
            backgroundLabel.putClientProperty("newBg", newBg);
        }
    }

    // (Helper) 이미지 파일 선택기
    private ImageIcon openImageChooser(String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (jpg, png, gif)", "jpg", "png", "gif", "bmp"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return new ImageIcon(selectedFile.getAbsolutePath());
        }
        return null;
    }

    // (Helper) 배경 업데이트
    private void updateBackground(ImageIcon bgIcon) {
        if (bgIcon != null) {
            backgroundLabel.setIcon(getScaledIcon(bgIcon, 320, 220));
        }
    }

    // (Helper) 아이콘 스케일링
    private ImageIcon getScaledIcon(ImageIcon srcIcon, int w, int h) {
        if (srcIcon == null) return null;
        Image scaled = srcIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    // '저장' 버튼 로직
    private void saveChanges() {
        ImageIcon newIcon = (ImageIcon) profileImageLabel.getClientProperty("newIcon");
        ImageIcon newBg = (ImageIcon) backgroundLabel.getClientProperty("newBg");

        if (newIcon != null) {
            userProfile.setIcon(newIcon);
        }
        if (newBg != null) {
            userProfile.setBackgroundImage(newBg);
        }
        userProfile.setStatusMessage(statusField.getText());

        JOptionPane.showMessageDialog(this, "프로필이 (로컬에) 저장되었습니다.");
        dispose();
    }
}