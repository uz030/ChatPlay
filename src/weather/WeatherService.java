package weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {

    // TODO: 여기에 네 OpenWeather API 키 넣기
    private static final String API_KEY = "f7e34460ca4bc41240c22dffd006cce9";

    private static final double SEOUL_LAT = 37.5665;
    private static final double SEOUL_LON = 126.9780;

    // 아이콘 캐시
    private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();

    // -----------------------------
    // 공통: OneCall API 호출
    // -----------------------------
    private static JSONObject requestRoot() throws Exception {

        String urlStr =
            "https://api.openweathermap.org/data/3.0/onecall?"
                + "lat=" + SEOUL_LAT
                + "&lon=" + SEOUL_LON
                + "&units=metric"
                + "&lang=kr"
                + "&appid=" + API_KEY;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("HTTP Error: " + conn.getResponseCode());
        }

        BufferedReader br =
            new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        return new JSONObject(sb.toString());
    }

    // -----------------------------
    // 1) 오늘 기준 24시간 (hourly)
    // -----------------------------
    public static List<WeatherData> loadTodayHourly() throws Exception {
        JSONObject root = requestRoot();
        JSONArray hourlyArr = root.getJSONArray("hourly");

        List<WeatherData> list = new ArrayList<>();

        long now = System.currentTimeMillis() / 1000;

        // 오늘 23:59:59 만들기
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long todayEnd = cal.getTimeInMillis() / 1000;

        for (int i = 0; i < hourlyArr.length(); i++) {
            JSONObject h = hourlyArr.getJSONObject(i);
            long dt = h.getLong("dt");

            // ➤ 지금 이후 & 오늘까지
            if (dt >= now && dt <= todayEnd) {
                WeatherData d = new WeatherData();
                d.isHourly = true;

                d.dt   = dt;
                d.temp = h.getDouble("temp");
                d.pop  = h.optDouble("pop", 0.0); // 0~1

                JSONObject w = h.getJSONArray("weather").getJSONObject(0);
                d.icon        = w.getString("icon");
                d.description = w.getString("description");

                list.add(d);
            }
        }

        return list;
    }



    // -----------------------------
    // 2) 오늘 포함 7일 (daily[0]~[6])
    // -----------------------------
    public static List<WeatherData> load7Days() throws Exception {
        JSONObject root = requestRoot();
        JSONArray dailyArr = root.getJSONArray("daily");

        List<WeatherData> list = new ArrayList<WeatherData>();

        int n = Math.min(7, dailyArr.length());
        for (int i = 0; i < n; i++) {
            JSONObject day = dailyArr.getJSONObject(i);

            WeatherData d = new WeatherData();
            d.isHourly = false;

            d.dt = day.getLong("dt");

            JSONObject temp = day.getJSONObject("temp");
            d.tempDay = temp.getDouble("day");
            d.tempMin = temp.getDouble("min");
            d.tempMax = temp.getDouble("max");

            d.pop = day.optDouble("pop", 0.0);

            JSONObject w = day.getJSONArray("weather").getJSONObject(0);
            d.icon        = w.getString("icon");
            d.description = w.getString("description");

            list.add(d);
        }

        return list;
    }

    // -----------------------------
    // 아이콘 캐시 + 로딩
    // -----------------------------
    public static ImageIcon getIcon(String iconId) {
        if (iconId == null || iconId.isEmpty()) return null;

        ImageIcon cached = ICON_CACHE.get(iconId);
        if (cached != null) return cached;

        try {
            URL url = new URL("https://openweathermap.org/img/wn/" + iconId + "@2x.png");
            ImageIcon icon = new ImageIcon(url);
            ICON_CACHE.put(iconId, icon);
            return icon;
        } catch (Exception e) {
            return null;
        }
    }
}
