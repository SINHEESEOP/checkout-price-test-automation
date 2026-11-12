package samplemall.policy;

/**
 * 회원 등급. 정책서 '회원 등급' 표를 그대로 옮긴 것이다.
 *
 * <p>등급은 무료배송 기준과 적립률을 바꾼다. 이 도구는 결제 금액만 보기 때문에
 * 무료배송 기준만 담았다.
 */
public enum Grade {

    NORMAL("일반", 30_000),
    VIP("VIP", 20_000);

    private final String label;
    private final int freeShippingThreshold;

    Grade(String label, int freeShippingThreshold) {
        this.label = label;
        this.freeShippingThreshold = freeShippingThreshold;
    }

    /** 화면과 TC에 적힌 이름 ("일반", "VIP"). */
    public String label() {
        return label;
    }

    /** 할인 후 상품금액이 이 금액 이상이면 배송비가 0원이다. */
    public int freeShippingThreshold() {
        return freeShippingThreshold;
    }

    /** "일반" 또는 "VIP"라는 글자를 등급으로 바꾼다. 데이터 파일을 읽을 때 쓴다. */
    public static Grade fromLabel(String label) {
        for (Grade grade : values()) {
            if (grade.label.equals(label)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("모르는 등급이에요: " + label);
    }
}
