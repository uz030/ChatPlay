package weather;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HourlyWeatherItem extends JPanel {

    public HourlyWeatherItem(WeatherData d) {

        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(0, 70));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setPreferredSize(new Dimension(0, 70));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // 왼쪽: 시간 (13시)
        String timeStr = new SimpleDateFormat("HH시").format(new Date(d.dt * 1000));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 가운데: 아이콘 + 온도
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

        ImageIcon baseIcon = WeatherService.getIcon(d.icon);
        JLabel iconLabel;
        if (baseIcon != null) {
            Image img = baseIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(img));
        } else {
            iconLabel = new JLabel("N/A");
        }

        JLabel tempLabel = new JLabel((int)d.temp + "°C");
        tempLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        centerPanel.add(iconLabel);
        centerPanel.add(tempLabel);

        // 오른쪽: 설명 + 강수확률
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel descLabel = new JLabel(d.description);
        descLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        int popPercent = (int)(d.pop * 100);
        JLabel popLabel = new JLabel("강수확률 " + popPercent + "%");
        popLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        popLabel.setForeground(new Color(90, 90, 90));

        rightPanel.add(descLabel);
        rightPanel.add(popLabel);

        add(timeLabel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // 아래 구분선
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(220, 220, 220));
        g2.drawLine(20, getHeight()-1, getWidth()-20, getHeight()-1);
        g2.dispose();
        super.paintComponent(g);
    }
}
