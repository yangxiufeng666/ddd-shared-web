package com.dsy.sunshine.web;

import com.dsy.sunshine.web.exception.GlobalHandlerExceptionResolver;
import com.dsy.sunshine.web.log.RequestIdMdcFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.Yangxiufeng
 * @date 2021-02-24
 * @time 15:18
 */
@Configuration
public class SunshineWebAutoConfiguration {


    @Bean
    public GlobalHandlerExceptionResolver globalHandlerExceptionResolver(){
        return new GlobalHandlerExceptionResolver();
    }

    @Bean
    public FilterRegistrationBean<RequestIdMdcFilter> filterRegistrationBean(){
        FilterRegistrationBean<RequestIdMdcFilter> registration = new FilterRegistrationBean();
        registration.setFilter(new RequestIdMdcFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(-1);
        return registration;
    }

}
