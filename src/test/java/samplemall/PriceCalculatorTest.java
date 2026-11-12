package samplemall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import samplemall.policy.Cart;
import samplemall.policy.CartItem;
import samplemall.policy.Coupon;
import samplemall.policy.ExpectedPrice;
import samplemall.policy.Grade;
import samplemall.policy.PriceCalculator;
import samplemall.policy.PriceSummary;
import samplemall.support.CombinationCase;
import samplemall.support.CombinationCsv;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 기대값 계산기({@link PriceCalculator})가 맞는지 확인하는 테스트.
 *
 * <p>화면 테스트는 "계산기가 낸 값 = 정답"이라고 믿고 화면을 판정한다.
 * 계산기가 틀리면 멀쩡한 화면을 결함이라고 하거나, 결함을 통과시킨다.
 * 그래서 화면을 보기 전에 계산기부터 두 가지 정답과 맞춰 본다.
 *
 * <ol>
 *   <li>정책서에 적힌 예시 6개 (A~F)</li>
 *   <li>TC 문서의 조합표 300줄. 엑셀 수식으로 따로 계산해 둔 기대값이다</li>
 * </ol>
 *
 * <p>브라우저도 서버도 필요 없다. 계산만 하므로 1초 안에 끝난다.
 * 실행: {@code mvn test -Dtest=PriceCalculatorTest}
 */
@DisplayName("기대값 계산기 검증")
class PriceCalculatorTest {

    private final PriceCalculator calculator = new PriceCalculator();

    /**
     * 정책서 '결제 금액 계산 규칙과 예시' 표를 그대로 옮긴 것.
     * 일반 등급 기준이고 상품은 모두 의류다.
     */
    @ParameterizedTest(name = "정책서 예시 {0}")
    @CsvSource(textBlock = """
            # 예시, 상품금액, 쿠폰,        포인트, 할인, 배송비, 최종 결제
            A,      32000,   NONE,            0,     0,     0,  32000
            B,      32000,   FASHION10,       0,  3200,  3000,  31800
            C,      33333,   FASHION10,       0,  3333,     0,  30000
            D,      80000,   FASHION10,       0,  5000,     0,  75000
            E,      25000,   WELCOME3000,  5000,  3000,  3000,  20000
            F,      10000,   NONE,        13000,     0,  3000,      0
            """)
    void matchesPolicyExampleTable(String example, int subtotal, Coupon coupon, int points,
                                   int discount, int shippingFee, int finalAmount) {
        Cart cart = new Cart(List.of(new CartItem("셔츠", "의류", subtotal, 1)));

        ExpectedPrice result = calculator.calculate(Grade.NORMAL, cart, coupon, points);

        assertEquals(new PriceSummary(discount, shippingFee, finalAmount), result.summary());
    }

    /** TC 문서의 조합표와 300줄 모두 같은 값을 내는지 확인한다. */
    @ParameterizedTest(name = "{0}")
    @MethodSource("allCombinations")
    void matchesTcDocument(CombinationCase c) {
        ExpectedPrice result = calculator.calculate(c.grade(), c.cart(), c.coupon(), 0);

        assertAll(
                () -> assertEquals(c.tcBlockCodes(), result.blockCodes(), "쿠폰을 쓸 수 없는 사유"),
                () -> assertEquals(c.tcExpected(), result.summary(), "결제 금액"));
    }

    static List<CombinationCase> allCombinations() {
        return CombinationCsv.loadAll();
    }
}
