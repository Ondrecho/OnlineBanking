package by.onlinebanking.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionLoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    @AfterThrowing(
            pointcut = "execution(* by.onlinebanking..*.*(..)) && " +
                       "!within(by.onlinebanking.filter..*)",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        LOGGER.error(
                "Exception in method: {} | Arguments: {} | Message: {} | Reason: {}",
                methodName, args, ex.getMessage(), ex.getCause() != null ? ex.getCause() : "N/A",
                ex
        );
    }
}