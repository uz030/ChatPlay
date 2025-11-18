package chatPlay;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class UserProfile {

    private String username;
    private ImageIcon icon;
    private String statusMessage;
    private ImageIcon backgroundImage;

    // 1. 서버가 보내준 최소 정보(이름)로 생성할 때
    public UserProfile(String username) {
        this.username = username;
        this.icon = null;
        this.statusMessage = "";
        this.backgroundImage = createDefaultBackground();
    }

    // 2. 로그인 시 '내' 프로필을 생성할 때
    public UserProfile(String username, ImageIcon icon) {
        this.username = username;
        this.icon = icon;
        this.statusMessage = "상태 메시지를 입력하세요.";
        this.backgroundImage = createDefaultBackground();
    }

    // 기본 회색 배경
    private ImageIcon createDefaultBackground() {
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, 0, 300, 200);
        g.dispose();
        return new ImageIcon(img);
    }

    // JList에서 이름을 표시하기 위한 toString()
    @Override
    public String toString() {
        return username;
    }

    public String getUsername() { return username; }
    public ImageIcon getIcon() { return icon; }
    public String getStatusMessage() { return statusMessage; }
    public ImageIcon getBackgroundImage() { return backgroundImage; }

    public void setIcon(ImageIcon icon) { this.icon = icon; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    public void setBackgroundImage(ImageIcon backgroundImage) { this.backgroundImage = backgroundImage; }
}