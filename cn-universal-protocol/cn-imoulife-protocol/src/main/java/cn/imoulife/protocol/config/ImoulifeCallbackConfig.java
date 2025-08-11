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

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.imoulife.protocol.entity.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 乐橙配置 */
@Component
public class ImoulifeCallbackConfig {

  @Autowired private ImoulifeRequest imoulifeRequest;

  private String location = "https://iot.iotuniv.cn/iot/lechengEvents";

  /*
   * 绑定 乐橙事件回调地址
   * */
  //  @PostConstruct
  public void setLocation() {
    JSONObject param = new JSONObject();
    param.set("status", "on");
    param.set("callbackFlag", "alarm,deviceStatus");
    param.set("basePush", "1");
    param.set("callbackUrl", location);
    RespBody respBody = imoulifeRequest.request("/openapi/setMessageCallback", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      System.out.println("视频门锁报警回调地址成功");
    } else {
      System.out.println("视频门锁报警回调地址失败");
    }
  }
}
