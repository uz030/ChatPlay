package weather;

public class WeatherData {

    // 공통
    public long dt;            // Unix time (seconds)
    public String icon;        // 아이콘 코드
    public String description; // 날씨 설명
    public double pop;         // 강수 확률 (0~1)

    // 일 단위 데이터 (7일 예보용)
    public double tempDay;
    public double tempMin;
    public double tempMax;

    // 시간 단위 데이터 (24시간용)
    public double temp;

    // 이 데이터가 시간 단위인지(24시간) / 일 단위인지(7일)
    public boolean isHourly = false;
}
