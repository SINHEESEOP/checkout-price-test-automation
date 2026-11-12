package samplemall.policy;

/**
 * 주문서 '결제 금액' 상자에서 비교하는 세 값.
 *
 * <p>기대값(정책서로 계산한 값)과 실제값(화면에서 읽은 값)을 같은 모양으로 담는다.
 * 그래서 테스트에서는 두 객체가 같은지만 보면 된다. record는 모든 값이 같으면 같은 객체로 본다.
 *
 * @param couponDiscount 쿠폰 할인 금액(원). 화면에는 "-2,000원"처럼 빼기 표시가 붙지만 여기서는 양수로 담는다
 * @param shippingFee    배송비(원). 0 아니면 3,000
 * @param finalAmount    최종 결제 금액(원)
 */
public record PriceSummary(int couponDiscount, int shippingFee, int finalAmount) {

    /** TC의 기대 결과와 같은 말투로 보여 준다. 테스트가 실패했을 때 이 글자가 그대로 출력된다. */
    @Override
    public String toString() {
        return String.format("쿠폰 할인 %,d원 · 배송비 %,d원 · 최종 결제 금액 %,d원", couponDiscount, shippingFee, finalAmount);
    }
}
