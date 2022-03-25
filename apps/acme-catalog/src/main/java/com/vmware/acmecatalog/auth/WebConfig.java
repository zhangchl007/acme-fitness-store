package com.vmware.acmecatalog.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private AuthorizeResource authorizeResource;

    public WebConfig(AuthorizeResource authorizeResource) {
        this.authorizeResource = authorizeResource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Custom interceptor, add intercept path and exclude intercept path
        registry.addInterceptor(authorizeResource).addPathPatterns("/products");
    }
}
