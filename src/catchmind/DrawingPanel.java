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
    private boolean isEraser = false;
    
    public interface DrawEventListener {
        void onDraw(int x1, int y1, int x2, int y2, Color color, int size);
        void onClear();
    }
    
    public DrawingPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 500));
        
        // 컴포넌트가 표시될 때 캔버스 초기화
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (canvas == null) {
                    initCanvas();
                }
            }
            
            @Override
            public void componentResized(ComponentEvent e) {
                if (canvas == null) {
                    initCanvas();
                }
            }
        });
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canDraw) {
                    showCannotDrawMessage();
                    return;
                }
                lastX = e.getX();
                lastY = e.getY();
                
                // 클릭 시에도 점 찍기
                drawLine(lastX, lastY, lastX, lastY, penColor, penSize);
                if (drawListener != null) {
                    drawListener.onDraw(lastX, lastY, lastX, lastY, penColor, penSize);
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!canDraw) return;
                
                int x = e.getX();
                int y = e.getY();
                
                // 캔버스 경계 체크
                if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
                    return;
                }
                
                drawLine(lastX, lastY, x, y, penColor, penSize);
                
                if (drawListener != null) {
                    drawListener.onDraw(lastX, lastY, x, y, penColor, penSize);
                }
                
                lastX = x;
                lastY = y;
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (canDraw) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
    
    public void initCanvas() {
        int w = getWidth();
        int h = getHeight();
        
        // 크기가 0이면 기본값 사용
        if (w <= 0) w = 700;
        if (h <= 0) h = 500;
        
        canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        
        // 안티앨리어싱 적용
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        // 흰색 배경
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
        clearCanvasLocal();
    }
    
    public void clearCanvasLocal() {
        if (g2d == null) initCanvas();
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        repaint();
    }
    
    public void clearCanvasAndNotify() {
        clearCanvasLocal();
        
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
    
    private void showCannotDrawMessage() {
        // 간단한 피드백 (선택사항)
        JOptionPane.showMessageDialog(
            this,
            "당신의 차례가 아닙니다!",
            "알림",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;

        if (canDraw) {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }

        repaint();
    }

    public boolean canDraw() {
        return canDraw;
    }

    
    public void setPenSize(int size) {
        this.penSize = Math.max(1, Math.min(size, 50)); // 1~50 범위로 제한
    }
    
    public void setPenColor(Color color) {
        this.penColor = color;
        this.isEraser = color.equals(Color.WHITE);
    }
    
    public void setDrawListener(DrawEventListener listener) {
        this.drawListener = listener;
    }
    
    public boolean isEraser() {
        return isEraser;
    }
    
    public Color getPenColor() {
        return penColor;
    }
    
    public int getPenSize() {
        return penSize;
    }
}