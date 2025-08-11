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

package cn.imoulife.protocol.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.service.IUP;
import cn.universal.core.utils.DelayedTaskUtil;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.imoulife.protocol.config.ImoulifeRequest;
import cn.imoulife.protocol.entity.RespBody;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 物联网平台海康设备在线记录保存
 */
@Service
@Slf4j
public class LechenOnlineTask {

  @Resource
  private DelayedTaskUtil delayedTaskUtil;

  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Resource
  private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Resource
  private IoTProductMapper ioTProductMapper;

  @Autowired
  private ImoulifeRequest imoulifeRequest;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Resource(name = "lechenUPService")
  private IUP iup;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  private final String LE_CHEN = "lechen";

  /**
   * 默认保持所有相机为最新版本
   *
   * <p>每周日执行
   */
  public void updateGrade() {
    Boolean flag =
        stringRedisTemplate
            .opsForValue()
            .setIfAbsent("ScheduleTask:LechenUpdateOnline", "1", 1, TimeUnit.HOURS);
    if (!flag) {
      return;
    }
    List<String> productKeyList = new ArrayList<>();
    List<IoTProduct> products =
        ioTProductMapper.select(
            IoTProduct.builder().thirdPlatform(LE_CHEN).state(Byte.valueOf("0")).build());
    for (int i = 0; i < products.size(); i++) {
      IoTProduct ioTProduct = products.get(i);
      if (StrUtil.isNotBlank(ioTProduct.getConfiguration())) {
        JSONObject parseObj = JSONUtil.parseObj(ioTProduct.getConfiguration());
        Boolean isCamera = parseObj.getBool("isCamera");
        if (Objects.nonNull(isCamera) && isCamera) {
          productKeyList.add(ioTProduct.getProductKey());
        }
      }
    }
    if (productKeyList.size() == 0) {
      productKeyList.add("无");
    }
    List<IoTDevice> select =
        ioTDeviceMapper.selectOfflineCamera(productKeyList, null, 1, null, null);
    if (select != null && select.size() > 0) {
      for (IoTDevice s : select) {
        try {
          JSONObject param = new JSONObject();
          param.set("deviceIds", s.getDeviceId());
          param.set("channelId", 0);
          RespBody request = imoulifeRequest.request("/openapi/deviceVersionList", param);
          if (request != null && "0".equalsIgnoreCase(request.getResult().getCode())) {
            Object data = request.getData();
            JSONObject obj = new JSONObject(data);
            JSONArray ar = obj.getJSONArray("deviceVersionList");
            if (ar == null) {
              continue;
            }
            for (Object object : ar) {
              JSONObject version = (JSONObject) object;
              boolean canBeUprade = version.getBool("canBeUpgrade");
              if (canBeUprade) {
                JSONObject update = new JSONObject();
                update.set("deviceId", s.getDeviceId());
                update.set("channelId", 0);
                imoulifeRequest.request("/openapi/upgradeDevice", update);
                log.info("lecheng={},upgradeDevice", s.getDeviceId());
              }
            }
          }
        } catch (Exception e) {
          log.error("升级异常=", e);
        }
      }
    }
  }

