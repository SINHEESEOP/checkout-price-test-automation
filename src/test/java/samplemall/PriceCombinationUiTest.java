package samplemall;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import samplemall.config.TestConfig;
import samplemall.page.CouponOption;
import samplemall.policy.Coupon;
import samplemall.policy.ExpectedPrice;
import samplemall.policy.PriceCalculator;
import samplemall.policy.PriceSummary;
import samplemall.support.CombinationCase;
import samplemall.support.CombinationCsv;
import samplemall.support.CombinationReport;
import samplemall.support.UiTestBase;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 이 도구의 중심 테스트: 결제 금액 조합 300개를 화면에서 확인한다.
 *
 * <p>조합 하나는 "어떤 등급이, 어떤 장바구니에, 어떤 쿠폰을 쓰는가"다
 * (등급 2 × 장바구니 30 × 쿠폰 5 = 300). 조합마다 하는 일은 손으로 할 때와 같다.
 *
 * <pre>
 * 손으로 할 때 (TC CMB-01~45)              이 테스트
 * ---------------------------------------  -----------------------------------
 * 셋팅 명령을 터미널에 붙여 넣는다     →   1. 준비: 테스트 API로 초기화, 장바구니 구성
 * 기대 결과 칸을 읽는다                →   2. 기대값: 정책 계산기로 계산
 * 새로고침하고 쿠폰을 고른다           →   3. 실행: 주문서를 열고 쿠폰 선택
 * 화면 숫자를 기대 결과와 비교한다     →   4. 판정: 기대값과 화면 값 비교
 * 결과 칸에 Pass/Fail을 적는다         →   5. 기록: 결과표에 한 줄 추가
 * </pre>
 *
 * <p>{@code @ParameterizedTest}는 테스트 메서드 하나를 데이터만 바꿔 가며 여러 번 실행하는 기능이다.
 * 아래 메서드는 하나지만 데이터 파일의 줄 수만큼(300번) 실행된다.
 *
 * <p>실행: {@code mvn test -Dtest=PriceCombinationUiTest} (전체 300개),
 * {@code -Dscope=manual77}을 붙이면 손으로 수행하도록 고른 77개만 실행한다.
 */
@Tag("ui")
@DisplayName("금액 조합: 등급 × 장바구니 × 쿠폰")
class PriceCombinationUiTest extends UiTestBase {

    private static final PriceCalculator calculator = new PriceCalculator();
    private static final CombinationReport report = new CombinationReport();

    /** 테스트에 넣을 데이터. {@code @MethodSource("cases")}가 이 메서드를 부른다. */
    static List<CombinationCase> cases() {
        return CombinationCsv.load(TestConfig.scope());
    }

    @BeforeAll
    static void startReport() {
        report.start();
    }

    @AfterAll
    static void writeReport() {
        report.writeTo(Path.of("target", "reports"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void screenShowsPriceFromPolicy(CombinationCase c) {
        // 1. 준비 — TC의 셋팅 명령(mall reset && mall cart ...)과 같은 일을 API로 한다
        api.resetToBaseline();
        api.setCart(c.userId(), c.cart());

        // 2. 기대값 — 정책서대로라면 화면이 어떻게 나와야 하는가
        ExpectedPrice expected = calculator.calculate(c.grade(), c.cart(), c.coupon(), 0);

        // 3. 실행 — 주문서를 열고 쿠폰을 고른 뒤 화면 값을 읽는다
        checkout.openAs(c.userId());
        assertEquals(c.cart().subtotal(), checkout.readSubtotal(), "준비한 장바구니가 화면에 반영되지 않았어요");
        CouponOption option = pickCoupon(c.coupon());
        PriceSummary actual = checkout.readSummary();

        // 4. 판정 — 세 가지를 모두 비교하고, 다른 것을 한꺼번에 보고한다
        boolean passed = false;
        try {
            assertAll(
                    () -> assertEquals(expected.couponUsable(), option.selectable(), "쿠폰을 고를 수 있는지"),
                    () -> assertBlockMessage(expected, option),
                    () -> assertEquals(expected.summary(), actual, "결제 금액"));
            passed = true;
        } finally {
            // 5. 기록 — 통과든 실패든 결과표에 남긴다. 실패면 화면도 그림으로 남긴다
            report.add(c, expected, option, actual, passed);
            if (!passed) {
                checkout.saveScreenshot(Path.of("target", "screenshots", c.id() + ".png"));
            }
        }
    }

    /**
     * 쿠폰을 고른다. 회색이라 고를 수 없으면 고르지 않고, 화면에 보인 상태만 돌려준다.
     * 쿠폰 없이 보는 조합은 두 칸 모두 '선택 안 함' 그대로 둔다.
     */
    private CouponOption pickCoupon(Coupon coupon) {
        if (coupon == Coupon.NONE) {
            return new CouponOption(true, "");
        }
        CouponOption option = checkout.findCoupon(coupon);
        if (option.selectable()) {
            checkout.selectCoupon(coupon);
        }
        return option;
    }

    /** 쿠폰을 고를 수 없어야 하는 조합이면 사유 문구도 확인한다. 정책서로 문구를 정할 수 있을 때만. */
    private void assertBlockMessage(ExpectedPrice expected, CouponOption option) {
        if (expected.hasCheckableMessage()) {
            assertEquals(expected.blockMessage(), option.reason(), "쿠폰을 고를 수 없는 사유 문구");
        }
    }
}
