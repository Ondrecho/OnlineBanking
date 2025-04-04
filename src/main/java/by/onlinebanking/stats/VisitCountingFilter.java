package by.onlinebanking.stats;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class VisitCountingFilter implements Filter {
    private final VisitCounterService visitCounterService;

    public VisitCountingFilter(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        visitCounterService.recordVisit(uri);
        chain.doFilter(request, response);
    }
}
