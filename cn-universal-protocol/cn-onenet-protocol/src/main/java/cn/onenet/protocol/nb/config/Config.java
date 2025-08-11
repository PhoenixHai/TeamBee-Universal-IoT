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

package cn.onenet.protocol.nb.config;

import cn.onenet.protocol.nb.exception.NBStatus;
import cn.onenet.protocol.nb.exception.OnenetNBException;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by zhuocongbin date 2018/3/15 Loading global properties
 */
public class Config {

  public static String domainName;

  static {
    Properties properties = new Properties();
    try {
      properties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
      domainName = (String) properties.get("domainName");
    } catch (IOException e) {
      throw new OnenetNBException(NBStatus.LOAD_CONFIG_ERROR);
    }
  }

  public static String getDomainName() {
    return domainName;
  }
}
