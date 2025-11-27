package weather;

import javax.swing.*;
import java.awt.*;

public class WeatherFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JPanel menuPage;
    private TodayWeatherPage todayPage;
    private WeekWeatherPage weekPage;

    public WeatherFrame() {
        super("날씨 예보");

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // 메뉴 페이지 (버튼 2개 정도 있을 거라고 가정)
        menuPage = createMenuPage();

        // 🔹 수정된 부분: onBack 으로 showMenu 전달
        todayPage = new TodayWeatherPage(() -> showMenu());
        weekPage  = new WeekWeatherPage(() -> showMenu());

        cardPanel.add(menuPage, "menu");
        cardPanel.add(todayPage, "today");
        cardPanel.add(weekPage, "week");

        setContentPane(cardPanel);
        setSize(400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private JPanel createMenuPage() {
        JPanel p = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnToday = new JButton("오늘의 날씨");
        JButton btnWeek  = new JButton("7일 예보");

        btnToday.addActionListener(e -> showToday());
        btnWeek.addActionListener(e -> showWeek());

        p.add(btnToday);
        p.add(btnWeek);
        return p;
    }

    public void showMenu() {
        cardLayout.show(cardPanel, "menu");
    }

    public void showToday() {
        cardLayout.show(cardPanel, "today");
    }

    public void showWeek() {
        cardLayout.show(cardPanel, "week");
    }
}