  /**
   * 获取乐橙设备的在线信息
   */
  //  @Scheduled(cron = "0 0 0/4 * *  ? ")
  public void saveOnlineQtyTask() {
    Boolean flag =
        stringRedisTemplate.opsForValue().setIfAbsent("sch:lechenOnline", "1", 1, TimeUnit.HOURS);
    if (!flag) {
      return;
    }
    try {
      log.info("乐橙摄像头定时任务开启");
      int start = 0;
      int limit = 500;
      List<IoTDevice> ioTDeviceList;
      do {
        // 查询离线且小于指定丢弃值的乐橙设备
        // 丢弃值：摄像头连续查询第三方平台平台且摄像头状态为离线的次数值
        List<String> productKeyList = new ArrayList<>();
        List<IoTProduct> products =
            ioTProductMapper.select(
                IoTProduct.builder().thirdPlatform(LE_CHEN).state(Byte.valueOf("0")).build());
        for (int i = 0; i < products.size(); i++) {
          IoTProduct ioTProduct = products.get(i);
          if (StrUtil.isNotBlank(ioTProduct.getConfiguration())) {
            JSONObject parseObj = JSONUtil.parseObj(ioTProduct.getConfiguration());
            Boolean isCamera = parseObj.getBool("isCamera");
            if (Objects.nonNull(isCamera) && isCamera) {
              productKeyList.add(ioTProduct.getProductKey());
            }
          }
        }
        if (productKeyList.size() == 0) {
          productKeyList.add("无");
        }
        ioTDeviceList = ioTDeviceMapper.selectOfflineCamera(productKeyList, 12, 0, start, limit);
        if (CollectionUtil.isEmpty(ioTDeviceList)) {
          log.info("乐橙摄像头定时任务结束");
          return;
        }
        JSONObject param = new JSONObject();
        for (IoTDevice ioTDevice : ioTDeviceList) {
          try {
            param.set("deviceId", ioTDevice.getDeviceId());
            JSONObject result =
                JSONUtil.parseObj(imoulifeRequest.request("/openapi/deviceOnline", param));
            JSONObject jsonObject = JSONUtil.parseObj(result.get("result"));
            JSONObject jsonData = JSONUtil.parseObj(jsonObject.get("data"));
            String online = jsonData.getStr("onLine");
            if ("1".equals(online)) {
              if (!ioTDevice.getState()) {
                log.info(
                    "乐橙摄像头定时任务处理【在线】:  deviceId = {};state = {};config = {};",
                    ioTDevice.getDeviceId(),
                    ioTDevice.getState(),
                    ioTDevice.getConfiguration());
                // 触发上线事件
                ioTDeviceActionAfterService.online(ioTDevice.getProductKey(),
                    ioTDevice.getDeviceId());
                // 更新上线设备的丢弃值为0
                IoTDevice instance = new IoTDevice();
                instance.setId(ioTDevice.getId());
                instance.setConfiguration(
                    JSONUtil.parseObj(ioTDevice.getConfiguration())
                        .set("discardValue", 0)
                        .toString());
                ioTDeviceMapper.updateDevInstance(instance);
              }
            } else {
              if (ioTDevice.getState()) {
                log.info(
                    "乐橙摄像头定时任务处理【离线】: deviceId = {};state = {};config = {};",
                    ioTDevice.getDeviceId(),
                    ioTDevice.getState(),
                    ioTDevice.getConfiguration());
                // 如果本地离线设备平台查询也为离线，则更新离线设备丢弃值+1
                IoTDevice instance = new IoTDevice();
                instance.setId(ioTDevice.getId());
                JSONObject config = JSONUtil.parseObj(ioTDevice.getConfiguration());
                Integer oldValue = config.getInt("discardValue");
                int newValue = ((oldValue == null ? 0 : oldValue)) + 1;
                config.set("discardValue", newValue);
                instance.setConfiguration(config.toString());
                ioTDeviceMapper.updateDevInstance(instance);
                ioTDeviceActionAfterService.offline(
                    ioTDevice.getProductKey(), ioTDevice.getDeviceId());
              }
            }
          } catch (Exception e) {
            log.error("乐橙摄像头定时任务异常 devMsg = {} result = {}", ioTDevice.toString(),
                e.getMessage());
          }
        }
        start += limit;
      } while (ioTDeviceList.size() == limit);
      log.info("乐橙摄像头定时任务结束");
    } catch (Exception e) {
      log.error("乐橙摄像头定时任务异常 result = {}", e.getMessage());
    }
  }

