package com.ani.taku_backend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${client.prod.front-url}")
    private String prodFrontUrl;

    @Value("${client.dev.front-url}")
    private String devFrontUrl;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 20, Sort.by("ID").descending()));
        resolvers.add(pageableResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(prodFrontUrl)
                .allowedOrigins(devFrontUrl)
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name()
                )
                .exposedHeaders("location")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/ws/**")
                .allowedOrigins(prodFrontUrl)
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name()
                )
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}