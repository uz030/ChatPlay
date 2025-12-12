package chatPlay;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 프로필 상세 정보를 보여주고 수정할 수 있는 다이얼로그
 * 내 프로필인 경우 상태메시지와 이미지 수정 가능
 */
public class ProfileDetailDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private UserProfile profile;
    private JLabel bgLabel, imgLabel;
    private JTextField statusField;

    /**
     * 프로필 상세 다이얼로그 생성
     * @param owner 부모 프레임
     * @param profile 표시할 프로필 정보
     * @param isMe 내 프로필 여부 (true면 수정 가능)
     */
    public ProfileDetailDialog(Frame owner, UserProfile profile, boolean isMe) {
        super(owner, "프로필", true);
        this.profile = profile;

        setSize(320, 500);
        setLocationRelativeTo(owner);

        JLayeredPane layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        bgLabel = new JLabel(getScaledIcon(profile.getBackgroundImage(), 320, 220));
        bgLabel.setBounds(0, 0, 320, 220);
        layeredPane.add(bgLabel, JLayeredPane.DEFAULT_LAYER);

        JPanel infoPanel = new JPanel(null);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBounds(0, 220, 320, 280);
        layeredPane.add(infoPanel, JLayeredPane.DEFAULT_LAYER);

        imgLabel = new JLabel(getScaledIcon(profile.getIcon(), 80, 80));
        imgLabel.setBounds(120, 180, 80, 80);
        imgLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        layeredPane.add(imgLabel, JLayeredPane.PALETTE_LAYER);

        JLabel nameLabel = new JLabel(profile.getUsername(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nameLabel.setBounds(0, 50, 320, 30);
        infoPanel.add(nameLabel);

        statusField = new JTextField(profile.getStatusMessage());
        statusField.setHorizontalAlignment(SwingConstants.CENTER);
        statusField.setBounds(40, 100, 240, 30);
        statusField.setEditable(isMe);
        statusField.setBorder(isMe ? BorderFactory.createLineBorder(Color.GRAY) : BorderFactory.createEmptyBorder());
        infoPanel.add(statusField);

        if (isMe) {
            JButton btnSave = new JButton("저장");
            btnSave.setBounds(110, 150, 100, 30);

            btnSave.addActionListener(e -> {
                String newStatus = statusField.getText();
                // 1. 로컬 프로필 객체에 상태메시지 저장
                profile.setStatusMessage(newStatus);
                
                // 2. 서버로 상태메시지 변경 전송 (다른 사용자에게 동기화)
                updateStatusToServer(newStatus);
                
                dispose();
            });

            infoPanel.add(btnSave);

            bgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            bgLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    changeImage(true);
                }
            });

            imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imgLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    changeImage(false);
                }
            });
        }
    }

    /**
     * 프로필 이미지 또는 배경 이미지 변경
     * @param isBg true면 배경 이미지, false면 프로필 이미지
     */
    private void changeImage(boolean isBg) {
        JFileChooser ch = new JFileChooser();
        ch.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
        
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ImageIcon icon = new ImageIcon(ch.getSelectedFile().getAbsolutePath());
            
            if (isBg) {
                profile.setBackgroundImage(icon);
                bgLabel.setIcon(getScaledIcon(icon, 320, 220));
                uploadToServer("background", icon);
            } else {
                profile.setIcon(icon);
                imgLabel.setIcon(getScaledIcon(icon, 80, 80));
                uploadToServer("icon", icon);
            }
        }
    }
    
    /**
     * 프로필 이미지를 서버로 업로드
     * @param imageType "icon" 또는 "background"
     * @param icon 업로드할 이미지
     */
    private void uploadToServer(String imageType, ImageIcon icon) {
        Frame owner = (Frame) getOwner();
        if (owner instanceof ChatClientMain) {
            ChatClientMain parent = (ChatClientMain) owner;
            parent.uploadProfileImage(imageType, icon);
        }
    }
    
    /**
     * 상태메시지 변경을 서버로 전송
     * 서버에서 모든 클라이언트에게 브로드캐스트하여 동기화
     * @param statusMessage 변경할 상태메시지
     */
    private void updateStatusToServer(String statusMessage) {
        Frame owner = (Frame) getOwner();
        if (owner instanceof ChatClientMain) {
            ChatClientMain parent = (ChatClientMain) owner;
            parent.updateStatusMessage(statusMessage);
        }
    }


    /**
     * 이미지 크기 조절 헬퍼 메서드
     * @param src 원본 이미지
     * @param w 목표 너비
     * @param h 목표 높이
     * @return 크기 조절된 이미지 아이콘
     */
    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) return null;
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}
