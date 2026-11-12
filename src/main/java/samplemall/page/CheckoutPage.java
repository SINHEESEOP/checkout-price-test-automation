package samplemall.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.SelectOption;
import samplemall.policy.Coupon;
import samplemall.policy.PriceSummary;

import java.nio.file.Path;

/**
 * 주문서 화면을 다루는 클래스 (Page Object).
 *
 * <p>Page Object는 "화면 하나를 클래스 하나로 감싸는" 설계 방식이다.
 * 테스트는 {@code checkout.selectCoupon(...)}처럼 사람이 하는 행동으로만 말하고,
 * 어떤 버튼을 어떻게 찾는지는 이 클래스만 안다.
 * 화면이 바뀌면 이 파일만 고치면 되고, 테스트 300개는 손대지 않는다.
 *
 * <p>이 클래스에 넣지 않은 것: 맞다·틀리다 판정.
 * 여기서는 누르고 읽기만 한다. 판정은 테스트가 한다.
 */
public class CheckoutPage {

    private final Page page;
    private final String baseUrl;

    // 화면 요소들. 화면이 붙여 둔 테스트용 이름표(data-testid)로 찾는다.
    // 문구나 모양(CSS)으로 찾으면 디자인이 조금만 바뀌어도 테스트가 깨진다.
    private final Locator userSelect;
    private final Locator discountCouponSelect;
    private final Locator shippingCouponSelect;
    private final Locator pointsInput;
    private final Locator subtotal;
    private final Locator couponDiscount;
    private final Locator shippingFee;
    private final Locator finalAmount;

    public CheckoutPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.userSelect = page.getByTestId("user-select");
        this.discountCouponSelect = page.getByTestId("coupon-select");
        this.shippingCouponSelect = page.getByTestId("ship-coupon-select");
        this.pointsInput = page.getByTestId("points-input");
        this.subtotal = page.getByTestId("sum-subtotal");
        this.couponDiscount = page.getByTestId("sum-discount");
        this.shippingFee = page.getByTestId("sum-shipping");
        this.finalAmount = page.getByTestId("sum-final");
    }

    // ---------------------------------------------------------------------
    // 행동: 사람이 화면에서 하는 일
    // ---------------------------------------------------------------------

    /**
     * 주문서를 열고, 맨 위 사용자 상자에서 사용자를 고른다.
     *
     * <p>사용자를 바꾼 뒤에는 새로고침한다. 손으로 테스트할 때 "셋팅 명령 → 새로고침 → 확인" 순서로
     * 하는 것과 같은 상태에서 시작하려는 것이다.
     */
    public void openAs(String userId) {
        waitForCheckoutLoaded(() -> page.navigate(baseUrl + "/"));

        boolean alreadySelected = userSelect.inputValue().equals(userId);
        if (!alreadySelected) {
            waitForCheckoutLoaded(() -> userSelect.selectOption(userId));
            waitForCheckoutLoaded(page::reload);
        }
    }

    /**
     * 쿠폰을 고른다. 할인 쿠폰은 '할인 쿠폰' 칸에서, 무료배송 쿠폰은 '배송비 쿠폰' 칸에서 고른다.
     * 고르면 화면이 금액을 다시 계산하므로, 다시 계산된 값이 그려질 때까지 기다린다.
     */
    public void selectCoupon(Coupon coupon) {
        Locator select = selectBoxOf(coupon);
        waitForRecalculated(() -> select.selectOption(new SelectOption().setLabel(coupon.label())));
    }

    /**
     * 사용할 포인트를 입력한다.
     * 화면은 입력 칸에서 빠져나올 때 금액을 다시 계산한다. 그래서 값을 넣고 Tab 키를 누른다.
     */
    public void usePoints(int points) {
        pointsInput.fill(String.valueOf(points));
        waitForRecalculated(() -> pointsInput.press("Tab"));
    }

    // ---------------------------------------------------------------------
    // 읽기: 사람이 화면에서 눈으로 확인하는 값
    // ---------------------------------------------------------------------

    /** 쿠폰 선택 상자에서 이 쿠폰이 어떤 상태로 보이는지 읽는다 (고를 수 있는지, 사유 문구). */
    public CouponOption findCoupon(Coupon coupon) {
        Locator option = selectBoxOf(coupon)
                .locator("option")
                .filter(new Locator.FilterOptions().setHasText(coupon.label()));

        if (option.count() == 0) {
            return new CouponOption(false, "");
        }
        return new CouponOption(option.isEnabled(), option.textContent().trim());
    }

    /** '결제 금액' 상자의 상품금액. 준비한 장바구니가 화면에 반영됐는지 확인할 때 쓴다. */
    public int readSubtotal() {
        return Won.parse(subtotal.innerText());
    }

    /** '결제 금액' 상자에서 비교할 세 값(쿠폰 할인, 배송비, 최종 결제 금액)을 읽는다. */
    public PriceSummary readSummary() {
        return new PriceSummary(
                Won.parse(couponDiscount.innerText()),
                Won.parse(shippingFee.innerText()),
                Won.parse(finalAmount.innerText()));
    }

    /** 지금 화면을 그림 파일로 남긴다. 테스트가 실패했을 때 증거로 쓴다. */
    public void saveScreenshot(Path file) {
        page.screenshot(new Page.ScreenshotOptions().setPath(file).setFullPage(true));
    }

    // ---------------------------------------------------------------------
    // 기다리기: 화면이 다 바뀐 뒤에 읽기 위한 장치
    // ---------------------------------------------------------------------
    //
    // 화면은 서버에 물어본 뒤에 값을 바꾼다. 누르자마자 읽으면 바뀌기 전 값을 읽을 수 있다.
    // "0.5초 쉬기" 같은 고정 대기는 느리고, 컴퓨터가 느린 날에는 그래도 실패한다.
    // 그래서 "화면이 서버 응답을 받았다"는 신호를 직접 기다린다.

    /** 주문서를 여는 동작을 하고, 주문서 데이터({@code /api/checkout}) 응답이 화면에 그려질 때까지 기다린다. */
    private void waitForCheckoutLoaded(Runnable action) {
        Response response = page.waitForResponse(r -> r.url().contains("/api/checkout"), action);
        response.finished();
        waitForRepaint();
    }

    /** 금액이 바뀌는 동작을 하고, 다시 계산한 결과({@code /api/quote}) 응답이 화면에 그려질 때까지 기다린다. */
    private void waitForRecalculated(Runnable action) {
        Response response = page.waitForResponse(r -> r.url().contains("/api/quote"), action);
        response.finished();
        waitForRepaint();
    }

    /** 브라우저가 화면을 두 번 다시 그릴 때까지 기다린다. 응답을 받은 화면이 값을 바꿔 그릴 시간을 준다. */
    private void waitForRepaint() {
        page.evaluate("() => new Promise(done => requestAnimationFrame(() => requestAnimationFrame(done)))");
    }

    private Locator selectBoxOf(Coupon coupon) {
        return coupon.isShippingCoupon() ? shippingCouponSelect : discountCouponSelect;
    }
}
