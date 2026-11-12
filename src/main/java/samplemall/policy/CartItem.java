package samplemall.policy;

/**
 * 장바구니에 담긴 상품 한 줄.
 *
 * <p>record는 "값만 담는 클래스"를 짧게 쓰는 자바 문법이다.
 * 생성자, 값을 꺼내는 메서드({@code name()}, {@code price()} 등), equals, toString을 자바가 만들어 준다.
 *
 * <p>필드 이름은 샘플몰 테스트 API({@code POST /test/cart})가 받는 JSON 이름과 같게 지었다.
 * 그래서 이 객체를 그대로 JSON으로 바꿔 보낼 수 있다.
 *
 * @param name     상품명. 이름표일 뿐 계산에는 쓰이지 않는다
 * @param category 카테고리 ("의류" 또는 "생활"). 카테고리 한정 쿠폰이 이 값을 본다
 * @param price    단가(원)
 * @param qty      수량
 */
public record CartItem(String name, String category, int price, int qty) {

    /** 이 상품 줄의 금액 = 단가 × 수량. */
    public int amount() {
        return price * qty;
    }
}
