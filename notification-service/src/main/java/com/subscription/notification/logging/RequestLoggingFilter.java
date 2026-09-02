package com.subscription.notification.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest   = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        String method = httpRequest.getMethod();
        String uri    = httpRequest.getRequestURI();

        log.info(">>> {} {} — started", method, uri);

        chain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        int  status   = httpResponse.getStatus();

        log.info("<<< {} {} — completed [status={}, time={}ms]", method, uri, status, duration);
    }
}
