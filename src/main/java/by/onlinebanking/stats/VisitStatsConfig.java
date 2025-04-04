package by.onlinebanking.stats;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class VisitStatsConfig {
    @Bean
    public FilterRegistrationBean<VisitCountingFilter> filterRegistrationBean(VisitCountingFilter filter) {
        FilterRegistrationBean<VisitCountingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
