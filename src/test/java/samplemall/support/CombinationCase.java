package samplemall.support;

import samplemall.policy.Cart;
import samplemall.policy.Coupon;
import samplemall.policy.Grade;
import samplemall.policy.PriceSummary;

import java.util.List;

/**
 * 금액 조합 테스트 케이스 하나 = 데이터 파일({@code price-combinations.csv})의 한 줄.
 *
 * <p>조합 하나는 "어떤 등급이, 어떤 장바구니에, 어떤 쿠폰을 쓰는가"다.
 * 장바구니 30가지 × 쿠폰 5가지(없음 포함) × 등급 2가지 = 300줄이 있다.
 *
 * @param id           조합 번호 (M-001 ~ M-300)
 * @param grade        회원 등급
 * @param userId       그 등급의 테스트 사용자 (일반 = u1, VIP = u2)
 * @param cart         장바구니
 * @param coupon       고를 쿠폰. 쿠폰 없이 보는 조합은 {@link Coupon#NONE}
 * @param tcBlockCodes TC 문서에 적힌 "쿠폰을 쓸 수 없는 사유". 쓸 수 있으면 빈 목록
 * @param tcExpected   TC 문서에 적힌 기대 금액. 계산기가 맞는지 검증할 때 비교 상대로 쓴다
 * @param manualTc     손으로 수행하는 TC에서의 위치 (예: "CMB-07 #2" = CMB-07의 절차 2번).
 *                     손으로 수행하도록 고른 77개에만 값이 있다
 */
public record CombinationCase(
        String id,
        Grade grade,
        String userId,
        Cart cart,
        Coupon coupon,
        List<String> tcBlockCodes,
        PriceSummary tcExpected,
        String manualTc) {

    /** 손으로 수행하도록 고른 77개에 들어 있는 조합인지. */
    public boolean isInManual77() {
        return !manualTc.isEmpty();
    }

    /** 테스트 결과 목록에 보이는 이름. 예: "M-067 | 일반 | 셔츠(의류) 31,000원 × 1 | 웰컴 3,000원 할인" */
    @Override
    public String toString() {
        return id + " | " + grade.label() + " | " + cart + " | " + coupon.label();
    }
}
