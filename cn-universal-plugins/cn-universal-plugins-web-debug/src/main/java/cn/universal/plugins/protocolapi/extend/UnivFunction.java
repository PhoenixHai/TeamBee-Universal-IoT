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

package cn.universal.plugins.protocolapi.extend;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.iot.engine.annotation.Comment;
import cn.universal.core.iot.engine.annotation.Function;
import cn.universal.core.iot.engine.functions.DateExtension;
import jakarta.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 封装定义的脚本
 *
 * @version 1.0 @Author Aleo
 * @since 2023/5/23
 */
@Component
public class UnivFunction {

  private final String url = "http://apilocate.amap.com/position?";
  // 地址逆编码
  private static final String reUrl = "https://restapi.amap.com/v3/geocode/regeo?";
  private static final String key = "f404bbd3dc312d7c0e1ed4c4a1472f5f";
  private final String imei = "86";
  private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
  private static final double PI = 3.14159265358979324;

  private static final double a = 6378245.0;

  private static final double EE = 0.00669342162296594323;
  static byte[] crc16_h = {
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x01,
    (byte) 0xC0,
    (byte) 0x80,
    (byte) 0x41,
    (byte) 0x00,
    (byte) 0xC1,
    (byte) 0x81,
    (byte) 0x40
  };

