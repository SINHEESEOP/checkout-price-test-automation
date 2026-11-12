package samplemall.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import samplemall.api.TestApiClient;
import samplemall.config.TestConfig;
import samplemall.page.CheckoutPage;

/**
 * 화면 테스트들이 함께 쓰는 준비·정리 코드.
 *
 * <p>브라우저를 켜고 끄는 일은 모든 화면 테스트가 똑같이 한다.
 * 테스트마다 되풀이해 쓰지 않도록 여기에 모아 두고, 화면 테스트 클래스가 이 클래스를 물려받는다.
 *
 * <p>실행 순서:
 * <pre>
 * {@code @BeforeAll}   브라우저를 켠다                     (클래스당 한 번)
 *   {@code @BeforeEach}  새 창을 연다                      (테스트마다)
 *     테스트 한 개
 *   {@code @AfterEach}   창을 닫는다                       (테스트마다)
 * {@code @AfterAll}    브라우저를 끈다                     (클래스당 한 번)
 * </pre>
 *
 * <p>브라우저는 켜는 데 시간이 걸려서 한 번만 켠다. 창은 테스트마다 새로 연다.
 * 새 창은 시크릿 창처럼 아무 기록이 없는 상태라서, 앞 테스트가 브라우저에 남긴 것이
 * 다음 테스트에 영향을 주지 않는다.
 */
public abstract class UiTestBase {

    private static Playwright playwright;
    private static Browser browser;

    /** 테스트 준비용 API. 장바구니를 만들고 서버를 처음 상태로 되돌릴 때 쓴다. */
    protected static TestApiClient api;

    private BrowserContext window;

    /** 주문서 화면. 테스트는 이 객체로 화면을 누르고 읽는다. */
    protected CheckoutPage checkout;

    @BeforeAll
    static void launchBrowser() {
        api = new TestApiClient(TestConfig.baseUrl());
        // 브라우저를 켜기 전에 서버부터 불러 본다. 서버가 꺼져 있으면 여기서 바로 알려 주고 멈춘다.
        api.resetToBaseline();

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.headless())
                .setSlowMo(TestConfig.slowMoMillis()));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openNewWindow() {
        window = browser.newContext();
        // 화면 요소를 기다리는 최대 시간. 기본값(30초)은 너무 길어서, 문제가 있으면 5초 안에 실패하게 줄인다.
        window.setDefaultTimeout(5_000);
        Page page = window.newPage();
        checkout = new CheckoutPage(page, TestConfig.baseUrl());
    }

    @AfterEach
    void closeWindow() {
        window.close();
    }
}
