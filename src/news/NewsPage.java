package news;

import javax.swing.*;
import chatPlay.ScrollUtil;
import java.awt.*;
import java.util.List;

public class NewsPage extends JPanel {
    private static final long serialVersionUID = 1L;
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

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(189, 203, 255), // 시작색 (연보라)
                    getWidth(), 0, new Color(241, 245, 255) // 끝색 (연한 하늘)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 60));
        header.setOpaque(false);

        // 1. 왼쪽: 뒤로가기 버튼
        JButton backButton = new JButton("←");
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setPreferredSize(new Dimension(60, 60)); // 크기 고정
        backButton.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });

        // 2. 가운데: 제목
        JLabel titleLabel = new JLabel(keyword + " 뉴스", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setForeground(Color.DARK_GRAY);

        // 3. 오른쪽: 균형을 맞추기 위한 빈 패널 (왼쪽 버튼과 동일한 크기)
        JPanel dummyPanel = new JPanel();
        dummyPanel.setOpaque(false);
        dummyPanel.setPreferredSize(new Dimension(60, 60)); // 뒤로가기 버튼과 같은 너비

        // 헤더에 배치
        header.add(backButton, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(dummyPanel, BorderLayout.EAST); // 오른쪽 빈 공간 추가 -> 제목이 정확히 가운데로 옴

        add(header, BorderLayout.NORTH);

        // 뉴스 리스트 영역
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
                    // 에러 발생 시 처리
                    listPanel.removeAll();
                    listPanel.add(new JLabel("데이터 로드 실패"));
                    listPanel.revalidate();
                    listPanel.repaint();
                }
            }
        }.execute();
    }

    private void updateList(List<NewsItem> items) {
        listPanel.removeAll();
        if (items == null || items.isEmpty()) {
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