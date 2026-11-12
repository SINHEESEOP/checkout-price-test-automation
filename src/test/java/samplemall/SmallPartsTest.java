package samplemall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samplemall.page.CouponOption;
import samplemall.page.Won;
import samplemall.policy.Cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 작은 부품들이 맞게 동작하는지 확인하는 테스트.
 *
 * <p>화면 테스트가 실패했을 때 "화면이 틀렸다"고 말하려면, 화면 글자를 숫자로 바꾸는 부품이나
 * 장바구니 글자를 읽는 부품이 틀리지 않았다는 걸 먼저 알아야 한다.
 * 브라우저도 서버도 필요 없다.
 */
@DisplayName("작은 부품 검증")
class SmallPartsTest {

    @Test
    @DisplayName("화면 금액 글자에서 숫자만 꺼낸다")
    void wonParsesAmountText() {
        assertEquals(32_000, Won.parse("32,000원"));
        assertEquals(2_000, Won.parse("-2,000원"));
        assertEquals(5_000, Won.parse("-5,000P"));
        assertEquals(0, Won.parse("0원"));
    }

    @Test
    @DisplayName("숫자가 없는 글자는 0으로 읽지 않고 오류를 낸다")
    void wonRejectsTextWithoutNumber() {
        assertThrows(IllegalStateException.class, () -> Won.parse("NaN원"));
        assertThrows(IllegalStateException.class, () -> Won.parse(""));
    }

    @Test
    @DisplayName("장바구니 글자를 읽어 상품금액과 카테고리별 금액을 구한다")
    void cartParsesTextAndSumsAmounts() {
        Cart cart = Cart.parse("셔츠 의류 10000 2 머그컵 생활 20000 1");

        assertEquals(2, cart.items().size());
        assertEquals(40_000, cart.subtotal());
        assertEquals(20_000, cart.subtotalOf("의류"));
        assertEquals(20_000, cart.subtotalOf("생활"));
        assertEquals("셔츠(의류) 10,000원 × 2 + 머그컵(생활) 20,000원 × 1", cart.toString());
    }

    @Test
    @DisplayName("네 칸씩 맞지 않는 장바구니 글자는 거부한다")
    void cartRejectsBrokenText() {
        assertThrows(IllegalArgumentException.class, () -> Cart.parse("셔츠 의류 10000"));
    }

    @Test
    @DisplayName("쿠폰 이름 뒤에 붙은 사유 문구를 꺼낸다")
    void couponOptionSplitsReason() {
        CouponOption blocked = new CouponOption(false, "웰컴 3,000원 할인 — 20,000원 이상 구매 시 사용할 수 있어요");
        CouponOption usable = new CouponOption(true, "웰컴 3,000원 할인");

        assertEquals("20,000원 이상 구매 시 사용할 수 있어요", blocked.reason());
        assertEquals("", usable.reason());
    }
}
