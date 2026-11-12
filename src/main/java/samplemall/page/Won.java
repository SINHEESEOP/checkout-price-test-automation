package samplemall.page;

/**
 * 화면에 적힌 금액 글자를 숫자로 바꾸는 도우미.
 *
 * <pre>
 * "32,000원"   → 32000
 * "-2,000원"   → 2000   (화면은 할인에 빼기 표시를 붙인다. 크기만 꺼낸다)
 * "-5,000P"    → 5000
 * </pre>
 */
public final class Won {

    private Won() {
        // 메서드만 꺼내 쓰는 클래스라서 객체를 만들지 못하게 막는다.
    }

    public static int parse(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            // 숫자가 하나도 없으면 0으로 넘기지 않는다.
            // 화면이 깨져서 빈칸이나 "NaN원"이 나온 걸 "0원"으로 잘못 읽으면 결함을 놓치기 때문이다.
            throw new IllegalStateException("화면에서 금액을 읽을 수 없어요: '" + text + "'");
        }
        return Integer.parseInt(digits);
    }
}
