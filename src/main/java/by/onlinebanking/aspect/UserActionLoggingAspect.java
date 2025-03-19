package by.onlinebanking.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserActionLoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @AfterReturning(
            pointcut = "execution(* by.onlinebanking.controller..*(..))",
            returning = "result"
    )
    public void logUserAction(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        logger.info("User action: {} | Arguments: {} | Result: {}", methodName, args, result);
    }
}