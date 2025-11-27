package weather;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private JButton backButton;
    private JLabel titleLabel;

    public HeaderPanel(String title, Runnable onBack) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 60));
        setOpaque(false);

        backButton = new JButton("←");
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        backButton.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(Color.DARK_GRAY);

        add(backButton, BorderLayout.WEST);
        add(titleLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 상단 그라데이션 배경
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c1 = new Color(189, 203, 255); // 연보라
        Color c2 = new Color(208, 235, 255); // 연파랑
        GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);

        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        super.paintComponent(g);
    }
}
