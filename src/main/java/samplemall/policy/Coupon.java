package samplemall.policy;

/**
 * 테스트에 쓰는 쿠폰. 정책서 '테스트용 쿠폰' 표를 그대로 옮긴 것이다.
 *
 * <p>쿠폰의 조건(최소 주문금액, 카테고리, 할인 방식)을 숫자로 적어 두었기 때문에,
 * 정책이 바뀌면 이 파일의 숫자만 고치면 기대값 계산이 따라 바뀐다.
 *
 * <p>사용 기간은 담지 않았다. 테스트를 시작할 때 서버 시각을 2026-11-15로 고정하는데,
 * 그 날짜에는 아래 네 쿠폰이 모두 사용 기간 안이기 때문이다.
 */
public enum Coupon {

    //          화면에 보이는 이름      할인 방식            정액    정률  최대 할인  최소 주문금액  카테고리
    NONE(       "선택 안 함",          Kind.NONE,              0,     0,       0,          0,   null),
    WELCOME3000("웰컴 3,000원 할인",   Kind.FIXED,         3_000,     0,       0,     20_000,   null),
    SORRY2000(  "2,000원 할인",        Kind.FIXED,         2_000,     0,       0,     10_000,   null),
    FASHION10(  "의류 10% 할인",       Kind.RATE,              0,    10,   5_000,     30_000,   "의류"),
    FREESHIP(   "무료배송",            Kind.FREE_SHIPPING,     0,     0,       0,          0,   null);

    /** 쿠폰의 할인 방식. */
    public enum Kind {
        /** 쿠폰을 쓰지 않음 */
        NONE,
        /** 정액: 정해진 금액을 깎는다 */
        FIXED,
        /** 정률: 대상 금액의 n%를 깎는다 */
        RATE,
        /** 무료배송: 배송비를 0원으로 만든다 */
        FREE_SHIPPING
    }

    private final String label;
    private final Kind kind;
    private final int fixedAmount;
    private final int ratePercent;
    private final int maxDiscount;
    private final int minOrder;
    private final String category;

    Coupon(String label, Kind kind, int fixedAmount, int ratePercent, int maxDiscount, int minOrder, String category) {
        this.label = label;
        this.kind = kind;
        this.fixedAmount = fixedAmount;
        this.ratePercent = ratePercent;
        this.maxDiscount = maxDiscount;
        this.minOrder = minOrder;
        this.category = category;
    }

    /** 화면의 쿠폰 목록에 보이는 이름. */
    public String label() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    /** 정액 쿠폰이 깎는 금액(원). */
    public int fixedAmount() {
        return fixedAmount;
    }

    /** 정률 쿠폰의 할인율(%). */
    public int ratePercent() {
        return ratePercent;
    }

    /** 정률 쿠폰이 깎을 수 있는 최대 금액(원). */
    public int maxDiscount() {
        return maxDiscount;
    }

    /** 최소 주문금액(원). 쿠폰을 쓰기 전 상품금액 합계가 이 금액 이상이어야 쓸 수 있다. */
    public int minOrder() {
        return minOrder;
    }

    /** 이 쿠폰이 적용되는 카테고리. 모든 상품에 적용되면 {@code null}. */
    public String category() {
        return category;
    }

    /** 카테고리 한정 쿠폰인지. */
    public boolean isCategoryLimited() {
        return category != null;
    }

    /** 무료배송 쿠폰인지. 화면에서 무료배송 쿠폰은 '배송비 쿠폰' 칸에서 고른다. */
    public boolean isShippingCoupon() {
        return kind == Kind.FREE_SHIPPING;
    }
}
