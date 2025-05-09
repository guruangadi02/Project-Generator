package com.npst.spring_boot_project_generator.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MDCLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            MDC.put("method", httpRequest.getMethod());
            MDC.put("path", httpRequest.getRequestURI());

            // Temporarily set responseTime to "-" or 0
            MDC.put("responseTime", "-");

            chain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // Set the actual duration *after* the request completes
            MDC.put("responseTime", String.valueOf(duration));
            log.info("Request Completed.");

            // Clean up to avoid MDC leaking into other threads
            MDC.clear();
        }
    }
}
