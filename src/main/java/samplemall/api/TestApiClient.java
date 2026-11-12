package samplemall.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import samplemall.config.TestConfig;
import samplemall.policy.Cart;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * 샘플몰의 테스트 전용 API({@code /test/...})를 부르는 클래스.
 *
 * <p>테스트하려면 "29,999원짜리 장바구니" 같은 상태가 먼저 있어야 한다.
 * 샘플몰에는 장바구니를 바꾸는 화면이 없어서 이 API로 만든다.
 * 손으로 테스트할 때 터미널에 넣는 {@code mall} 명령과 같은 일을 한다.
 *
 * <p>화면으로 확인할 것과 API로 준비할 것을 나누는 게 이 도구의 기본 생각이다.
 * 준비는 API로 빠르고 정확하게, 확인은 사용자가 보는 화면에서 한다.
 */
public class TestApiClient {

    private final String baseUrl;
    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public TestApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 모든 데이터를 처음 상태로 되돌리고 서버 시각을 TC 기준 시각으로 고정한다.
     * TC 셋팅 명령의 {@code mall reset}과 같다.
     *
     * <p>테스트마다 맨 먼저 부른다. 앞 테스트가 남긴 상태가 다음 테스트 결과를 바꾸지 않게 하기 위해서다.
     */
    public void resetToBaseline() {
        post("/test/reset", Map.of());
        post("/test/time", Map.of("now", TestConfig.BASELINE_SERVER_TIME));
    }

    /** 사용자의 장바구니를 바꾼다. TC 셋팅 명령의 {@code mall cart u1 셔츠 의류 29999 1}과 같다. */
    public void setCart(String userId, Cart cart) {
        post("/test/cart", Map.of("user", userId, "items", cart.items()));
    }

    /** 사용자의 보유 포인트를 바꾼다. TC 셋팅 명령의 {@code mall points u1 999}와 같다. */
    public void setPoints(String userId, int points) {
        post("/test/points", Map.of("user", userId, "points", points));
    }

    /**
     * JSON을 담아 POST 요청을 보낸다. 실패하면 이유를 담은 예외를 던져서 테스트가 바로 멈추게 한다.
     *
     * <p>준비가 실패한 채로 화면을 확인하면 결함이 아닌데도 실패로 보인다. 그래서 여기서 막는다.
     */
    private void post(String path, Object body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException("샘플몰 서버에 연결하지 못했어요. 서버가 켜져 있는지 확인하세요: " + baseUrl, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("요청을 기다리다가 중단됐어요: " + path, e);
        }

        if (response.statusCode() != 200 || saysNotOk(response.body())) {
            throw new IllegalStateException("테스트 준비에 실패했어요: POST " + path + " → " + response.statusCode() + " " + response.body());
        }
    }

    /** 응답 본문에 {@code "ok": false}가 들어 있는지. */
    private boolean saysNotOk(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        return json.has("ok") && !json.get("ok").getAsBoolean();
    }
}
