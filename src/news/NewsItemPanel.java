package news;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class NewsItemPanel extends JPanel {
    
    public NewsItemPanel(NewsItem item) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 10, 5, 10),
            BorderFactory.createLineBorder(new Color(230,230,230), 1)
        ));
        setBackground(Color.WHITE);

        // 제목
        JLabel titleLabel = new JLabel("<html><b>" + item.getTitle() + "</b></html>");
        titleLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 2, 5));

        // 내용 (간략)
        JLabel descLabel = new JLabel("<html><body style='width: 250px'>" + item.getDescription() + "</body></html>");
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        add(titleLabel, BorderLayout.NORTH);
        add(descLabel, BorderLayout.CENTER);
        
        // 클릭 시 브라우저 열기
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(item.getLink()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}