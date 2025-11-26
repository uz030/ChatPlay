package chatPlay;
import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

	private static final long serialVersionUID = 1L;
	private Color baseColor, hoverColor, textColor;

    public RoundedButton(String text, Color base, Color hover, Color textCol) {
        super(text);
        this.baseColor = base; this.hoverColor = hover; this.textColor = textCol;
        setBorderPainted(false); setFocusPainted(false); setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isRollover() ? hoverColor : baseColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
        
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        Rectangle r = fm.getStringBounds(getText(), g2).getBounds();
        g2.drawString(getText(), (getWidth()-r.width)/2, (getHeight()-r.height)/2 + fm.getAscent());
    }
}