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

package cn.universal.web.config.intercepter;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 */
@Configuration
public class WebAppConfig implements WebMvcConfigurer {

  @Resource
  private WhitelistInterceptor whitelistInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AuthContextIntercepter()).addPathPatterns("/api/**", "/admin/**");
    registry.addInterceptor(new APILogIntercepter()).addPathPatterns("/api/**");
    registry
        .addInterceptor(whitelistInterceptor)
        .addPathPatterns("/iot/**", "/api/**", "/debug/**");
  }
}
