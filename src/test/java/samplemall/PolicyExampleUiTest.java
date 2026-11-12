package samplemall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import samplemall.policy.Cart;
import samplemall.policy.Coupon;
import samplemall.policy.ExpectedPrice;
import samplemall.policy.Grade;
import samplemall.policy.PriceCalculator;
import samplemall.support.UiTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 정책서에 정답이 적혀 있는 예시 6개(A~F)를 화면에서 확인한다.
 *
 * <p>300개를 돌리기 전에 먼저 돌려 보는 짧은 테스트다. 여기서 실패하면 환경(서버, 날짜, 장바구니 준비)이나
 * 기본 계산부터 어긋난 것이라, 300개 결과를 볼 필요가 없다.
 *
 * <p>금액 조합 테스트와 다른 점은 포인트다. 예시 E와 F는 포인트를 쓴다.
 *
 * <p>기대값은 계산기가 계산한다. 계산기가 이 예시 6개의 정답과 같은 값을 내는지는
 * {@code PriceCalculatorTest}가 따로 확인한다.
 */
@Tag("ui")
@DisplayName("정책서 예시 A~F")
class PolicyExampleUiTest extends UiTestBase {

    private static final String USER = "u1"; // 정책서 예시는 모두 일반 등급 기준이다

    private final PriceCalculator calculator = new PriceCalculator();

    @ParameterizedTest(name = "예시 {0}: {1} · {2} · 포인트 {3}P")
    @CsvSource(textBlock = """
            # 예시, 장바구니,             쿠폰,         포인트
            A,      티셔츠 의류 16000 2,  NONE,             0
            B,      티셔츠 의류 16000 2,  FASHION10,        0
            C,      셔츠 의류 33333 1,    FASHION10,        0
            D,      셔츠 의류 80000 1,    FASHION10,        0
            E,      셔츠 의류 25000 1,    WELCOME3000,   5000
            F,      셔츠 의류 10000 1,    NONE,         13000
            """)
    void screenShowsPriceFromPolicyExample(String example, String cartText, Coupon coupon, int points) {
        // 준비
        Cart cart = Cart.parse(cartText);
        api.resetToBaseline();
        api.setCart(USER, cart);
        ExpectedPrice expected = calculator.calculate(Grade.NORMAL, cart, coupon, points);

        // 실행
        checkout.openAs(USER);
        if (coupon != Coupon.NONE) {
            checkout.selectCoupon(coupon);
        }
        if (points > 0) {
            checkout.usePoints(points);
        }

        // 판정
        assertEquals(expected.summary(), checkout.readSummary());
    }
}
