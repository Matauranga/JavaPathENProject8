package com.openclassrooms.tourguide.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Log4j2
public class LoggingFilter extends OncePerRequestFilter {

    /**
     * Method using a custom filter to capture the payload of the server request and response //TODO franck "payload"
     *
     * @param request     information in the request send
     * @param response    information returned by the server
     * @param filterChain the filter
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(requestWrapper, responseWrapper);

        log.info("STARTING PROCESSING : METHOD={}; REQUEST_URI={};", request.getMethod(), request.getRequestURI());
        log.info("FINISHED PROCESSING : RESPONSE CODE={}; ", response.getStatus());

        responseWrapper.copyBodyToResponse();
    }

}