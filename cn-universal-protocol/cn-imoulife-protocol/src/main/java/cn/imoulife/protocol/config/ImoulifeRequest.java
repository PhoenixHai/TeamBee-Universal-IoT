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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.iot.exception.BizException;
import cn.imoulife.protocol.entity.RespBody;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 乐橙请求工具
 */
@Slf4j
@Component
public class ImoulifeRequest {

  public static final String EXPIRE_CODE = "TK1002";

  @Resource
  private ImoulifeConfig imoulifeConfig;

  private volatile ImoulifeToken accessToken = new ImoulifeToken();
  private volatile ImoulifeToken kitToken = new ImoulifeToken();

  private static ExecutorService executor =
      new ThreadPoolExecutor(
          4,
          8,
          1000,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(),
          new ThreadFactoryBuilder().setNameFormat("imoulife-request-%d").build(),
          new ThreadPoolExecutor.AbortPolicy());

  public RespBody request(String url, JSONObject params) {
    JSONObject jsonObject = imoulifeConfig.refreshParams();

    if (params != null) {
      params.set("token", getAccessToken());
      jsonObject.set("params", params);
    }
    log.info("乐橙摄像头请求参数：" + jsonObject);
    HttpRequest request = new HttpRequest(imoulifeConfig.getHost() + url).method(Method.POST);
    request.body(jsonObject.toString());

    HttpResponse httpResponse = request.execute();
    log.info("乐橙摄像头回复：" + httpResponse);
    if (httpResponse.isOk()) {
      RespBody data = JSONUtil.toBean(httpResponse.body(), RespBody.class);
      return data;
    } else {
      throw new BizException("请求乐橙异常");
    }
  }

  public void requestDownload(JSONObject params) {
    log.info("乐橙摄像头请求参数：" + params);
    String token = getAccessToken();
    String deviceId = (String) params.get("deviceId");
    String filePath = (String) params.get("filePath");
    String psk = (String) params.get("psk");
    String recordRegionId = (String) params.get("recordRegionId");
    int type = Integer.parseInt(params.get("type").toString());
    int index = Integer.parseInt(params.get("index").toString());
    int channelId = Integer.parseInt(params.get("channelId").toString());
    //    int i = LCOpenSdkSingleton.getInstance().startDownloadCloudRecord(index, token, deviceId,
    // channelId, filePath,
    //    recordRegionId, psk, recordRegionId, type);
    //    log.info("下载录像结果是： " + i);
  }

  public String getAccessToken() {
    // 判断当前token是否在有效期内
    if (accessToken.isExpire()) {
      requestAccessToken(false);
    }
    return accessToken == null ? null : accessToken.getAccessToken();
  }

  public String getKitToken(String deviceId) {
    // 判断当前token是否在有效期内
    requestKitToken(true, deviceId);
    //    if (kitToken.isKitExpire()) {
    //
    //    }
    return kitToken == null ? null : kitToken.getKitToken();
  }

  private ImoulifeToken accessToken() {
    JSONObject jsonObject = imoulifeConfig.refreshParams();
    HttpRequest request =
        new HttpRequest(imoulifeConfig.getHost() + "/openapi/accessToken").method(Method.POST);
    request.body(jsonObject.toString());
    HttpResponse httpResponse = request.execute();
    if (httpResponse.isOk()) {
      RespBody data = JSONUtil.toBean(httpResponse.body(), RespBody.class);
      ImoulifeToken token = BeanUtil.copyProperties(data.getData(), ImoulifeToken.class);
      if (EXPIRE_CODE.equals(data.errCode())) {
        return accessToken();
      }
      System.out.println(token.isExpire());
      token.setExpireTime(token.getExpireTime());
      if (token != null && !token.isExpire()) {
        return token;
      }
    }
    return null;
  }

  private ImoulifeToken kitToken(String deviceId) {
    JSONObject object = new JSONObject();
    object.set("token", getAccessToken());
    object.set("deviceId", deviceId);
    object.set("channelId", 0);
    object.set("type", 0);
    JSONObject jsonObject = imoulifeConfig.refreshParams();
    jsonObject.set("params", object);
    HttpRequest request =
        new HttpRequest(imoulifeConfig.getHost() + "/openapi/getKitToken").method(Method.POST);
    request.body(jsonObject.toString());
    HttpResponse httpResponse = request.execute();
    if (httpResponse.isOk()) {
      RespBody data = JSONUtil.toBean(httpResponse.body(), RespBody.class);
      ImoulifeToken token = BeanUtil.copyProperties(data.getData(), ImoulifeToken.class);
      if (EXPIRE_CODE.equals(data.errCode())) {
        return kitToken(deviceId);
      }
      if (token != null && !token.isKitExpire()) {
        return token;
      }
    }
    return null;
  }

  private synchronized void requestAccessToken(boolean forceRefresh) {
    if (accessToken.isExpire() || forceRefresh) {
      accessToken = accessToken();
      log.info("access_token获取成功，结果: {}", accessToken);
    } else {
      log.error("access_token获取失败，参数: {}", accessToken);
    }
  }

  private synchronized void requestKitToken(boolean forceRefresh, String deviceId) {
    if (kitToken.isExpire() || forceRefresh) {
      kitToken = kitToken(deviceId);
      log.info("kit_token获取成功，结果: {}", kitToken);
    } else {
      log.error("kit_token获取失败，参数: {}", kitToken);
    }
  }
}
