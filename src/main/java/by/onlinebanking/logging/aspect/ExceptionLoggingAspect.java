package by.onlinebanking.logging.aspect;

import java.util.Arrays;
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
                       "!within(by.onlinebanking.security.filter..*)",
            throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(
                    "Exception in method: {} | Args: {} | Type: {} | Msg: {} | At: {}",
                    methodName,
                    Arrays.toString(args),
                    ex.getClass().getSimpleName(),
                    ex.getMessage(),
                    ex.getStackTrace().length > 0 ? ex.getStackTrace()[0] : "?"
            );
        }

        LOGGER.debug("Full exception stack trace", ex);
    }
}