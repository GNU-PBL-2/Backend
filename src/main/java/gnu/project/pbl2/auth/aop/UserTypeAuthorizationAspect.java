package gnu.project.pbl2.auth.aop;

import static gnu.project.pbl2.common.error.ErrorCode.AUTH_FORBIDDEN;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.common.exception.AuthException;
import java.util.Arrays;
import java.util.function.Predicate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserTypeAuthorizationAspect {

    @Around("@annotation(gnu.project.pbl2.auth.aop.OnlyUser)")
    public Object authorizeUser(ProceedingJoinPoint joinPoint) throws Throwable {
        return authorize(joinPoint, Accessor::isUser);
    }

    @Around("@annotation(gnu.project.pbl2.auth.aop.OnlyAdmin)")
    public Object authorizeAdmin(ProceedingJoinPoint joinPoint) throws Throwable {
        return authorize(joinPoint, Accessor::isAdmin);
    }

    private Object authorize(
        ProceedingJoinPoint joinPoint,
        Predicate<Accessor> condition
    ) throws Throwable {
        Arrays.stream(joinPoint.getArgs())
            .filter(Accessor.class::isInstance)
            .map(Accessor.class::cast)
            .filter(condition)
            .findFirst()
            .orElseThrow(() -> new AuthException(AUTH_FORBIDDEN));

        return joinPoint.proceed();
    }
}
