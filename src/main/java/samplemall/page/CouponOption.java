package samplemall.page;

/**
 * 쿠폰 선택 상자에 보이는 쿠폰 한 줄의 상태.
 *
 * <p>쓸 수 없는 쿠폰은 화면에서 회색으로 나오고 이름 뒤에 사유가 붙는다.
 * 예: "웰컴 3,000원 할인 — 20,000원 이상 구매 시 사용할 수 있어요"
 *
 * @param selectable 고를 수 있는지 (회색이 아닌지)
 * @param text       화면에 적힌 글자 전체. 목록에 쿠폰이 아예 없으면 빈 글자
 */
public record CouponOption(boolean selectable, String text) {

    /** 쿠폰 이름과 사유를 나누는 표시. */
    private static final String SEPARATOR = " — ";

    /** 이름 뒤에 붙은 사유 문구. 사유가 없으면 빈 글자. */
    public String reason() {
        int at = text.indexOf(SEPARATOR);
        return at < 0 ? "" : text.substring(at + SEPARATOR.length()).trim();
    }
}