  static byte[] crc16_l = {
    (byte) 0x00,
    (byte) 0xC0,
    (byte) 0xC1,
    (byte) 0x01,
    (byte) 0xC3,
    (byte) 0x03,
    (byte) 0x02,
    (byte) 0xC2,
    (byte) 0xC6,
    (byte) 0x06,
    (byte) 0x07,
    (byte) 0xC7,
    (byte) 0x05,
    (byte) 0xC5,
    (byte) 0xC4,
    (byte) 0x04,
    (byte) 0xCC,
    (byte) 0x0C,
    (byte) 0x0D,
    (byte) 0xCD,
    (byte) 0x0F,
    (byte) 0xCF,
    (byte) 0xCE,
    (byte) 0x0E,
    (byte) 0x0A,
    (byte) 0xCA,
    (byte) 0xCB,
    (byte) 0x0B,
    (byte) 0xC9,
    (byte) 0x09,
    (byte) 0x08,
    (byte) 0xC8,
    (byte) 0xD8,
    (byte) 0x18,
    (byte) 0x19,
    (byte) 0xD9,
    (byte) 0x1B,
    (byte) 0xDB,
    (byte) 0xDA,
    (byte) 0x1A,
    (byte) 0x1E,
    (byte) 0xDE,
    (byte) 0xDF,
    (byte) 0x1F,
    (byte) 0xDD,
    (byte) 0x1D,
    (byte) 0x1C,
    (byte) 0xDC,
    (byte) 0x14,
    (byte) 0xD4,
    (byte) 0xD5,
    (byte) 0x15,
    (byte) 0xD7,
    (byte) 0x17,
    (byte) 0x16,
    (byte) 0xD6,
    (byte) 0xD2,
    (byte) 0x12,
    (byte) 0x13,
    (byte) 0xD3,
    (byte) 0x11,
    (byte) 0xD1,
    (byte) 0xD0,
    (byte) 0x10,
    (byte) 0xF0,
    (byte) 0x30,
    (byte) 0x31,
    (byte) 0xF1,
    (byte) 0x33,
    (byte) 0xF3,
    (byte) 0xF2,
    (byte) 0x32,
    (byte) 0x36,
    (byte) 0xF6,
    (byte) 0xF7,
    (byte) 0x37,
    (byte) 0xF5,
    (byte) 0x35,
    (byte) 0x34,
    (byte) 0xF4,
    (byte) 0x3C,
    (byte) 0xFC,
    (byte) 0xFD,
    (byte) 0x3D,
    (byte) 0xFF,
    (byte) 0x3F,
    (byte) 0x3E,
    (byte) 0xFE,
    (byte) 0xFA,
    (byte) 0x3A,
    (byte) 0x3B,
    (byte) 0xFB,
    (byte) 0x39,
    (byte) 0xF9,
    (byte) 0xF8,
    (byte) 0x38,
    (byte) 0x28,
    (byte) 0xE8,
    (byte) 0xE9,
    (byte) 0x29,
    (byte) 0xEB,
    (byte) 0x2B,
    (byte) 0x2A,
    (byte) 0xEA,
    (byte) 0xEE,
    (byte) 0x2E,
    (byte) 0x2F,
    (byte) 0xEF,
    (byte) 0x2D,
    (byte) 0xED,
    (byte) 0xEC,
    (byte) 0x2C,
    (byte) 0xE4,
    (byte) 0x24,
    (byte) 0x25,
    (byte) 0xE5,
    (byte) 0x27,
    (byte) 0xE7,
    (byte) 0xE6,
    (byte) 0x26,
    (byte) 0x22,
    (byte) 0xE2,
    (byte) 0xE3,
    (byte) 0x23,
    (byte) 0xE1,
    (byte) 0x21,
    (byte) 0x20,
    (byte) 0xE0,
    (byte) 0xA0,
    (byte) 0x60,
    (byte) 0x61,
    (byte) 0xA1,
    (byte) 0x63,
    (byte) 0xA3,
    (byte) 0xA2,
    (byte) 0x62,
    (byte) 0x66,
    (byte) 0xA6,
    (byte) 0xA7,
    (byte) 0x67,
    (byte) 0xA5,
    (byte) 0x65,
    (byte) 0x64,
    (byte) 0xA4,
    (byte) 0x6C,
    (byte) 0xAC,
    (byte) 0xAD,
    (byte) 0x6D,
    (byte) 0xAF,
    (byte) 0x6F,
    (byte) 0x6E,
    (byte) 0xAE,
    (byte) 0xAA,
    (byte) 0x6A,
    (byte) 0x6B,
    (byte) 0xAB,
    (byte) 0x69,
    (byte) 0xA9,
    (byte) 0xA8,
    (byte) 0x68,
    (byte) 0x78,
    (byte) 0xB8,
    (byte) 0xB9,
    (byte) 0x79,
    (byte) 0xBB,
    (byte) 0x7B,
    (byte) 0x7A,
    (byte) 0xBA,
    (byte) 0xBE,
    (byte) 0x7E,
    (byte) 0x7F,
    (byte) 0xBF,
    (byte) 0x7D,
    (byte) 0xBD,
    (byte) 0xBC,
    (byte) 0x7C,
    (byte) 0xB4,
    (byte) 0x74,
    (byte) 0x75,
    (byte) 0xB5,
    (byte) 0x77,
    (byte) 0xB7,
    (byte) 0xB6,
    (byte) 0x76,
    (byte) 0x72,
    (byte) 0xB2,
    (byte) 0xB3,
    (byte) 0x73,
    (byte) 0xB1,
    (byte) 0x71,
    (byte) 0x70,
    (byte) 0xB0,
    (byte) 0x50,
    (byte) 0x90,
    (byte) 0x91,
    (byte) 0x51,
    (byte) 0x93,
    (byte) 0x53,
    (byte) 0x52,
    (byte) 0x92,
    (byte) 0x96,
    (byte) 0x56,
    (byte) 0x57,
    (byte) 0x97,
    (byte) 0x55,
    (byte) 0x95,
    (byte) 0x94,
    (byte) 0x54,
    (byte) 0x9C,
    (byte) 0x5C,
    (byte) 0x5D,
    (byte) 0x9D,
    (byte) 0x5F,
    (byte) 0x9F,
    (byte) 0x9E,
    (byte) 0x5E,
    (byte) 0x5A,
    (byte) 0x9A,
    (byte) 0x9B,
    (byte) 0x5B,
    (byte) 0x99,
    (byte) 0x59,
    (byte) 0x58,
    (byte) 0x98,
    (byte) 0x88,
    (byte) 0x48,
    (byte) 0x49,
    (byte) 0x89,
    (byte) 0x4B,
    (byte) 0x8B,
    (byte) 0x8A,
    (byte) 0x4A,
    (byte) 0x4E,
    (byte) 0x8E,
    (byte) 0x8F,
    (byte) 0x4F,
    (byte) 0x8D,
    (byte) 0x4D,
    (byte) 0x4C,
    (byte) 0x8C,
    (byte) 0x44,
    (byte) 0x84,
    (byte) 0x85,
    (byte) 0x45,
    (byte) 0x87,
    (byte) 0x47,
    (byte) 0x46,
    (byte) 0x86,
    (byte) 0x82,
    (byte) 0x42,
    (byte) 0x43,
    (byte) 0x83,
    (byte) 0x41,
    (byte) 0x81,
    (byte) 0x80,
    (byte) 0x40
  };
  private AtomicLong currentSerialNumber = new AtomicLong(1L);
  @Resource private StringRedisTemplate stringRedisTemplate;

