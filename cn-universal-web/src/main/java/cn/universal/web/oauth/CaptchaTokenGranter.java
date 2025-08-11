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

package cn.universal.web.oauth; // package cn.universal.oauth;
//
// import cn.hutool.common.util.ObjectUtil;
// import cn.hutool.common.util.StrUtil;
// import cn.hutool.json.JSONUtil;
// import cn.universal.admin.monitor.third.AsyncService;
// import cn.universal.core.constant.Constants;
// import cn.universal.admin.system.third.IIotUserService;
// import cn.universal.core.utils.RSAUtils;
// import cn.universal.core.utils.ServletUtils;
// import cn.universal.constant.iot.IotConstant;
// import cn.universal.persistence.entity.IoTUser;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.concurrent.TimeUnit;
// import java.util.regex.Matcher;
// import javax.servlet.http.HttpServletRequest;
// import org.springframework.data.redis.common.StringRedisTemplate;
// import org.springframework.security.authentication.AbstractAuthenticationToken;
// import org.springframework.security.authentication.AccountStatusException;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.common.Authentication;
// import org.springframework.security.oauth2.common.OAuth2AccessToken;
// import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
// import org.springframework.security.oauth2.provider.ClientDetails;
// import org.springframework.security.oauth2.provider.ClientDetailsService;
// import org.springframework.security.oauth2.provider.OAuth2Authentication;
// import org.springframework.security.oauth2.provider.OAuth2Request;
// import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
// import org.springframework.security.oauth2.provider.TokenRequest;
// import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
// import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
// import org.springframework.util.StringUtils;
//
/// **
// * oauth2验证码登录
// *
// * @Author Aleo
// * @since 2025/12/21 17:22
// */
//
// public class CaptchaTokenGranter extends AbstractTokenGranter {
//
//  private static final String GRANT_TYPE = "captcha";
//  private final StringRedisTemplate stringRedisTemplate;
//  private final AuthenticationManager authenticationManager;
//  private final IIotUserService iIotUserService;
/// /  private static Cache<String, Integer> retryCountCache = /
/// Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(256).build(); /  private
/// static Cache<String, Integer> ipBlackListCache = / Caffeine.newBuilder().expireAfterWrite(60,
/// TimeUnit.MINUTES).maximumSize(256).build();
//  /**
//   * 密码错误超过数量5次，则冻结5分钟
//   */
//  private static final Integer maxRetryCount = 5;
//  private static final Integer maxIpRetryCount = 15;
//  private final AsyncService asyncService;
//
//  public CaptchaTokenGranter(AuthorizationServerTokenServices tokenServices,
//      ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory,
//      StringRedisTemplate stringRedisTemplate, AuthenticationManager authenticationManager,
//      AsyncService asyncService, IIotUserService iIotUserService) {
//    super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
//    this.stringRedisTemplate = stringRedisTemplate;
//    this.authenticationManager = authenticationManager;
//    this.asyncService = asyncService;
//    this.iIotUserService = iIotUserService;
//  }
//
//  @Override
//  protected OAuth2AccessToken getAccessToken(ClientDetails client,
//      TokenRequest tokenRequest) {
//    HttpServletRequest request = ServletUtils.getRequest();
//    Map<String, String> parameters = new LinkedHashMap<>(
//        tokenRequest.getRequestParameters());
//    String username = parameters.get("username");
//    String password = parameters.get("password");
//    String captcha = parameters.get("code");
//    String uuid = parameters.get("uuid");
//    if (StringUtils.isEmpty(captcha)) {
//      throw new InvalidGrantException("验证码不能为空");
//    }
//    String code = stringRedisTemplate.opsForValue().get("captcha_codes:" + uuid);
//    if (StringUtils.isEmpty(code)) {
//      throw new InvalidGrantException("验证码已过期");
//    }
//
//    if (!code.equalsIgnoreCase(captcha)) {
//      throw new InvalidGrantException("验证码输入错误");
//    }
//    String ip = ServletUtils.getClientIP();
//    String retryKey = "loginCheck:retryCount:" + username;
//    String ipKey = "loginCheck:retryIpCount:" + ip;
//    Integer retryCount = stringRedisTemplate.opsForValue().get(retryKey) != null ?
// Integer.parseInt(
//        stringRedisTemplate.opsForValue().get(retryKey)) : 0;
//    Integer ipCount = stringRedisTemplate.opsForValue().get(ipKey) != null ? Integer.parseInt(
//        stringRedisTemplate.opsForValue().get(ipKey)) : 0;
//    if (ipCount > maxIpRetryCount) {
//      throw new InvalidGrantException("ip已锁定，请一小时后再试");
//    }
//
//    if (retryCount > maxRetryCount) {
//      throw new InvalidGrantException("账号已锁定，请15分钟后再试");
//    }
//    try {
//      String privateKey = stringRedisTemplate.opsForValue().get("RSAPrivateKey");
//      if (privateKey != null) {
//        password = RSAUtils.decrypt(password, privateKey);
//      }
//    } catch (Exception e) {
//      stringRedisTemplate.opsForValue()
//          .set(retryKey, String.valueOf(retryCount + 1), 10, TimeUnit.MINUTES);
//      stringRedisTemplate.opsForValue()
//          .set(ipKey, String.valueOf(ipCount + 1), 60, TimeUnit.MINUTES);
//      throw new InvalidGrantException("密钥已过期");
//    }
//    Matcher matcher = IotConstant.pattern.matcher(password);
//    if (!matcher.matches()) {
//      throw new InvalidGrantException("密码中必须包含字母、数字、特殊字符，至少8个字符，最多30个字符");
//    }
//    //独占式登录判断
//    IoTUser user = iIotUserService.selectUserByUserName(username);
//    if (ObjectUtil.isEmpty(user)) {
//      stringRedisTemplate.opsForValue()
//          .set(ipKey, String.valueOf(ipCount + 1), 60, TimeUnit.MINUTES);
//      throw new InvalidGrantException("用户名或密码错误");
//    }
//    if (IotConstant.UN_NORMAL.toString().equals(user.getStatus())) {
//      throw new InvalidGrantException("用户已冻结,请联系管理员解冻");
//    }
//    boolean isExclusive = "true".equals(
//        JSONUtil.parseObj(user.getCfg()).getStr(IotConstant.EXCLUSIVE_FIRST_LOGIN));
//    if (isExclusive) {
//      String loginedIp = stringRedisTemplate.opsForValue()
//          .get(IotConstant.EXCLUSIVE_LOGIN + ":" + username);
//      if (StrUtil.isNotBlank(loginedIp)) {
//        throw new InvalidGrantException("账号已登录，请先退出已登录的账号,登录ip：" + loginedIp);
//      }
//    }
//
//    Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
//    parameters.put("alias", user.getAlias());
//    ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
//    try {
//      userAuth = authenticationManager.authenticate(userAuth);
//    } catch (AccountStatusException | BadCredentialsException ase) {
//      //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
//      asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, ase.getMessage(), request);
//      stringRedisTemplate.opsForValue()
//          .set(retryKey, String.valueOf(retryCount + 1), 10, TimeUnit.MINUTES);
//      stringRedisTemplate.opsForValue()
//          .set(ipKey, String.valueOf(ipCount + 1), 60, TimeUnit.MINUTES);
/// /      retryCountCache.put(username, retryCount + 1);
//      throw new InvalidGrantException(ase.getMessage());
//    } // If the username/password are wrong the spec says we should send 400/invalid grant
//
//    if (userAuth == null || !userAuth.isAuthenticated()) {
//      throw new InvalidGrantException("Could not authenticate user: " + username);
//    }
//
//    asyncService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功", request);
/// /    retryCountCache.invalidate(username);
//    stringRedisTemplate.delete(retryKey);
//    stringRedisTemplate.delete(ipKey);
//    OAuth2Request oAuth2Request = getRequestFactory().createOAuth2Request(client,
//        tokenRequest);
//    OAuth2AccessToken oAuth2AccessToken = getTokenServices().createAccessToken(
//        new OAuth2Authentication(oAuth2Request, userAuth));
//    if (isExclusive) {
//      String token = oAuth2AccessToken.getValue();
//      stringRedisTemplate.opsForValue()
//          .set(IotConstant.EXCLUSIVE_LOGIN + ":" + username, ip, 1800, TimeUnit.SECONDS);
//      stringRedisTemplate.opsForValue()
//          .set(IotConstant.EXCLUSIVE_LOGIN_TOKEN + ":" + username, token, 1800, TimeUnit.SECONDS);
//    }
//
//    return oAuth2AccessToken;
//  }
// }
