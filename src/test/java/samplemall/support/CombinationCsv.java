package samplemall.support;

import samplemall.policy.Cart;
import samplemall.policy.Coupon;
import samplemall.policy.Grade;
import samplemall.policy.PriceSummary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 조합 데이터 파일({@code src/test/resources/price-combinations.csv})을 읽는 클래스.
 *
 * <p>테스트 코드와 테스트 데이터를 나눠 두면, 조합을 늘릴 때 코드를 고치지 않고 파일에 줄만 더하면 된다.
 * 이런 방식을 데이터 주도 테스트라고 부른다.
 *
 * <p>파일의 열:
 * <pre>
 * id, grade, user, cart, coupon, tcBlockCodes, tcDiscount, tcShipping, tcFinal, manualTc
 * M-067, 일반, u1, 셔츠 의류 31000 1, WELCOME3000, , 3000, 3000, 31000, CMB-08 #1
 * </pre>
 */
public final class CombinationCsv {

    private static final String FILE = "/price-combinations.csv";

    private CombinationCsv() {
    }

    /**
     * 실행 범위에 맞는 조합을 읽는다.
     *
     * @param scope "all"이면 300개 전체, "manual77"이면 손으로 수행하도록 고른 77개
     */
    public static List<CombinationCase> load(String scope) {
        List<CombinationCase> all = loadAll();
        if (scope.equals("all")) {
            return all;
        }
        if (scope.equals("manual77")) {
            List<CombinationCase> selected = new ArrayList<>();
            for (CombinationCase c : all) {
                if (c.isInManual77()) {
                    selected.add(c);
                }
            }
            return selected;
        }
        throw new IllegalArgumentException("scope는 all 또는 manual77이에요: " + scope);
    }

    /** 파일의 모든 줄을 읽는다. 첫 줄은 열 이름이라 건너뛴다. */
    public static List<CombinationCase> loadAll() {
        InputStream stream = CombinationCsv.class.getResourceAsStream(FILE);
        if (stream == null) {
            throw new IllegalStateException("데이터 파일을 찾지 못했어요: " + FILE);
        }

        List<CombinationCase> cases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            reader.readLine(); // 열 이름 줄
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    cases.add(parseLine(line));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("데이터 파일을 읽지 못했어요: " + FILE, e);
        }
        return cases;
    }

    private static CombinationCase parseLine(String line) {
        // -1을 주면 맨 끝의 빈 칸도 버리지 않는다 (manualTc가 비어 있는 줄이 많다).
        String[] cell = line.split(",", -1);

        String id = cell[0];
        Grade grade = Grade.fromLabel(cell[1]);
        String userId = cell[2];
        Cart cart = Cart.parse(cell[3]);
        Coupon coupon = Coupon.valueOf(cell[4]);
        List<String> tcBlockCodes = cell[5].isEmpty() ? List.of() : List.of(cell[5].split("\\+"));
        PriceSummary tcExpected = new PriceSummary(
                Integer.parseInt(cell[6]),
                Integer.parseInt(cell[7]),
                Integer.parseInt(cell[8]));
        String manualTc = cell[9];

        return new CombinationCase(id, grade, userId, cart, coupon, tcBlockCodes, tcExpected, manualTc);
    }
}
