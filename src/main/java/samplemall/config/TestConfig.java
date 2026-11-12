package samplemall.config;

/**
 * 실행할 때 바꿀 수 있는 값을 한곳에 모아 둔 클래스.
 *
 * <p>값은 명령줄에서 {@code -D이름=값}으로 바꾼다.
 * 예: {@code mvn test -Dmall.url=http://localhost:9000 -Dheadless=false}
 *
 * <p>주소나 옵션이 코드 여기저기에 흩어져 있으면 환경이 바뀔 때 고칠 곳을 찾기 어렵다.
 * 그래서 "바뀔 수 있는 값"은 모두 이 클래스에서만 읽는다.
 */
public final class TestConfig {

    /**
     * TC가 기준으로 삼는 서버 시각 (2026-11-15 낮 12시, 한국 시간).
     *
     * <p>쿠폰에는 사용 기간이 있어서, 실제 날짜로 테스트하면 날짜가 지난 뒤에는 같은 테스트가
     * 다른 결과를 낸다. 그래서 테스트를 시작할 때마다 서버 시각을 이 값으로 고정한다.
     */
    public static final String BASELINE_SERVER_TIME = "2026-11-15T12:00:00+09:00";

    private TestConfig() {
        // 값만 꺼내 쓰는 클래스라서 객체를 만들지 못하게 막는다.
    }

    /** 샘플몰 서버 주소. 기본값은 {@code http://localhost:8000}. */
    public static String baseUrl() {
        return System.getProperty("mall.url", "http://localhost:8000");
    }

    /** 브라우저 창을 띄우지 않고 실행할지. 눈으로 보고 싶으면 {@code -Dheadless=false}. */
    public static boolean headless() {
        return !"false".equalsIgnoreCase(System.getProperty("headless", "true"));
    }

    /** 동작 사이에 넣는 지연 시간(밀리초). 눈으로 따라가려면 {@code -Dslowmo=300}. */
    public static int slowMoMillis() {
        return Integer.parseInt(System.getProperty("slowmo", "0"));
    }

    /**
     * 금액 조합 테스트의 실행 범위.
     *
     * <ul>
     *   <li>{@code all} (기본값): 조합 300개 전체</li>
     *   <li>{@code manual77}: 손으로 수행하도록 고른 77개 (TC의 CMB-01~45)</li>
     * </ul>
     */
    public static String scope() {
        return System.getProperty("scope", "all");
    }
}
