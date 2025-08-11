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

package cn.imoulife.protocol.config;

import cn.universal.core.protocol.ProtocolModuleRuntimeRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * ImouLife 协议模块自动配置类
 * 
 * <p>当 imoulife.protocol.enabled=true 时，自动扫描包并创建所有ImouLife相关的Bean
 * <p>通过 META-INF/spring.factories 实现 Spring Boot 自动配置
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Configuration
@ConditionalOnProperty(name = "imoulife.protocol.enabled", havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = "cn.imoulife.protocol")
@EnableConfigurationProperties(ImouLifeProperties.class)
@Slf4j
public class ImouLifeAutoConfiguration {

  @Autowired(required = false)
  private ImouLifeModuleInfo moduleInfo;

  public ImouLifeAutoConfiguration() {
    log.info("[ImouLife协议] ImouLife(乐橙)协议模块自动配置已启用");
  }
  
  @PostConstruct
  public void registerProtocol() {
    if (moduleInfo != null) {
      ProtocolModuleRuntimeRegistry.registerProtocol(moduleInfo);
      log.info("[ImouLife自动配置] 协议模块已注册到运行时注册表");
    }
  }
  
  @PreDestroy
  public void unregisterProtocol() {
    if (moduleInfo != null) {
      ProtocolModuleRuntimeRegistry.unregisterProtocol(moduleInfo.getCode());
      log.info("[ImouLife自动配置] 协议模块已从运行时注册表注销");
    }
  }
}
