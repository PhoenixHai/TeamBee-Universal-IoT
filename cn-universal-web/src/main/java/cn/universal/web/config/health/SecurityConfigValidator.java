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

package cn.universal.web.config.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 安全配置验证器 @Author Aleo
 */
@Slf4j
@Component
public class SecurityConfigValidator implements CommandLineRunner {

  @Value("${security.production.enabled:false}")
  private boolean productionSecurityEnabled;

  @Value("${security.allowed.ips:}")
  private String allowedIps;

  @Value("${spring.profiles.active:iot}")
  private String activeProfile;

  @Override
  public void run(String... args) throws Exception {
    log.info("=== 安全配置验证 ===");
    log.info("当前环境: {}", activeProfile);
    log.info("生产环境安全模式: {}", productionSecurityEnabled);
    log.info("允许的IP地址: {}", allowedIps.isEmpty() ? "所有内网IP" : allowedIps);

    if ("prod".equals(activeProfile) && !productionSecurityEnabled) {
      log.warn("⚠️  警告：生产环境未启用安全模式！");
    }

    if (productionSecurityEnabled && allowedIps.isEmpty()) {
      log.warn("⚠️  警告：生产环境安全模式已启用，但未配置允许的IP地址！");
    }

    log.info("=== 安全配置验证完成 ===");
  }
}