  @Function
  @Comment("基于CSQ信号强度评估")
  public String csq(@Comment(name = "csq", value = "信号强度指示值(0-31)") Integer csq) {
    // 参数有效性校验
    if (csq == null || csq < 0 || csq > 31) {
      return "参数异常";
    }
    if (csq == 99) {
      return "信道无效"; // 特殊错误码处理
    }
    // CSQ转RSSI（参考网页2中CSQ与RSSI的换算关系）
    double rssi = csq * 2 - 113; // 公式：RSSI = CSQ*2 - 113

    // 信号等级划分（综合网页6中国移动标准与网页3行业实践）
    if (rssi > -65) {
      return "极好";
    }
    if (rssi > -75) {
      return "好";
    }
    if (rssi > -85) {
      return "中";
    }
    if (rssi > -95) {
      return "差";
    }
    return "极差";
  }

  @Function
  @Comment("基于RSRP和SNR的信号强度评估")
  public String rsrpSnr(
      @Comment(name = "rsrp", value = "参考信号接收功率(dBm)") Double rsrp,
      @Comment(name = "snr", value = "信噪比(可能为10倍值需转换)") Double snr) {
    // 参数有效性校验（网页6标准范围）
    if (rsrp == null || snr == null) {
      return "参数缺失";
    }
    if (rsrp > -40 || rsrp < -140) {
      return "RSRP超限";
    }
    if (snr < 0) {
      return "SNR异常";
    }
    // 处理SNR放大倍数（如模块返回30表示实际值3dB）
    double realSnr = snr >= 100 ? snr / 10.0 : snr;
    // RSRP等级判断（网页6标准）
    String rsrpLevel = "极差";
    if (rsrp > -85) {
      rsrpLevel = "极好";
    } else if (rsrp >= -95) {
      rsrpLevel = "好";
    } else if (rsrp >= -105) {
      rsrpLevel = "中";
    } else if (rsrp >= -115) {
      rsrpLevel = "差";
    }
    // SNR等级判断（网页6标准）
    String snrLevel = "极差";
    if (realSnr > 25) {
      snrLevel = "极好";
    } else if (realSnr >= 16) {
      snrLevel = "好";
    } else if (realSnr >= 11) {
      snrLevel = "中";
    } else if (realSnr >= 3) {
      snrLevel = "差";
    }
    return Stream.of(rsrpLevel, snrLevel)
        .min(
            Comparator.comparingInt(
                level -> {
                  // 手动构建等级优先级映射（网页1、网页5实现思路）
                  Map<String, Integer> priorityMap = new HashMap<>();
                  priorityMap.put("极好", 5);
                  priorityMap.put("好", 4);
                  priorityMap.put("中", 3);
                  priorityMap.put("差", 2);
                  priorityMap.put("极差", 1);
                  return priorityMap.get(level);
                }))
        .orElse("未知");
  }

  @Comment("获取指定key的缓存")
  @Function
  public String redisOfValue(@Comment(name = "key", value = "目标对象") String key) {
    // 固定前缀 magicRedisSign:
    key = IotConstant.MAGIC_REDIS_SIGN + key;
    String value = stringRedisTemplate.opsForValue().get(key);
    return value;
  }

  @Comment("设置指定key的缓存")
  @Function
  public Boolean redisOfValueSet(
      @Comment(name = "key", value = "目标对象") String key,
      @Comment(name = "value", value = "目标对象") String value) {
    // 固定前缀 magicRedisSign:
    key = IotConstant.MAGIC_REDIS_SIGN + key;
    stringRedisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
    return true;
  }

  @Comment("获取通用流水号")
  @Function
  public String randomSign() {
    long nextSerialNumber = currentSerialNumber.incrementAndGet();
    if (nextSerialNumber > 0xFFFFL) {
      currentSerialNumber.set(1L);
      nextSerialNumber = currentSerialNumber.incrementAndGet();
    }
    return String.valueOf(nextSerialNumber);
  }

