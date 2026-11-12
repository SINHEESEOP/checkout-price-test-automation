package samplemall.support;

import samplemall.config.TestConfig;
import samplemall.page.CouponOption;
import samplemall.policy.Coupon;
import samplemall.policy.ExpectedPrice;
import samplemall.policy.PriceSummary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 금액 조합 테스트의 결과표를 만드는 클래스.
 *
 * <p>JUnit은 통과·실패 개수만 알려 준다. 결함을 보고하려면 "어떤 조합에서 기대가 무엇이었고
 * 화면에는 무엇이 나왔는지"가 한눈에 보여야 한다. 그래서 조합마다 기대와 화면 값을 모아 두었다가
 * 테스트가 다 끝나면 파일 두 개로 쓴다.
 *
 * <ul>
 *   <li>{@code price-combinations-범위.md}: 사람이 읽는 결과표 (기대와 다른 조합이 위에 온다)</li>
 *   <li>{@code price-combinations-범위.csv}: 엑셀로 열어 정렬하고 걸러 볼 수 있는 원본</li>
 * </ul>
 */
public final class CombinationReport {

    /** 조합 하나의 결과. */
    private record Row(CombinationCase c, ExpectedPrice expected, CouponOption option, PriceSummary actual, boolean passed) {
    }

    private final List<Row> rows = new ArrayList<>();
    private long startedAtNanos;

    /** 시간을 재기 시작한다. 첫 테스트 전에 한 번 부른다. */
    public void start() {
        startedAtNanos = System.nanoTime();
    }

    /** 조합 하나의 결과를 적어 둔다. */
    public void add(CombinationCase c, ExpectedPrice expected, CouponOption option, PriceSummary actual, boolean passed) {
        rows.add(new Row(c, expected, option, actual, passed));
    }

    /** 모아 둔 결과를 파일로 쓴다. 마지막 테스트 뒤에 한 번 부른다. */
    public void writeTo(Path directory) {
        double seconds = (System.nanoTime() - startedAtNanos) / 1_000_000_000.0;
        String fileName = "price-combinations-" + TestConfig.scope();
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(fileName + ".md"), toMarkdown(seconds), StandardCharsets.UTF_8);
            Files.writeString(directory.resolve(fileName + ".csv"), toCsv(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("결과표를 쓰지 못했어요: " + directory, e);
        }
    }

    // ---------------------------------------------------------------------
    // 사람이 읽는 결과표 (Markdown)
    // ---------------------------------------------------------------------

    private String toMarkdown(double seconds) {
        List<Row> failed = new ArrayList<>();
        for (Row row : rows) {
            if (!row.passed()) {
                failed.add(row);
            }
        }

        StringBuilder md = new StringBuilder();
        md.append("# 금액 조합 테스트 결과\n\n");
        md.append("- 실행: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append('\n');
        md.append("- 대상: ").append(TestConfig.baseUrl()).append('\n');
        md.append("- 범위: ").append(TestConfig.scope()).append(" (조합 ").append(rows.size()).append("개)\n");
        md.append("- 결과: 정책서와 같음 ").append(rows.size() - failed.size())
                .append("개 · 다름 ").append(failed.size()).append("개\n");
        md.append(String.format("- 걸린 시간: %.1f초 (준비 → 화면 조작 → 비교 → 기록까지)%n%n", seconds));

        md.append("## 정책서와 다른 조합 (").append(failed.size()).append("개)\n\n");
        if (failed.isEmpty()) {
            md.append("없음\n\n");
        } else {
            md.append("| 번호 | 손 TC | 등급 | 장바구니 | 쿠폰 | 기대 (정책서) | 화면 |\n");
            md.append("|---|---|---|---|---|---|---|\n");
            for (Row row : failed) {
                CombinationCase c = row.c();
                md.append("| ").append(c.id())
                        .append(" | ").append(c.manualTc().isEmpty() ? "-" : c.manualTc())
                        .append(" | ").append(c.grade().label())
                        .append(" | ").append(c.cart())
                        .append(" | ").append(c.coupon().label())
                        .append(" | ").append(describeExpected(row))
                        .append(" | ").append(describeActual(row))
                        .append(" |\n");
            }
            md.append('\n');
        }

        md.append("## 전체 결과 (").append(rows.size()).append("개)\n\n");
        md.append("| 번호 | 등급 | 장바구니 | 쿠폰 | 화면 | 결과 |\n");
        md.append("|---|---|---|---|---|---|\n");
        for (Row row : rows) {
            CombinationCase c = row.c();
            md.append("| ").append(c.id())
                    .append(" | ").append(c.grade().label())
                    .append(" | ").append(c.cart())
                    .append(" | ").append(c.coupon().label())
                    .append(" | ").append(describeActual(row))
                    .append(" | ").append(row.passed() ? "같음" : "**다름**")
                    .append(" |\n");
        }
        return md.toString();
    }

    /** 기대 결과를 한 줄로. 예: "고를 수 없음(E01) · 쿠폰 할인 0원 · 배송비 3,000원 · 최종 결제 금액 12,999원" */
    private String describeExpected(Row row) {
        if (row.c().coupon() == Coupon.NONE) {
            return row.expected().summary().toString();
        }
        String usable = row.expected().couponUsable()
                ? "고를 수 있음"
                : "고를 수 없음(" + String.join("+", row.expected().blockCodes()) + ")";
        return usable + " · " + row.expected().summary();
    }

    /** 화면에서 본 결과를 한 줄로. */
    private String describeActual(Row row) {
        if (row.c().coupon() == Coupon.NONE) {
            return row.actual().toString();
        }
        String usable = row.option().selectable()
                ? "고를 수 있음"
                : "고를 수 없음(" + row.option().reason() + ")";
        return usable + " · " + row.actual();
    }

    // ---------------------------------------------------------------------
    // 엑셀로 여는 원본 (CSV)
    // ---------------------------------------------------------------------

    private String toCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("id,manualTc,grade,cart,coupon,")
                .append("expectedUsable,expectedDiscount,expectedShipping,expectedFinal,")
                .append("screenUsable,screenReason,screenDiscount,screenShipping,screenFinal,result\n");
        for (Row row : rows) {
            CombinationCase c = row.c();
            PriceSummary expected = row.expected().summary();
            PriceSummary actual = row.actual();
            csv.append(String.join(",",
                    c.id(),
                    c.manualTc(),
                    c.grade().label(),
                    c.cart().toString().replace(",", ""),   // 금액의 쉼표가 칸을 나누지 않게 뺀다
                    c.coupon().name(),
                    row.expected().couponUsable() ? "Y" : "N",
                    String.valueOf(expected.couponDiscount()),
                    String.valueOf(expected.shippingFee()),
                    String.valueOf(expected.finalAmount()),
                    row.option().selectable() ? "Y" : "N",
                    row.option().reason().replace(",", ""),
                    String.valueOf(actual.couponDiscount()),
                    String.valueOf(actual.shippingFee()),
                    String.valueOf(actual.finalAmount()),
                    row.passed() ? "PASS" : "FAIL"));
            csv.append('\n');
        }
        return csv.toString();
    }
}
