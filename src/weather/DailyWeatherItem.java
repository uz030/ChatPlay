package weather;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyWeatherItem extends JPanel {

    public DailyWeatherItem(WeatherData d) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setPreferredSize(new Dimension(0, 90));

        // 카드 배경용 패널 하나 더 감싸기
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 왼쪽: 날짜
        String dateStr = new SimpleDateFormat("MM/dd (E)").format(new Date(d.dt * 1000));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 가운데: 아이콘 + 오늘 평균온도
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        centerPanel.setOpaque(false);

        ImageIcon baseIcon = WeatherService.getIcon(d.icon);
        JLabel iconLabel;
        if (baseIcon != null) {
            Image img = baseIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(img));
        } else {
            iconLabel = new JLabel("N/A");
        }

        JLabel tempLabel = new JLabel((int)d.tempDay + "°C");
        tempLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        centerPanel.add(iconLabel);
        centerPanel.add(tempLabel);

        // 오른쪽: 설명 + 최저/최고 + 강수확률
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel descLabel = new JLabel(d.description);
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        JLabel minmaxLabel = new JLabel(
                "최저 " + (int)d.tempMin + "° / 최고 " + (int)d.tempMax + "°"
        );
        minmaxLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        int popPercent = (int)(d.pop * 100);
        JLabel popLabel = new JLabel("강수확률 " + popPercent + "%");
        popLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        popLabel.setForeground(new Color(90, 90, 90));

        rightPanel.add(descLabel);
        rightPanel.add(minmaxLabel);
        rightPanel.add(popLabel);

        card.add(dateLabel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        add(card, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 하얀 카드 + 둥근 모서리 + 그림자 느낌
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 20;
        int x = 8;
        int y = 4;
        int w = getWidth() - 16;
        int h = getHeight() - 8;

        g2.setColor(new Color(240, 240, 240));
        g2.fillRoundRect(x+1, y+2, w, h, arc, arc); // 그림자

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setColor(new Color(230, 230, 230));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
