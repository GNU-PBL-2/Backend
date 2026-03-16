package gnu.project.pbl2.common.logging;

import gnu.project.pbl2.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TransactionalLoggingAspect {

    private static final String LOG_FORMAT = "[{}] - [END TX] ({}ms)";

    @Around("@within(org.springframework.stereotype.Service) && execution(* gnu.project.pbl2.*(..))")
    public Object logWritableTransactions(final ProceedingJoinPoint joinPoint) throws Throwable {
        String signature =
            joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature()
                .getName();
        long startTime = System.currentTimeMillis();
        log.info("[{}] - [BEGIN TX]", signature);

        try {
            Object proceed = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info(LOG_FORMAT, signature, elapsedTime);
            return proceed;
        } catch (BusinessException e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info(LOG_FORMAT, signature, elapsedTime);
            throw e;
        } catch (Throwable t) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error(LOG_FORMAT, signature, elapsedTime);
            throw t;
        }
    }
}