package chatPlay;

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UserProfile {
    private String username;
    private ImageIcon icon;
    private String statusMessage;
    private ImageIcon backgroundImage;

    public UserProfile(String username) {
        this(username, null);
    }

    public UserProfile(String username, ImageIcon icon) {
        this.username = username;
        this.icon = icon;
        this.statusMessage = "상태 메시지를 입력하세요.";
        this.backgroundImage = createDefaultBackground();
    }

    private ImageIcon createDefaultBackground() {
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, 0, 300, 200);
        g.dispose();
        return new ImageIcon(img);
    }

    @Override
    public String toString() { return username; }

    public String getUsername() { return username; }
    public ImageIcon getIcon() { return icon; }
    public void setIcon(ImageIcon icon) { this.icon = icon; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String msg) { this.statusMessage = msg; }

    public ImageIcon getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(ImageIcon bg) { this.backgroundImage = bg; }
}
