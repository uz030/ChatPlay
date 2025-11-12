package chatPlay;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private final Color baseColor;
    private final Color hoverColor;
    private final Color textColor;

    public RoundedButton(String text, Color baseColor, Color hoverColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        decorate();
    }

    protected void decorate() {
        setBorderPainted(false);
        setOpaque(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setForeground(textColor);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 상태에 따라 색상 변경
        if (getModel().isArmed()) {
            graphics.setColor(hoverColor.darker());
        } else if (getModel().isRollover()) {
            graphics.setColor(hoverColor);
        } else {
            graphics.setColor(baseColor);
        }

        // 둥근 버튼 배경
        graphics.fillRoundRect(0, 0, width, height, 25, 25);

        // 텍스트 중앙 정렬
        FontMetrics fm = graphics.getFontMetrics();
        Rectangle stringBounds = fm.getStringBounds(this.getText(), graphics).getBounds();
        int textX = (width - stringBounds.width) / 2;
        int textY = (height - stringBounds.height) / 2 + fm.getAscent();

        graphics.setColor(textColor);
        graphics.setFont(getFont());
        graphics.drawString(getText(), textX, textY);

        graphics.dispose();
        super.paintComponent(g);
    }
}