package weather;

import javax.swing.*;

import chatPlay.ScrollUtil;

import java.awt.*;
import java.util.List;

public class WeekWeatherPage extends JPanel {

	private Runnable onBack;   // 🔹 뒤로가기 동작
    private JPanel listPanel;

    public WeekWeatherPage(Runnable onBack) {
        this.onBack = onBack;
        initUI();
        loadDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        HeaderPanel header = new HeaderPanel("7일 예보", onBack);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setBackground(Color.WHITE);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        listPanel.add(Box.createVerticalStrut(40));
        JLabel loading = new JLabel("날씨 정보를 불러오는 중입니다...", SwingConstants.CENTER);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(loading);
        
        JScrollPane scroll = new JScrollPane(listPanel);
        ScrollUtil.applyCustomScrollBar(scroll);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }


    private void loadDataAsync() {
        new SwingWorker<List<WeatherData>, Void>() {
            @Override
            protected List<WeatherData> doInBackground() throws Exception {
                return WeatherService.load7Days();
            }

            @Override
            protected void done() {
                try {
                    List<WeatherData> list = get();
                    updateList(list);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateList(List<WeatherData> list) {
        listPanel.removeAll();

        if (list == null || list.isEmpty()) {
            JLabel label = new JLabel("표시할 데이터가 없습니다.", SwingConstants.CENTER);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(label);
        } else {
            for (WeatherData d : list) {
                listPanel.add(new DailyWeatherItem(d));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}
