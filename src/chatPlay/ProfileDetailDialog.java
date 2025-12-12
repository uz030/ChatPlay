package chatPlay;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfileDetailDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private UserProfile profile;
    private JLabel bgLabel, imgLabel;
    private JTextField statusField;

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
                profile.setStatusMessage(newStatus);
                
                // 서버에 상태메시지 업데이트 전송
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

    private void changeImage(boolean isBg) {
        JFileChooser ch = new JFileChooser();
        ch.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
        
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ImageIcon icon = new ImageIcon(ch.getSelectedFile().getAbsolutePath());
            
            if (isBg) {
                profile.setBackgroundImage(icon);
                bgLabel.setIcon(getScaledIcon(icon, 320, 220));
                
                // ✅ 서버에 배경 이미지 업로드
                uploadToServer("background", icon);
                
            } else {
                profile.setIcon(icon);
                imgLabel.setIcon(getScaledIcon(icon, 80, 80));
                
                // ✅ 서버에 프로필 이미지 업로드
                uploadToServer("icon", icon);
            }
        }
    }
    
    private void uploadToServer(String imageType, ImageIcon icon) {
        // owner가 ChatClientMain인지 확인
        Frame owner = (Frame) getOwner();
        if (owner instanceof ChatClientMain) {
            ChatClientMain parent = (ChatClientMain) owner;
            parent.uploadProfileImage(imageType, icon);
        }
    }
    
    private void updateStatusToServer(String statusMessage) {
        // owner가 ChatClientMain인지 확인
        Frame owner = (Frame) getOwner();
        if (owner instanceof ChatClientMain) {
            ChatClientMain parent = (ChatClientMain) owner;
            parent.updateStatusMessage(statusMessage);
        }
    }


    private ImageIcon getScaledIcon(ImageIcon src, int w, int h) {
        if (src == null) return null;
        return new ImageIcon(src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}
