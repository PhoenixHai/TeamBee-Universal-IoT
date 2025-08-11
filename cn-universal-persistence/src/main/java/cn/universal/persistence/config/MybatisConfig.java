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

package cn.universal.persistence.config;

import cn.universal.persistence.common.inteceptor.PerformanceInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author 🐤 Aleo ✉ Aleo@outlook.com
 *
 * @since ⏰ 2019/1/17
 */
@Configuration
@MapperScan(value = {"cn.universal.**.mapper"})
public class MybatisConfig {

  @Bean
  Interceptor sqlExplainInterceptor() {
    return new PerformanceInterceptor();
  }
  //  /*
  //   * 解决druid 日志报错：discard long time none received connection:xxx
  //   * */
  //  @PostConstruct
  //  public void setProperties(){
  //    System.setProperty("druid.mysql.usePingMethod","false");
  //  }

}
