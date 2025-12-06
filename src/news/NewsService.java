package news;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class NewsService {

    private static final String CLIENT_ID = "CRblJv6kSEuc5aljHzSt"; 
    private static final String CLIENT_SECRET = "_rw8IOfk7Z";

    @SuppressWarnings("deprecation")
	public static List<NewsItem> searchNews(String keyword) {
        List<NewsItem> list = new ArrayList<>();
        try {
            String text = URLEncoder.encode(keyword, "UTF-8");
            
            // 검색어(keyword)로 뉴스 20개 검색, 유사도순(sim) 정렬
            String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + text + "&display=20&sort=sim";

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // JSON 파싱
            JSONObject root = new JSONObject(response.toString());
            JSONArray items = root.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                list.add(new NewsItem(
                    item.getString("title"),
                    item.getString("link"),
                    item.getString("description"),
                    item.getString("pubDate")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}