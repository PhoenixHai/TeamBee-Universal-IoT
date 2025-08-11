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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author 🐤 zhongxin
 *
 * @email ✉ asimooc@foxmail.com
 * @since ⏰ 2018/11/21
 */
@Data
@Slf4j
public class ImoulifeToken {

  private String accessToken;
  private String kitToken;
  private long expireTime;
  private long createTime;

  public ImoulifeToken() {
    this.setCreateTime(System.currentTimeMillis() / 1000);
  }

  public boolean isExpire() {
    boolean isExpire = System.currentTimeMillis() / 1000 > this.getCreateTime() + getExpireTime();
    if (isExpire) {
      log.info("access_token: {}已过期", this.getAccessToken());
      return true;
    } else {
      return false;
    }
  }

  public boolean isKitExpire() {
    // 默认每次获取新的kitToken
    return true;
  }
}
