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

package cn.universal.web.oauth; /// *
// * Copyright 2002-2011 the original Author or Authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
// package cn.universal.oauth;
//
// import cn.hutool.common.util.ObjectUtil;
// import cn.hutool.common.util.StrUtil;
// import cn.hutool.json.JSONUtil;
// import cn.universal.admin.monitor.third.AsyncService;
// import cn.universal.admin.system.third.IIotUserService;
// import cn.universal.core.utils.ServletUtils;
// import cn.universal.constant.iot.IotConstant;
// import cn.universal.persistence.entity.IoTUser;
// import java.util.concurrent.TimeUnit;
// import org.springframework.data.redis.common.StringRedisTemplate;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.oauth2.common.OAuth2AccessToken;
// import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
// import org.springframework.security.oauth2.provider.ClientDetails;
// import org.springframework.security.oauth2.provider.ClientDetailsService;
// import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
// import org.springframework.security.oauth2.provider.TokenRequest;
// import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
// import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
//
/// **
// * @Author Dave Syer
// */
// public class MyRefreshTokenGranter extends RefreshTokenGranter {
//
//  private static final String GRANT_TYPE = "refresh_token";
//
//  private final StringRedisTemplate stringRedisTemplate;
//  private final AuthenticationManager authenticationManager;
//  private final IIotUserService iIotUserService;
//
//  public MyRefreshTokenGranter(AuthorizationServerTokenServices tokenServices,
//      ClientDetailsService clientDetailsService,
//      OAuth2RequestFactory requestFactory,
//      StringRedisTemplate stringRedisTemplate, AuthenticationManager authenticationManager,
//      AsyncService asyncService, IIotUserService iIotUserService) {
//    super(tokenServices, clientDetailsService, requestFactory);
//    this.stringRedisTemplate = stringRedisTemplate;
//    this.authenticationManager = authenticationManager;
//    this.iIotUserService = iIotUserService;
//  }
//
//
//  @Override
//  public OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
//    String refreshToken = tokenRequest.getRequestParameters().get("refresh_token");
//    OAuth2AccessToken oAuth2AccessToken = getTokenServices().refreshAccessToken(refreshToken,
//        tokenRequest);
//    String username = tokenRequest.getRequestParameters().get("username");
//    if (StrUtil.isBlank(username)) {
//      return oAuth2AccessToken;
//    }
//    IoTUser user = iIotUserService.selectUserByUserName(username);
//    if (ObjectUtil.isEmpty(user)) {
//      throw new InvalidGrantException("用户名或密码错误");
//    }
//    boolean isExclusive = "true".equals(
//        JSONUtil.parseObj(user.getCfg()).getStr(IotConstant.EXCLUSIVE_FIRST_LOGIN));
//    if (isExclusive) {
//      String ip = ServletUtils.getClientIP();
//      String token = oAuth2AccessToken.getValue();
//      stringRedisTemplate.opsForValue()
//          .set(IotConstant.EXCLUSIVE_LOGIN + ":" + username, ip, 1800, TimeUnit.SECONDS);
//      stringRedisTemplate.opsForValue()
//          .set(IotConstant.EXCLUSIVE_LOGIN_TOKEN + ":" + username, token, 1800, TimeUnit.SECONDS);
//    }
//    return oAuth2AccessToken;
//  }
// }
