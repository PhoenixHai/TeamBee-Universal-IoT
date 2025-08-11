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

import cn.universal.web.config.xss.XssHttpServletRequestWrapper;
import cn.universal.web.config.xss.XssProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

/**
 * @version 1.0 @Author ZQQ
 * @desc :
 * @since 2025/9/22 15:54
 */
@Slf4j
public class XssFilter implements Filter {

  @Resource private XssProperties xssProperties;

  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("init success XSS");
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String path = ((HttpServletRequest) request).getServletPath();

    if (!xssProperties.getEnabled() || isXssSkip(path)) {
      chain.doFilter(request, response);
    } else {
      XssHttpServletRequestWrapper xssRequest =
          new XssHttpServletRequestWrapper((HttpServletRequest) request);
      chain.doFilter(xssRequest, response);
    }
  }

  private boolean isXssSkip(String path) {
    return xssProperties.getSkipUrl().stream()
        .anyMatch(pattern -> antPathMatcher.match(pattern, path));
  }

  @Override
  public void destroy() {}
}
