/*
 *
 * Copyright (c) 2025, iot-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.config.filter;

import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/6/27
 */
@Configuration
public class FilterConfig {

  /*
   * 创建一个bean
   * @return
   */
  @Bean(name = "responseHeadFilter")
  public Filter responseHeadFilter() {
    return new ResponseHeadFilter();
  }

  @Bean
  public FilterRegistrationBean responseHeadFilterInit() {
    FilterRegistrationBean registration = new FilterRegistrationBean();

    registration.setFilter(responseHeadFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(0);
    return registration;
  }

  @Bean(name = "innerFeignFilter")
  public Filter innerFeignFilter() {
    return new InnerFeignFilter();
  }

  @Bean
  public FilterRegistrationBean innerFilterInit() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(innerFeignFilter());
    registration.addUrlPatterns("/inner/*");
    registration.setOrder(2); // ??写999就不行
    return registration;
  }

  @Bean(name = "xssFilter")
  public Filter xssFilter() {
    return new XssFilter();
  }

  @Bean
  public FilterRegistrationBean xssFilterInit() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(xssFilter());
    registration.addUrlPatterns("/*");
    registration.addUrlPatterns("!/admin/protocol/protocol*");
    registration.setOrder(1);
    return registration;
  }

  /*
   * 创建一个bean
   * @return
   */
  @Bean(name = "replaceStreamFilter")
  public Filter replaceStreamFilter() {
    return new ReplaceStreamFilter();
  }

  @Bean
  public FilterRegistrationBean replaceStreamFilterInit() {
    FilterRegistrationBean registration = new FilterRegistrationBean();
    registration.setFilter(replaceStreamFilter());
    registration.addUrlPatterns("/api/*");
    registration.setOrder(0);
    return registration;
  }
}
