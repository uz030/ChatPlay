package news;

public class NewsItem {
    private String title;
    private String link;
    private String description;
    private String pubDate;

    public NewsItem(String title, String link, String description, String pubDate) {
        this.title = removeHtmlTag(title);
        this.link = link;
        this.description = removeHtmlTag(description);
        this.pubDate = pubDate;
    }

    // 네이버 API 반환값의 HTML 태그(<b> 등) 제거
    private String removeHtmlTag(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "")
                   .replaceAll("&quot;", "\"")
                   .replaceAll("&amp;", "&")
                   .replaceAll("&lt;", "<")
                   .replaceAll("&gt;", ">");
    }

    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getDescription() { return description; }
    public String getPubDate() { return pubDate; }
}