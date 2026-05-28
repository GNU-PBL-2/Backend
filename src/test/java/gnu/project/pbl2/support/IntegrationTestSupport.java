package gnu.project.pbl2.support;

import gnu.project.pbl2.auth.jwt.JwtProvider;
import gnu.project.pbl2.auth.jwt.JwtResolver;
import gnu.project.pbl2.common.enumerated.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code @SpringBootTest} 기반 통합 테스트 공통 베이스 클래스.
 *
 * <p>전체 스프링 컨텍스트를 로드하며 H2 인메모리 DB(test 프로파일)를 사용한다.
 * 각 테스트는 트랜잭션 내에서 실행되어 자동 롤백된다.
 *
 * <p>사용 방법:
 * <pre>{@code
 * class MyServiceTest extends IntegrationTestSupport {
 *     @Autowired MyService myService;
 *
 *     @Test
 *     void test() { ... }
 * }
 * }</pre>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected JwtResolver jwtResolver;

    /**
     * 테스트용 USER 역할 JWT 토큰을 발급한다.
     *
     * @param userId   사용자 ID
     * @param socialId OAuth 소셜 ID
     * @return "Bearer xxx.yyy.zzz" 형식의 Authorization 헤더 값
     */
    protected String bearerToken(Long userId, String socialId) {
        String token = jwtProvider.createAccessToken(userId, socialId, UserRole.USER);
        return "Bearer " + token;
    }
}
