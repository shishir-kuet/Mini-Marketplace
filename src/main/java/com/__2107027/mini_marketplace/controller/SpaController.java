package com.__2107027.mini_marketplace.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles SPA routing for the React frontend.
 *
 * Strategy: implement ErrorController so that any 404 NOT originating from
 * an /api/** path is forwarded to index.html, letting React Router handle it.
 * This works reliably with Spring Boot 3's PathPatternParser.
 */
@Controller
public class SpaController implements ErrorController {

    /**
     * Called by Spring Boot's error handling pipeline for all errors.
     * We intercept 404s on non-API paths and forward them to the React SPA.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri  = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (statusAttr != null) {
            int status = Integer.parseInt(statusAttr.toString());
            // Only hijack 404s that are not API calls
            if (status == HttpStatus.NOT_FOUND.value()
                    && requestUri != null
                    && !requestUri.startsWith("/api/")) {
                return "forward:/index.html";
            }
        }
        // For all other errors (500, 403, …) let Spring's default error page handle it
        return "forward:/index.html";
    }

    /** Also directly serve index.html for the root path. */
    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }
}