  @Comment("base64解码")
  @Function
  public String base64Decode(@Comment(name = "value", value = "目标对象") String payload) {
    return Base64.decodeStr(payload);
  }

  @Comment("base64编码")
  @Function
  public String base64Encode(@Comment(name = "value", value = "目标对象") String payload) {
    return Base64.encode(payload);
  }

  @Comment("判断是否是json字符串")
  @Function
  public Boolean isJson(@Comment(name = "value", value = "目标对象") String payload) {
    try {
      return JSONUtil.isTypeJSON(payload);
    } catch (Exception e) {
      return false;
    }
  }

  @Comment("判断是否是json数组")
  @Function
  public Boolean isJsonArray(@Comment(name = "value", value = "目标对象") String payload) {
    try {
      return JSONUtil.isTypeJSON(payload);
    } catch (Exception e) {
      return false;
    }
  }

  @Comment("字符串转JSON对象")
  @Function
  public Object toJson(@Comment(name = "value", value = "目标对象") String payload) {
    if (JSONUtil.isTypeJSON(payload)) {
      return JSONUtil.parseObj(payload);
    }
    return payload;
  }

  @Comment("字符串转JSON数组")
  @Function
  public Object toJsonArray(@Comment(name = "value", value = "目标对象") String payload) {
    if (JSONUtil.isTypeJSONArray(payload)) {
      return JSONUtil.parseArray(payload);
    }
    return payload;
  }

  @Comment("JSON转字符串")
  @Function
  public String jsonToStr(@Comment(name = "value", value = "目标对象") Object payload) {
    if (JSONUtil.isTypeJSON(payload + "")) {
      return JSONUtil.toJsonStr(payload);
    }
    return payload + "";
  }

  @Comment("JSON数组转字符串")
  @Function
  public String jsonArrayToStr(@Comment(name = "value", value = "目标对象") Object payload) {
    if (JSONUtil.isTypeJSONArray(payload + "")) {
      return JSONUtil.toJsonStr(payload);
    }
    return payload + "";
  }

  @Comment("base64转16进制")
  @Function
  public String base64ToHex(@Comment(name = "value", value = "目标对象") String payload) {
    return Convert.toHex(Base64.decode(payload));
  }

