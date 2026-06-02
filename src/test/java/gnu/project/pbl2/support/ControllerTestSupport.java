package gnu.project.pbl2.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.auth.jwt.JwtInterceptor;
import gnu.project.pbl2.auth.service.OauthService;
import gnu.project.pbl2.common.enumerated.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * {@code @WebMvcTest} 기반 컨트롤러 테스트 공통 베이스 클래스.
 *
 * <p>인증 인프라(JwtInterceptor, LoginArgumentResolver)를 Mock으로 대체하여
 * 컨트롤러 계층의 HTTP 요청/응답 로직만 검증한다.
 *
 * <p>사용 방법:
 * <pre>{@code
 * @WebMvcTest(FridgeController.class)
 * class FridgeControllerTest extends ControllerTestSupport {
 *     @MockBean FridgeService fridgeService;
 *
 *     @Test
 *     void test() {
 *         mockMvc.perform(get("/api/v1/fridge/1"))
 *                .andExpect(status().isOk());
 *     }
 * }
 * }</pre>
 */
@ActiveProfiles("test")
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /** JwtInterceptor 를 Mock으로 교체 — 모든 요청을 인증 없이 통과 */
    @MockBean
    protected JwtInterceptor jwtInterceptor;

    /**
     * LoginArgumentResolver 의존 서비스 Mock.
     * FridgeController 처럼 @Auth 파라미터가 없는 컨트롤러에서도
     * WebConfig 가 LoginArgumentResolver 빈을 생성할 때 필요하다.
     */
    @MockBean
    protected OauthService oauthService;

    /**
     * 각 테스트 전에 JwtInterceptor.preHandle() 이 항상 true(통과)를 반환하도록 설정한다.
     * Mockito 기본값은 false 이므로 명시적으로 지정해야 한다.
     */
    @BeforeEach
    void setUpAuthMock() throws Exception {
        given(jwtInterceptor.preHandle(any(), any(), any())).willReturn(true);
        given(oauthService.getCurrentAccessor(any(), any(), any()))
            .willReturn(Accessor.user("test-social-id", 1L, UserRole.USER));
    }

    /** 인증된 유저 request attribute를 주입하는 헬퍼 */
    protected MockHttpServletRequestBuilder withUser(MockHttpServletRequestBuilder builder) {
        return builder
            .requestAttr("socialId", "test-social-id")
            .requestAttr("userId", 1L)
            .requestAttr("userRole", UserRole.USER);
    }
}
