package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public class CircularProfileIcon {
    
    public static ImageIcon createCircularIcon(ImageIcon icon, int size) {
        if (icon == null) {
            return createDefaultCircularIcon(size);
        }
        
        // 원본 이미지를 size x size로 리사이즈
        Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        
        // 투명한 BufferedImage 생성
        BufferedImage circularImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImg.createGraphics();
        
        // 안티앨리어싱 설정
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // 원형 클리핑 영역 설정
        Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(circle);
        
        // 이미지 그리기
        g2.drawImage(img, 0, 0, null);
        
        g2.dispose();
        
        return new ImageIcon(circularImg);
    }
    
    public static ImageIcon createDefaultCircularIcon(int size) {
        BufferedImage circularImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImg.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 회색 원 배경
        g2.setColor(new Color(180, 180, 180));
        g2.fillOval(0, 0, size, size);
        
        // 흰색 사람 아이콘 그리기
        g2.setColor(Color.WHITE);
        
        // 머리 (원)
        int headSize = size / 3;
        int headX = (size - headSize) / 2;
        int headY = size / 4;
        g2.fillOval(headX, headY, headSize, headSize);
        
        // 몸통 (반원/타원)
        int bodyWidth = (int)(size * 0.7);
        int bodyHeight = (int)(size * 0.5);
        int bodyX = (size - bodyWidth) / 2;
        int bodyY = (int)(size * 0.55);
        g2.fillOval(bodyX, bodyY, bodyWidth, bodyHeight);
        
        g2.dispose();
        
        return new ImageIcon(circularImg);
    }
    
    public static void setCircularIcon(JLabel label, ImageIcon icon, int size) {
        label.setIcon(createCircularIcon(icon, size));
    }
}