package chatPlay;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CustomScrollBarUI extends BasicScrollBarUI {

    private final int BAR_THICKNESS = 6;   // 전체 스크롤바 두께
    private final int THUMB_ARC = 8;       // thumb radius

    private BufferedImage thumbImage;

    public CustomScrollBarUI(BufferedImage thumbImage) {
        this.thumbImage = thumbImage;
    }

    @Override
    protected void configureScrollBarColors() {
        this.trackColor = new Color(255, 255, 255);
        this.thumbColor = new Color(180, 180, 180);
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setBorder(null);
        button.setFocusable(false);
        button.setVisible(false);
        return button;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (!scrollbar.isEnabled() || thumbBounds.width > thumbBounds.height) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (thumbImage != null) {
            g2.drawImage(thumbImage, thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, null);
        } else {
            g2.setColor(new Color(180, 180, 180));
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, THUMB_ARC, THUMB_ARC);
        }
        g2.dispose();
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
            return new Dimension(BAR_THICKNESS, super.getPreferredSize(c).height);
        } else {
            return new Dimension(super.getPreferredSize(c).width, BAR_THICKNESS);
        }
    }
}
