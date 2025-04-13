package by.onlinebanking.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("(execution(* by.onlinebanking.controller..*(..)) || " +
            "execution(* by.onlinebanking.service..*(..)) ||" +
            " execution(* by.onlinebanking.security.service..*(..))) && " +
            "!within(by.onlinebanking.logs.service.LogsService)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        LOGGER.info("Method invocation: {} | Arguments: {}", methodName, joinPoint.getArgs());

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsedTime = System.currentTimeMillis() - startTime;

        LOGGER.info("Method {} completed in {} ms | Result: {}", methodName, elapsedTime, result);
        return result;
    }
}