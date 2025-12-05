package news;

import javax.swing.*;
import chatPlay.ScrollUtil;
import weather.HeaderPanel; 
import java.awt.*;
import java.util.List;

public class NewsPage extends JPanel {
    private Runnable onBack;
    private JPanel listPanel;
    private String keyword;

    public NewsPage(String keyword, Runnable onBack) {
        this.keyword = keyword;
        this.onBack = onBack;
        initUI();
        loadDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 헤더 (키워드 + 뉴스)
        add(new HeaderPanel(keyword + " 뉴스", onBack), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listPanel);
        ScrollUtil.applyCustomScrollBar(scroll); 
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadDataAsync() {
        listPanel.add(Box.createVerticalStrut(20));
        JLabel loading = new JLabel("뉴스를 불러오는 중...", SwingConstants.CENTER);
        loading.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(loading);
        
        // 백그라운드 스레드에서 API 호출
        new SwingWorker<List<NewsItem>, Void>() {
            @Override
            protected List<NewsItem> doInBackground() throws Exception {
                return NewsService.searchNews(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<NewsItem> items = get();
                    updateList(items);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateList(List<NewsItem> items) {
        listPanel.removeAll();
        if (items.isEmpty()) {
            JLabel empty = new JLabel("검색 결과가 없습니다.", SwingConstants.CENTER);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(empty);
        } else {
            for (NewsItem item : items) {
                listPanel.add(new NewsItemPanel(item));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}