package samplemall.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * 정책서의 결제 금액 계산 규칙을 코드로 옮긴 "기대값 계산기".
 *
 * <p>테스트는 화면에 나온 금액이 맞는지 판정해야 한다. 그러려면 "맞는 금액"을 누가 알려 줘야 한다.
 * 이 클래스가 그 역할을 한다. 테스트 용어로는 테스트 오라클이라고 부른다.
 *
 * <p>지킨 원칙 두 가지:
 * <ul>
 *   <li>정책서만 보고 썼다. 테스트 대상(샘플몰)의 코드는 보지 않았다.
 *       대상의 코드를 베끼면 대상이 틀렸을 때 계산기도 똑같이 틀려서 결함을 못 잡는다.</li>
 *   <li>계산기가 틀리면 테스트 전체가 틀린다. 그래서 계산기부터 검증한다.
 *       {@code PriceCalculatorTest}가 정책서 예시 6개, TC 문서의 기대값 300개와 맞춰 본다.</li>
 * </ul>
 *
 * <p>계산 순서(정책서 '결제 금액 계산 규칙'): 상품금액 → 쿠폰 → 배송비 → 포인트
 */
public final class PriceCalculator {

    /** 무료배송 조건에 못 미칠 때 붙는 배송비. */
    public static final int SHIPPING_FEE = 3_000;

    /**
     * 등급, 장바구니, 쿠폰, 사용 포인트가 주어졌을 때 화면에 나와야 하는 결과를 계산한다.
     *
     * @param points 사용할 포인트. 쓰지 않으면 0
     */
    public ExpectedPrice calculate(Grade grade, Cart cart, Coupon coupon, int points) {
        // 먼저 이 쿠폰을 쓸 수 있는 주문인지 본다.
        // 쓸 수 없으면 화면에서 고를 수 없으므로, 금액은 쿠폰이 없는 주문과 같아야 한다.
        List<String> blockCodes = findBlockCodes(cart, coupon);
        Coupon applied = blockCodes.isEmpty() ? coupon : Coupon.NONE;

        int subtotal = cart.subtotal();                              // 규칙 1. 상품금액
        int discount = discountOf(applied, cart);                    // 규칙 2. 쿠폰 할인
        int discounted = subtotal - discount;                        // 규칙 3. 할인 후 상품금액
        int shippingFee = shippingFeeOf(grade, applied, discounted); // 규칙 4. 배송비
        int finalAmount = discounted + shippingFee - points;         // 규칙 5. 최종 결제 금액

        PriceSummary summary = new PriceSummary(discount, shippingFee, finalAmount);
        return new ExpectedPrice(blockCodes, blockMessageOf(coupon, blockCodes), summary);
    }

    /**
     * 쿠폰을 쓸 수 없는 사유를 찾는다 (정책서 '쿠폰 정책').
     *
     * <ul>
     *   <li>E01: 쿠폰을 쓰기 전 상품금액 합계가 최소 주문금액보다 적다</li>
     *   <li>E03: 카테고리 한정 쿠폰인데 그 카테고리 상품이 없다</li>
     * </ul>
     */
    private List<String> findBlockCodes(Cart cart, Coupon coupon) {
        List<String> codes = new ArrayList<>();
        if (cart.subtotal() < coupon.minOrder()) {
            codes.add("E01");
        }
        if (coupon.isCategoryLimited() && cart.subtotalOf(coupon.category()) == 0) {
            codes.add("E03");
        }
        return codes;
    }

    /**
     * 쓸 수 없는 쿠폰 옆에 보여야 하는 문구 (정책서 '오류 메시지').
     *
     * <p>사유가 둘 이상일 때 어느 문구를 보여 줄지는 정책서에 없다(TC '정책 문의' PR-06).
     * 정답이 없는 값은 판정하지 않는다. 그래서 그때는 빈 글자를 돌려준다.
     */
    private String blockMessageOf(Coupon coupon, List<String> blockCodes) {
        if (blockCodes.size() != 1) {
            return "";
        }
        return switch (blockCodes.get(0)) {
            case "E01" -> String.format("%,d원 이상 구매 시 사용할 수 있어요", coupon.minOrder());
            case "E03" -> "이 주문에 적용할 수 있는 상품이 없어요";
            default -> "";
        };
    }

    /** 규칙 2. 쿠폰 할인 금액. */
    private int discountOf(Coupon coupon, Cart cart) {
        // 할인 대상 금액: 카테고리 한정 쿠폰은 그 카테고리 상품금액만, 아니면 상품금액 전체
        int target = coupon.isCategoryLimited() ? cart.subtotalOf(coupon.category()) : cart.subtotal();

        return switch (coupon.kind()) {
            case NONE, FREE_SHIPPING -> 0;
            // 정액: 정해진 금액. 단, 할인액은 할인 대상 금액을 넘지 않는다
            case FIXED -> Math.min(coupon.fixedAmount(), target);
            // 정률: 대상 금액의 n%. 정수끼리 나누면 소수점 아래가 버려져서 '원 단위 내림'이 된다.
            //       그다음 최대 할인액을 넘지 않게 자른다
            case RATE -> Math.min(target * coupon.ratePercent() / 100, coupon.maxDiscount());
        };
    }

    /**
     * 규칙 4. 배송비.
     *
     * <p>기준은 쿠폰을 쓰기 전 금액이 아니라 <b>할인 후</b> 상품금액이다.
     * 그래서 쿠폰을 쓰면 무료배송 기준 아래로 내려가 배송비가 붙을 수 있다(정책서 예시 B).
     */
    private int shippingFeeOf(Grade grade, Coupon coupon, int discounted) {
        if (coupon.isShippingCoupon()) {
            return 0;
        }
        return discounted >= grade.freeShippingThreshold() ? 0 : SHIPPING_FEE;
    }
}
