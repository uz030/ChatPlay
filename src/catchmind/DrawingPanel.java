package catchmind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class DrawingPanel extends JPanel {

    private BufferedImage canvas;
    private Graphics2D g2d;
    private int penSize = 3;
    private Color penColor = Color.BLACK;
    private boolean canDraw = false;

    private int lastX, lastY;
    private DrawEventListener drawListener;

    public interface DrawEventListener {
        void onDraw(int x1, int y1, int x2, int y2, Color color, int size);
        void onClear();
    }

    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 400));

        // ✅ 컴포넌트가 표시될 때 캔버스 초기화
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (canvas == null) {
                    initCanvas();
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canDraw) return;
                lastX = e.getX();
                lastY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!canDraw) return;

                int x = e.getX();
                int y = e.getY();

                drawLine(lastX, lastY, x, y, penColor, penSize);

                if (drawListener != null) {
                    drawListener.onDraw(lastX, lastY, x, y, penColor, penSize);
                }

                lastX = x;
                lastY = y;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void initCanvas() {
        int w = getWidth();
        int h = getHeight();
        
        // ✅ 크기가 0이면 기본값 사용
        if (w <= 0) w = 600;
        if (h <= 0) h = 400;
        
        canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, w, h);
        repaint();
    }

    public void drawLine(int x1, int y1, int x2, int y2, Color color, int size) {
        if (g2d == null) initCanvas();

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }

    public void clearCanvas() {
        if (g2d == null) initCanvas();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        repaint();

        if (drawListener != null) {
            drawListener.onClear();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas != null) {
            g.drawImage(canvas, 0, 0, null);
        }
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
        setCursor(canDraw ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                          : Cursor.getDefaultCursor());
    }

    public void setPenSize(int size) { this.penSize = size; }
    public void setPenColor(Color color) { this.penColor = color; }
    public void setDrawListener(DrawEventListener listener) { this.drawListener = listener; }
}