  @Comment("16进制转base64")
  @Function
  public String hexToBase64(@Comment(name = "value", value = "目标对象") String payload) {
    if (StrUtil.isBlank(payload)) {
      return "";
    }
    int len = payload.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte)
              ((Character.digit(payload.charAt(i), 16) << 4)
                  + Character.digit(payload.charAt(i + 1), 16));
    }
    return Base64.encode(data);
  }

  @Comment("判断字符串为空")
  @Function
  public boolean isEmpty(@Comment(name = "value", value = "目标对象") String value) {
    return StrUtil.isEmpty(value);
  }

  @Function
  @Comment("日期格式化")
  public String date_format(
      @Comment(name = "target", value = "目标日期") String dateStr,
      @Comment(name = "pattern", value = "格式") String pattern) {
    return dateStr == null ? null : DateExtension.format(DateUtil.parse(dateStr), pattern);
  }

  @Function
  @Comment("时间戳转日期")
  public DateTime timestampToDate(@Comment(name = "target", value = "时间戳") String dateStr) {
    return dateStr == null ? null : DateUtil.date(Long.parseLong(dateStr));
  }

  @Function
  @Comment("字符串反转")
  public String reverse(@Comment(name = "target", value = "参数") String str) {
    return StrUtil.reverse(str);
  }

  @Function
  @Comment("字符串转Ascii")
  public String stringToAscii(@Comment(name = "target", value = "字符串") String value) {
    StringBuffer sbu = new StringBuffer();
    char[] chars = value.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (i != chars.length - 1) {
        sbu.append((int) chars[i]).append(",");
      } else {
        sbu.append((int) chars[i]);
      }
    }
    return sbu.toString();
  }

  @Function
  @Comment("Ascii转字符串")
  public String asciiToString(@Comment(name = "target", value = "字符串") String value) {
    StringBuffer sbu = new StringBuffer();
    String[] chars = value.split(",");
    for (int i = 0; i < chars.length; i++) {
      sbu.append((char) Integer.parseInt(chars[i]));
    }
    return sbu.toString();
  }

  @Function
  @Comment("Ascii转字符串")
  public String convertAsciiToStr(@Comment(name = "target", value = "字符串") String value) {
    byte[] bytes = new byte[value.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      int index = i * 2;
      int j = Integer.parseInt(value.substring(index, index + 2), 16);
      bytes[i] = (byte) j;
    }
    String str = new String(bytes);
    return str;
  }

  @Function
  @Comment("字符串转utf-8")
  public String stringToUTF8(@Comment(name = "target", value = "字符串") String str)
      throws UnsupportedEncodingException {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLEncoder.encode(str, "UTF-8");
  }

  @Function
  @Comment("URL解码")
  public String urlDecode(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLDecoder.decode(str, CharsetUtil.CHARSET_UTF_8);
  }

  @Function
  @Comment("wifi查询坐标")
  public String locateByWifi(@Comment(name = "target", value = "字符串列表") List<String> wifis) {
    // 高德坐标查询
    String queryUrl =
        url
            + "accesstype=1&imei="
            + imei
            + "&macs="
            + String.join("|", wifis)
            + "&output=json&key="
            + key;
    JSONObject obj = new JSONObject();
    String lng = "";
    String lat = "";
    try {
      if (CollectionUtil.isNotEmpty(wifis)) {
        String result = JSONUtil.parseObj(HttpUtil.get(queryUrl, 5000)).getStr("result");
        if (StrUtil.isNotEmpty(result)) {
          JSONObject jsonObject = JSONUtil.parseObj(result);
          String[] finalLocation = jsonObject.getStr("location").split(",");
          lng = finalLocation[0];
          lat = finalLocation[1];
          obj.set("location", jsonObject.getStr("desc"));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      obj.set("lng", lng);
      obj.set("lat", lat);
    }
    return JSONUtil.toJsonStr(obj);
  }

  @Function
  @Comment("基站查询坐标")
  public String locateByLbs(
      @Comment(name = "target", value = "是否是CDMA卡") Boolean isCDMA,
      @Comment(name = "target", value = "字符串列表") List<String> lbs) {
    JSONObject obj = new JSONObject();
    String lng = "";
    String lat = "";
    if (CollectionUtil.isEmpty(lbs)) {
      return JSONUtil.toJsonStr(obj);
    }
    // 460,00,22572,11037,-60|460,00,22572,21845,-70|460,00,22572,21037,-70
    String bts = lbs.get(0);
    lbs.remove(0);
    String nearbts = String.join("|", lbs);
    // 高德坐标查询
    String queryUrl =
        url
            + "accesstype=0&imei="
            + imei
            + "&cdma="
            + (isCDMA ? "1" : "0")
            + "&bts="
            + bts
            + "&nearbts="
            + nearbts
            + "&output=json&key="
            + key;
    try {
      String result = JSONUtil.parseObj(HttpUtil.get(queryUrl, 5000)).getStr("result");
      if (StrUtil.isNotEmpty(result)) {
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String[] finalLocation = jsonObject.getStr("location").split(",");
        lng = finalLocation[0];
        lat = finalLocation[1];
        obj.set("location", jsonObject.getStr("desc"));
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      obj.set("lng", lng);
      obj.set("lat", lat);
    }
    return JSONUtil.toJsonStr(obj);
  }

  // 入参经度在前纬度在后，逗号分隔
  @Function
  @Comment("坐标转地址")
  public static String coordinateToAddr(
      @Comment(name = "target", value = "字符串") String coordinate) {
    JSONObject obj = new JSONObject();
    // 高德坐标查询
    String queryUrl =
        reUrl + "location=" + coordinate + "&extensions=base&radius=100&output=json&key=" + key;
    try {
      String result = JSONUtil.parseObj(HttpUtil.get(queryUrl, 5000)).getStr("regeocode");
      if (StrUtil.isNotEmpty(result)) {
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String address = jsonObject.getStr("formatted_address");
        return address;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /***
   * CRC每个字节异或
   * @param str
   * @return
   */
  @Function
  @Comment("CRC每个字节异或")
  public String checkCRCTwoDigit(@Comment(name = "target", value = "字符串") String str) {
    if (str == null || str.length() % 2 != 0) {
      return null;
    }
    Integer result = Integer.parseInt(str.substring(0, 2), 16);
    for (int i = 2; i < str.length() - 1; i += 2) {
      Integer c1 = Integer.parseInt(str.substring(i, i + 2), 16);
      result = (result ^ c1);
    }
    return Integer.toHexString(result);
  }

  @Function
  @Comment("字符串转unicode")
  public static String stringToUnicode(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    final int len = str.length();
    final StrBuilder unicode = StrBuilder.create(str.length() * 6);
    char c;
    for (int i = 0; i < len; i++) {
      c = str.charAt(i);
      unicode.append(HexUtil.toUnicodeHex(c)); // 形如 \ue696，刚好占6个字符
    }
    return unicode.toString();
  }

  @Function
  @Comment("十六进制转GB2312编码汉字")
  public String hexToGB2312(@Comment(name = "target", value = "字符串") String hexStr) {
    if (StrUtil.isBlank(hexStr)) {
      return hexStr;
    }
    byte[] a = HexUtil.decodeHex(hexStr);
    return new String(a, Charset.forName("GB2312")).trim();
  }

  @Function
  @Comment("GB2312编码汉字转十六进制")
  public String gb2312ToHex(@Comment(name = "target", value = "字符串") String str)
      throws UnsupportedEncodingException {
    if (StrUtil.isEmpty(str)) {
      return str;
    }
    return URLEncoder.encode(str, "GB2312").replaceAll("%", "");
  }

  /***
   * CRC校验
   * @param crc
   * @return
   */
  @Function
  @Comment("CRC校验")
  public String checkCRC(@Comment(name = "target", value = "字符串") String crc) {
    if (crc.length() % 2 != 0) {
      return null;
    }
    int sum = 0;
    for (int i = 0; i < crc.length(); i += 2) {
      sum += Integer.parseInt(crc.substring(i, i + 2), 16);
    }
    String var = String.format("%X", sum);
    return var.substring(var.length() - 2);
  }

  /***
   * CRC16 MODBUS
   * @param str
   * @return
   */
  @Function
  @Comment("CRC16 MODBUS")
  public String checkCRCMODBUS(@Comment(value = "字符串") String str) {
    str = str.replaceAll("(.{2})", "$1,");
    String[] bytes = str.split(",");
    byte[] data = new byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      data[i] = (byte) (Integer.parseInt(bytes[i], 16));
    }
    int crc = 0xff;
    int ucCRCHi = 0xff;
    int ucCRCLo = 0xff;
    int iIndex;
    for (int i = 0; i < data.length; ++i) {
      iIndex = (ucCRCLo ^ data[i]) & 0x00ff;
      ucCRCLo = ucCRCHi ^ crc16_h[iIndex];
      ucCRCHi = crc16_l[iIndex];
    }
    crc = ((ucCRCHi & 0x00ff) << 8) | (ucCRCLo & 0x00ff) & 0xffff;
    // 高低位互换，输出符合相关工具对Modbus CRC16的运算
    crc = ((crc & 0xFF00) >> 8) | ((crc & 0x00FF) << 8);
    return String.format("%04X", crc);
  }

  /***
   * CRC-CCITT(0xFFFF) 校验
   */
  @Function
  @Comment("CRC-CCITT(0xFFFF) 校验")
  public static String checkCRCCCITT(@Comment(name = "target", value = "字符串") String crc) {
    if (StrUtil.isBlank(crc)) {
      return null;
    }
    byte[] test = HexUtil.decodeHex(crc);
    return Integer.toHexString(getCRC(test));
  }

  /** 字符串每两位倒转 */
  @Function
  @Comment("字符串每两位倒转")
  public String reverseTwoDigit(@Comment(name = "target", value = "字符串") String str) {
    String newInput = str.replaceAll("(.{2})", "$1,");
    List<String> inputList = Arrays.asList(newInput.split(","));
    Collections.reverse(inputList);
    return String.join("", inputList);
  }

  /** int强转float */
  @Function
  @Comment("int强转float")
  public Float intToFloat(@Comment(name = "target", value = "字符串") Integer number) {
    if (number == null) {
      return null;
    }
    return Float.intBitsToFloat(number);
  }

  /** long强转double */
  @Function
  @Comment("long强转double")
  public Double longToDouble(@Comment(name = "target", value = "字符串") Long number) {
    if (number == null) {
      return null;
    }
    return Double.longBitsToDouble(number);
  }

  /** 地球坐标系转火星坐标系 */
  @Function
  @Comment("地球坐标系转火星坐标系")
  public String wgs84ToGcj02(@Comment(name = "target", value = "字符串") String coordinate) {
    JSONObject obj = new JSONObject();
    String lng = "";
    String lat = "";
    if (coordinate == null || !coordinate.contains(",")) {
      return JSONUtil.toJsonStr(obj);
    }
    double wgLon = Double.parseDouble(coordinate.split(",")[0]);
    double wgLat = Double.parseDouble(coordinate.split(",")[1]);
    double[] c = transform(wgLon, wgLat);
    obj.set("lng", String.format("%.6f", c[0]));
    obj.set("lat", String.format("%.6f", c[1]));

    return JSONUtil.toJsonStr(obj);
  }

  /** 火星坐标系转地球坐标系 */
  @Function
  @Comment("火星坐标系转地球坐标系")
  public String gcj02ToWgs84(@Comment(name = "target", value = "字符串") String coordinate) {
    JSONObject obj = new JSONObject();
    if (coordinate == null || !coordinate.contains(",")) {
      return JSONUtil.toJsonStr(obj);
    }
    double wgLon = Double.parseDouble(coordinate.split(",")[0]);
    double wgLat = Double.parseDouble(coordinate.split(",")[1]);
    Map<String, Double> map = delta(wgLat, wgLon);
    double lat = map.get("lat");
    double lon = map.get("lon");
    obj.set("lng", String.format("%.6f", wgLon - lon));
    obj.set("lat", String.format("%.6f", wgLat - lat));
    return JSONUtil.toJsonStr(obj);
  }

  /** 车辆 十六进制转中文gbk 内部bytebuf中转 */
  @Function
  @Comment("车辆 十六进制转中文gbk  内部bytebuf中转")
  public String hexToGBK(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isBlank(str)) {
      return null;
    }
    byte[] a = HexUtil.decodeHex(str);
    return new String(a, Charset.forName("gbk")).trim();
  }

  /** 车辆 中文gbk转十六进制 内部bytebuf中转 */
  @Function
  @Comment("车辆 中文gbk转十六进制  内部bytebuf中转")
  public String GBKToHex(@Comment(name = "target", value = "字符串") String str) {
    if (StrUtil.isBlank(str)) {
      return null;
    }
    byte[] bytes = str.getBytes(Charset.forName("GBK"));
    return HexUtil.encodeHexStr(bytes);
  }

  public static double[] transform(double wgLon, double wgLat) {
    double[] result = new double[2];

    if (outOfChina(wgLon, wgLat)) {
      result[0] = wgLon;
      result[1] = wgLat;
      return result;
    }
    double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
    double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
    double radLat = wgLat / 180.0 * PI;
    double magic = Math.sin(radLat);
    magic = 1 - EE * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - EE)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
    result[0] = wgLon + dLon;
    result[1] = wgLat + dLat;
    return result;
  }

  private static double transformLon(double x, double y) {
    double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
    return ret;
  }

  private static double transformLat(double x, double y) {
    double ret =
        -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
    ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
    return ret;
  }

  private static Map delta(double lat, double lon) {
    double dLat = transformLat(lon - 105.0, lat - 35.0);
    double dLon = transformLon(lon - 105.0, lat - 35.0);
    double radLat = lat / 180.0 * PI;
    double magic = Math.sin(radLat);
    magic = 1 - EE * magic * magic;
    double sqrtMagic = Math.sqrt(magic);
    dLat = (dLat * 180.0) / ((a * (1 - EE)) / (magic * sqrtMagic) * PI);
    dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);

    Map<String, Double> map = new HashMap<>();
    map.put("lat", dLat);
    map.put("lon", dLon);
    return map;
  }

  private static boolean outOfChina(double lon, double lat) {
    if ((lon < 72.004 || lon > 137.8347) && (lat < 0.8293 || lat > 55.8271)) {
      return true;
    } else {
      return false;
    }
  }

  public static int getCRC(byte[] bytes) {
    int crc = 0xFFFF;
    int polynomial = 0x1021;
    for (byte b : bytes) {
      for (int i = 0; i < 8; i++) {
        boolean bit = ((b >> (7 - i) & 1) == 1);
        boolean c15 = ((crc >> 15 & 1) == 1);
        crc <<= 1;
        if (c15 ^ bit) {
          crc ^= polynomial;
        }
      }
    }
    crc &= 0xFFFF;
    return crc;
  }
}
