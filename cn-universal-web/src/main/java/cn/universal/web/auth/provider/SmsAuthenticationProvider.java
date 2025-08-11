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

package cn.universal.web.auth.provider;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.monitor.service.AsyncService;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.core.constant.Constants;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.web.auth.token.SmsAuthenticationToken;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SmsAuthenticationProvider implements AuthenticationProvider {

  @Autowired private StringRedisTemplate stringRedisTemplate;
  @Autowired private UserDetailsService userDetailsService;

  @Autowired private AsyncService asyncService;
  @Autowired private IIotUserService iIotUserService;

  private static final Integer maxRetryCount = 5;
  private static final Integer maxIpRetryCount = 15;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;
    String phone = token.getPhone();
    String smsCode = token.getCode();

    // 获取IP地址 - 避免使用 ServletUtils.getRequest()
    String ip = "unknown";
    Object details = authentication.getDetails();
    if (details instanceof WebAuthenticationDetails) {
      ip = ((WebAuthenticationDetails) details).getRemoteAddress();
    }

    // 校验短信验证码
    if (!StringUtils.hasText(smsCode)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_grant", "短信验证码不能为空", null));
    }
    String code = stringRedisTemplate.opsForValue().get("sms_codes:" + phone);
    if (!StringUtils.hasText(code)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_grant", "短信验证码已过期", null));
    }
    if (!code.equalsIgnoreCase(smsCode)) {
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_grant", "短信验证码输入错误", null));
    }

    // 登录重试次数/IP 限制
    String retryKey = "loginCheck:retryCount:" + phone;
    String ipKey = "loginCheck:retryIpCount:" + ip;
    Integer retryCount =
        stringRedisTemplate.opsForValue().get(retryKey) != null
            ? Integer.parseInt(stringRedisTemplate.opsForValue().get(retryKey))
            : 0;
    Integer ipCount =
        stringRedisTemplate.opsForValue().get(ipKey) != null
            ? Integer.parseInt(stringRedisTemplate.opsForValue().get(ipKey))
            : 0;

    if (ipCount > maxIpRetryCount) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_grant", "ip已锁定，请一小时后再试", null));
    }
    if (retryCount > maxRetryCount) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_grant", "账号已锁定，请15分钟后再试", null));
    }

    // 根据手机号查找用户
    IoTUser user = iIotUserService.selectUserByMobile(phone);
    if (ObjectUtil.isEmpty(user)) {
      stringRedisTemplate
          .opsForValue()
          .set(ipKey, String.valueOf(ipCount + 1), 60, TimeUnit.MINUTES);
      throw new OAuth2AuthenticationException(new OAuth2Error("invalid_grant", "手机号未注册", null));
    }
    if (IotConstant.UN_NORMAL.toString().equals(user.getStatus())) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_grant", "用户已冻结,请联系管理员解冻", null));
    }

    // 独占式登录判断
    boolean isExclusive =
        "true".equals(JSONUtil.parseObj(user.getCfg()).getStr(IotConstant.EXCLUSIVE_FIRST_LOGIN));
    if (isExclusive) {
      String loginedIp =
          stringRedisTemplate
              .opsForValue()
              .get(IotConstant.EXCLUSIVE_LOGIN + ":" + user.getUsername());
      if (StrUtil.isNotBlank(loginedIp)) {
        throw new OAuth2AuthenticationException(
            new OAuth2Error("invalid_grant", "账号已登录，请先退出已登录的账号,登录ip：" + loginedIp, null));
      }
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

    // 登录成功
    asyncService.recordLogininfor(user.getUsername(), Constants.LOGIN_SUCCESS, "短信验证码登录成功", null);
    stringRedisTemplate.delete(retryKey);
    stringRedisTemplate.delete(ipKey);

    if (isExclusive) {
      stringRedisTemplate
          .opsForValue()
          .set(IotConstant.EXCLUSIVE_LOGIN + ":" + user.getUsername(), ip, 1800, TimeUnit.SECONDS);
    }

    UsernamePasswordAuthenticationToken result =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    result.setDetails(token.getDetails());
    return result;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return SmsAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
