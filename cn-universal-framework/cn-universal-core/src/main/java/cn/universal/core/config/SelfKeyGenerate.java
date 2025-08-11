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

package cn.universal.core.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONUtil;
import java.lang.reflect.Method;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

@Component("selfKeyGenerate")
public class SelfKeyGenerate implements KeyGenerator {

  @Override
  public Object generate(Object target, Method method, Object... params) {
    // 生成参数哈希值
    String paramsHash = generateParamsHash(params);
    return target.getClass().getSimpleName() + "::" + paramsHash;
  }

  private String generateParamsHash(Object... params) {
    if (params == null || params.length == 0) {
      return StrUtil.EMPTY;
    }
    // 使用MD5或SHA256生成短哈希
    return MD5.create().digestHex16(JSONUtil.toJsonStr(params));
  }
}
