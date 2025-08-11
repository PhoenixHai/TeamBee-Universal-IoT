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

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

public class OAuth2SmsAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

  public static final AuthorizationGrantType SMS_GRANT_TYPE = new AuthorizationGrantType("sms");

  private final String phone;
  private final String code;

  public OAuth2SmsAuthenticationToken(
      Authentication clientPrincipal, Map<String, Object> additionalParameters) {
    super(SMS_GRANT_TYPE, clientPrincipal, additionalParameters);
    this.phone = (String) additionalParameters.get("phone");
    this.code = (String) additionalParameters.get("code");
  }

  public String getPhone() {
    return phone;
  }

  public String getCode() {
    return code;
  }
}
