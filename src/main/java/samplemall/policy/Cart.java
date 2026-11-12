package samplemall.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * 장바구니. 상품 줄({@link CartItem})의 목록이다.
 *
 * @param items 담긴 상품들
 */
public record Cart(List<CartItem> items) {

    /**
     * 글자로 적힌 장바구니를 객체로 바꾼다.
     *
     * <p>형식은 TC의 셋팅 명령({@code mall cart})과 같다. 네 칸(상품명 카테고리 단가 수량)이 상품 하나다.
     *
     * <pre>
     * "셔츠 의류 29999 1"                      → 상품 1개
     * "셔츠 의류 10000 1 머그컵 생활 20000 1"   → 상품 2개
     * </pre>
     */
    public static Cart parse(String text) {
        String[] words = text.trim().split("\\s+");
        if (words.length == 0 || words.length % 4 != 0) {
            throw new IllegalArgumentException("장바구니는 '상품명 카테고리 단가 수량'을 네 칸씩 적어요: " + text);
        }

        List<CartItem> items = new ArrayList<>();
        for (int i = 0; i < words.length; i += 4) {
            String name = words[i];
            String category = words[i + 1];
            int price = Integer.parseInt(words[i + 2]);
            int qty = Integer.parseInt(words[i + 3]);
            items.add(new CartItem(name, category, price, qty));
        }
        return new Cart(items);
    }

    /** 상품금액 = 모든 상품 줄의 금액을 더한 값 (정책서 계산 규칙 1). */
    public int subtotal() {
        int sum = 0;
        for (CartItem item : items) {
            sum += item.amount();
        }
        return sum;
    }

    /** 한 카테고리 상품만 더한 금액. 카테고리 한정 쿠폰의 할인 대상 금액을 구할 때 쓴다. */
    public int subtotalOf(String category) {
        int sum = 0;
        for (CartItem item : items) {
            if (item.category().equals(category)) {
                sum += item.amount();
            }
        }
        return sum;
    }

    /** 사람이 읽기 좋은 모양. 예: "셔츠(의류) 10,000원 × 1 + 머그컵(생활) 20,000원 × 1" */
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        for (CartItem item : items) {
            parts.add(String.format("%s(%s) %,d원 × %d", item.name(), item.category(), item.price(), item.qty()));
        }
        return String.join(" + ", parts);
    }
}
