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

import cn.universal.core.iot.exception.BizException;
import cn.universal.core.utils.AESUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 内部feign调用鉴权 @Author Aleo
 *
 * @since 2023/6/1 9:50
 */
@Slf4j
public class InnerFeignFilter implements Filter {

  private static final String SIGN_KEY = "univ";

  private static final String INNER_CALL_KEY = "inner:call:key:";

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("init success InnerFeignFilter");
  }

  @Resource private StringRedisTemplate stringRedisTemplate;

  private static final long TIME = 1000 * 60 * 5;

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;

    String header = request.getHeader("inner-sign");
    if (StringUtils.isEmpty(header)) {
      throw new BizException("sign empty");
    }
    header = AESUtil.decrypt(header, "8171f862ecd152f39b4598c1e2a60f31");

    String[] signs = header.split(":");

    if (!SIGN_KEY.equals(signs[0])) {
      throw new BizException("sign error");
    }
    long time = Long.parseLong(signs[1]);
    long expireTime = System.currentTimeMillis() - time;
    if (expireTime > TIME) {
      throw new BizException("sign expire");
    }
    String cacheKey = INNER_CALL_KEY + signs[2];
    if (Objects.isNull(stringRedisTemplate.opsForValue().get(cacheKey))) {
      if ((expireTime <= 0)) {
        expireTime = TIME;
      }
      stringRedisTemplate.opsForValue().set(cacheKey, "1", expireTime, TimeUnit.MILLISECONDS);
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }
    throw new BizException("sign error");
  }
}
