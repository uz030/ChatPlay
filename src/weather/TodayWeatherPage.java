package weather;

import javax.swing.*;

import chatPlay.ScrollUtil;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TodayWeatherPage extends JPanel {

    private Runnable onBack;   // 🔹 뒤로가기 동작을 넘겨받음
    private JPanel listPanel;
    private JLabel dateChip;

    public TodayWeatherPage(Runnable onBack) {
        this.onBack = onBack;
        initUI();
        loadDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 헤더
        HeaderPanel header = new HeaderPanel("오늘의 날씨", onBack);
        add(header, BorderLayout.NORTH);

        // 가운데: 날짜 칩 + 리스트
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());
        center.setBackground(Color.WHITE);

        // 날짜 칩
        JPanel datePanel = new JPanel();
        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String todayStr = new SimpleDateFormat("MM/dd (E)").format(new Date());
        dateChip = new JLabel(todayStr, SwingConstants.CENTER);
        dateChip.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        dateChip.setOpaque(true);
        dateChip.setBackground(Color.WHITE);
        dateChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)
        ));

        datePanel.add(dateChip);

        center.add(datePanel, BorderLayout.NORTH);

        // 리스트 패널
        listPanel = new JPanel();
        listPanel.setBackground(Color.WHITE);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        // 처음에는 "로딩중" 표시
        listPanel.add(Box.createVerticalStrut(40));
        JLabel loading = new JLabel("날씨 정보를 불러오는 중입니다...", SwingConstants.CENTER);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(loading);

        JScrollPane scroll = new JScrollPane(listPanel);
        ScrollUtil.applyCustomScrollBar(scroll);
        scroll.setBorder(null);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void loadDataAsync() {
        new SwingWorker<List<WeatherData>, Void>() {
            @Override
            protected List<WeatherData> doInBackground() throws Exception {
                return WeatherService.loadTodayHourly();
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
                listPanel.add(new HourlyWeatherItem(d));
            }
        }

        listPanel.add(Box.createVerticalGlue());

        
        listPanel.revalidate();
        listPanel.repaint();
    }
}
