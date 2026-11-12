package samplemall.policy;

import java.util.List;

/**
 * 정책서대로라면 화면이 어떻게 나와야 하는지를 담은 기대 결과.
 *
 * @param blockCodes   쿠폰을 쓸 수 없는 사유 코드. 쓸 수 있으면 빈 목록.
 *                     "E01" = 최소 주문금액 미달, "E03" = 적용할 카테고리 상품 없음
 * @param blockMessage 쓸 수 없을 때 쿠폰 이름 옆에 보여야 하는 사유 문구.
 *                     쓸 수 있거나, 사유가 둘 이상이라 문구를 정할 수 없으면 빈 글자
 * @param summary      결제 금액 상자에 나와야 하는 값. 쿠폰을 쓸 수 없으면 쿠폰이 없는 주문과 같은 금액
 */
public record ExpectedPrice(List<String> blockCodes, String blockMessage, PriceSummary summary) {

    /** 쿠폰을 고를 수 있어야 하는지. */
    public boolean couponUsable() {
        return blockCodes.isEmpty();
    }

    /** 사유 문구까지 확인할 수 있는지. 사유가 정확히 하나일 때만 정책서로 문구를 정할 수 있다. */
    public boolean hasCheckableMessage() {
        return !blockMessage.isEmpty();
    }
}