  /**
   * 延迟20秒检测在线状态
   */
  public void delayCheckOnlineStatus(IoTDevice ioTDevice) {
    delayedTaskUtil.putTask(
        () -> {
          JSONObject param = new JSONObject();
          param.set("deviceId", ioTDevice.getDeviceId());
          JSONObject result =
              JSONUtil.parseObj(imoulifeRequest.request("/openapi/deviceOnline", param));
          JSONObject jsonObject = JSONUtil.parseObj(result.get("result"));
          JSONObject jsonData = JSONUtil.parseObj(jsonObject.get("data"));
          String online = jsonData.getStr("onLine");
          if ("1".equals(online)) {
            if (!ioTDevice.getState()) {
              // 触发上线事件
              ioTDeviceActionAfterService.online(ioTDevice.getProductKey(),
                  ioTDevice.getDeviceId());
              // 更新上线设备的丢弃值为0
              IoTDevice instance = new IoTDevice();
              instance.setId(ioTDevice.getId());
              instance.setConfiguration(
                  JSONUtil.parseObj(ioTDevice.getConfiguration())
                      .set("discardValue", 0)
                      .toString());
              ioTDeviceMapper.updateDevInstance(instance);
            }
          } else {
            if (ioTDevice.getState()) {
              // 如果本地离线设备平台查询也为离线，则更新离线设备丢弃值+1
              IoTDevice instance = new IoTDevice();
              instance.setId(ioTDevice.getId());
              JSONObject config = JSONUtil.parseObj(ioTDevice.getConfiguration());
              Integer oldValue = config.getInt("discardValue");
              int newValue = ((oldValue == null ? 0 : oldValue)) + 1;
              config.set("discardValue", newValue);
              instance.setConfiguration(config.toString());
              ioTDeviceMapper.updateDevInstance(instance);
              ioTDeviceActionAfterService.offline(ioTDevice.getProductKey(),
                  ioTDevice.getDeviceId());
            }
          }
        },
        20,
        TimeUnit.SECONDS);
  }

  /**
   * 无延迟检测在线状态
   */
  public void checkOnlineStatus(IoTDevice ioTDevice) {
    delayedTaskUtil.putTask(
        () -> {
          JSONObject param = new JSONObject();
          param.set("deviceId", ioTDevice.getDeviceId());
          JSONObject result =
              JSONUtil.parseObj(imoulifeRequest.request("/openapi/deviceOnline", param));
          JSONObject jsonObject = JSONUtil.parseObj(result.get("result"));
          JSONObject jsonData = JSONUtil.parseObj(jsonObject.get("data"));
          String online = jsonData.getStr("onLine");
          if ("1".equals(online)) {
            if (!ioTDevice.getState()) {
              // 触发上线事件
              ioTDeviceActionAfterService.online(ioTDevice.getProductKey(),
                  ioTDevice.getDeviceId());
              // 更新上线设备的丢弃值为0
              IoTDevice instance = new IoTDevice();
              instance.setId(ioTDevice.getId());
              instance.setConfiguration(
                  JSONUtil.parseObj(ioTDevice.getConfiguration())
                      .set("discardValue", 0)
                      .toString());
              ioTDeviceMapper.updateDevInstance(instance);
            }
          } else {
            if (ioTDevice.getState()) {
              // 如果本地离线设备平台查询也为离线，则更新离线设备丢弃值+1
              IoTDevice instance = new IoTDevice();
              instance.setId(ioTDevice.getId());
              JSONObject config = JSONUtil.parseObj(ioTDevice.getConfiguration());
              Integer oldValue = config.getInt("discardValue");
              int newValue = ((oldValue == null ? 0 : oldValue)) + 1;
              config.set("discardValue", newValue);
              instance.setConfiguration(config.toString());
              ioTDeviceMapper.updateDevInstance(instance);
              ioTDeviceActionAfterService.offline(ioTDevice.getProductKey(),
                  ioTDevice.getDeviceId());
            }
          }
        },
        0,
        TimeUnit.SECONDS);
  }
}
