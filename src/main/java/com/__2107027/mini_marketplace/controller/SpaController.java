package com.__2107027.mini_marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards all non-API, non-static routes to the React SPA's index.html.
 * This is required so that React Router routes (e.g. /products/123, /cart)
 * work correctly on a full page refresh or direct URL navigation.
 *
 * Pattern breakdown:
 *   {path:[^\\.]*}   — matches path segments that contain NO dot (i.e. not file extensions like .js/.css)
 *   The double-wildcard variant catches nested routes like /admin/users/5
 */
@Controller
public class SpaController {

    @GetMapping(value = "/{path:[^\\.]*}")
    public String redirectRoot() {
        return "forward:/index.html";
    }

    @GetMapping(value = "/**/{path:[^\\.]*}")
    public String redirectAll() {
        return "forward:/index.html";
    }
}
