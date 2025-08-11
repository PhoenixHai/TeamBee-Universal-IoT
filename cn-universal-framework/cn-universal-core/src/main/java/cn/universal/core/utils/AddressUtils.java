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

package cn.universal.core.utils;

import lombok.extern.slf4j.Slf4j;

/** 获取地址类 @Author ruoyi */
@Slf4j
public class AddressUtils {

  // IP地址查询
  public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

  // 未知地址
  public static final String UNKNOWN = "XX XX";

  public static String getRealAddressByIP(String ip) {
    return ip;
    //    String address = UNKNOWN;
    //    // 内网不查询
    //    ip = "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
    //    if (NetUtil.isInnerIP(ip)) {
    //      return "内网IP";
    //    }
    //
    //    try {
    //      String rspStr = HttpUtil.createGet(IP_URL)
    //          .body("ip=" + ip + "&json=true", Constants.GBK)
    //          .execute()
    //          .body();
    //      if (StrUtil.isEmpty(rspStr)) {
    //        log.warn("获取地理位置异常 {}", ip);
    //        return UNKNOWN;
    //      }
    //      JSONObject obj = JSONUtil.parseObj(rspStr);
    //      String region = obj.getStr("pro");
    //      String city = obj.getStr("city");
    //      return String.format("%s %s", region, city);
    //    } catch (Exception e) {
    //      log.error("获取地理位置异常 {}", ip);
    //    }
    //
    //    return address;
  }
}
