package chatPlay;

import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 사용자 프로필 정보를 담는 데이터 모델 클래스
 * 사용자명, 프로필 이미지, 상태메시지, 배경 이미지를 관리
 */
public class UserProfile {
    private String username;
    private ImageIcon icon;
    private String statusMessage;
    private ImageIcon backgroundImage;

    /**
     * 프로필 이미지 없이 사용자명만으로 생성
     */
    public UserProfile(String username) {
        this(username, null);
    }

    /**
     * 사용자명과 프로필 이미지로 생성
     * 기본 상태메시지와 배경 이미지를 자동 설정
     */
    public UserProfile(String username, ImageIcon icon) {
        this.username = username;
        this.icon = icon;
        this.statusMessage = "상태 메시지를 입력하세요.";
        this.backgroundImage = createDefaultBackground();
    }

    /**
     * 기본 배경 이미지 생성 (회색 배경)
     */
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

    // Getter/Setter 메서드들
    public String getUsername() { return username; }
    public ImageIcon getIcon() { return icon; }
    public void setIcon(ImageIcon icon) { this.icon = icon; }

    /**
     * 상태메시지 조회
     */
    public String getStatusMessage() { return statusMessage; }
    
    /**
     * 상태메시지 설정
     * 변경 시 서버로 동기화되어 다른 사용자에게도 반영됨
     */
    public void setStatusMessage(String msg) { this.statusMessage = msg; }

    public ImageIcon getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(ImageIcon bg) { this.backgroundImage = bg; }
}


