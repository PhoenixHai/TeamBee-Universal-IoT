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

package cn.universal.web.auth.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class SmsAuthenticationToken extends AbstractAuthenticationToken {

  private final String phone;
  private final String code;

  public SmsAuthenticationToken(String phone, String code) {
    super(null);
    this.phone = phone;
    this.code = code;
    setAuthenticated(false);
  }

  @Override
  public Object getCredentials() {
    return code;
  }

  @Override
  public Object getPrincipal() {
    return phone;
  }

  public String getPhone() {
    return phone;
  }

  public String getCode() {
    return code;
  }
}
