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

package cn.universal.persistence.base;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.iot.constant.IotConstant.DeviceStatus;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.core.utils.ThreadLocalCache;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/6 11:56
 */
@Slf4j
public abstract class IotDownAdapter<T extends BaseDownRequest> implements IRule {

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Resource(name = "ioTDeviceActionBeforeService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private DeviceExtTemplate deviceExtTemplate;

  @Resource(name = "iotDownRuleService")
  private IotDownWrapper iotDownWrapper;

  @Resource private SupportMapAreasMapper supportMapAreasMapper;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  private String CUSTOM_FIELD = "customField";

  /**
   * iot_product=>{third_configuration}字段配置JSON->customField
   *
   * <p>用于[添加设备]校验data->是否包含自定义字段
   */
  protected R preDown(IoTProduct product, Object data, DownRequest downRequest) {
    deviceExtTemplate.downExt(downRequest);
    Map<String, IotDownWrapper> downWrapperMap = SpringUtil.getBeansOfType(IotDownWrapper.class);
    if (MapUtil.isNotEmpty(downWrapperMap)) {
      for (Map.Entry<String, IotDownWrapper> entry : downWrapperMap.entrySet()) {
        String serviceName = entry.getKey();
        IotDownWrapper iotDownWrapper = entry.getValue();
        R r = iotDownWrapper.beforeDownAction(product, data, downRequest);
        //        log.debug("调用全局beforeDevAdd函数,实现名称={}.返回结果={}", serviceName, r);
        if (r != null) {
          return r;
        }
      }
    }
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);
    return null;
  }

  /** 调用全局功能函数 如果没有匹配到返回null */
  protected R callGlobalFunction(IoTProduct product, IoTDevice ioTDevice, DownRequest downRequest) {
    Map<String, IotDownWrapper> downWrapperMap = SpringUtil.getBeansOfType(IotDownWrapper.class);
    if (MapUtil.isNotEmpty(downWrapperMap)) {
      for (Map.Entry<String, IotDownWrapper> entry : downWrapperMap.entrySet()) {
        String serviceName = entry.getKey();
        IotDownWrapper iotDownWrapper = entry.getValue();
        R r = iotDownWrapper.beforeFunctionOrConfigDown(product, ioTDevice, downRequest);
        //        log.debug("调用全局函数,实现名称={}.返回结果={}", serviceName, r);
        if (r != null) {
          return r;
        }
      }
    }
    return null;
  }

  /**
   * 设备后置处理
   *
   * <p>用于[添加设备]，添加data额外属性值到数据库configuration字段
   */
  protected void finalDown(
      Map<String, Object> config, IoTProduct product, DownCmd downCmd, Object data) {
    try {
      if (product != null
          && StrUtil.isNotBlank(product.getThirdConfiguration())
          && DownCmd.DEV_ADD.equals(downCmd)) {
        JSONObject jsonObject = JSONUtil.parseObj(product.getThirdConfiguration());
        JSONArray customFields = jsonObject.getJSONArray(CUSTOM_FIELD);
        if (jsonObject == null || customFields == null || customFields.size() <= 0) {
          return;
        }
        for (Object obj : customFields) {
          JSONObject object = (JSONObject) obj;
          Object fieldValue = BeanUtil.getFieldValue(data, object.getStr("id"));
          config.put(object.getStr("id"), fieldValue);
        }
      }
    } catch (Exception e) {
      log.warn("={}", e);
    }
  }

  /** 保存发送指令 */
  protected void storeCommand(String productKey, String deviceId, Object data) {
    String value =
        stringRedisTemplate
            .opsForValue()
            .get(IotConstant.REDIS_STORE_COMMAND + ":" + productKey + ":" + deviceId);
    if (StrUtil.isBlank(value)) {
      stringRedisTemplate
          .opsForValue()
          .set(
              IotConstant.REDIS_STORE_COMMAND + ":" + productKey + ":" + deviceId,
              JSONUtil.toJsonStr(Stream.of(data).collect(Collectors.toList())),
              3,
              TimeUnit.DAYS);
    } else {
      JSONArray ar = JSONUtil.parseArray(value);
      JSONArray ne = keepLatestFunction(ar, data);
      //      ar.add(data);
      stringRedisTemplate
          .opsForValue()
          .set(
              IotConstant.REDIS_STORE_COMMAND + ":" + productKey + ":" + deviceId,
              JSONUtil.toJsonStr(ne),
              3,
              TimeUnit.DAYS);
    }
  }

  /** 同一function保留最后一条指令 */
  private JSONArray keepLatestFunction(JSONArray array, Object data) {
    JSONArray result = new JSONArray();
    AtomicBoolean isOldData = new AtomicBoolean(false);
    array.forEach(
        o -> {
          String originFunction = JSONUtil.parseObj(o).getJSONObject("function").getStr("function");
          String latestFunction =
              JSONUtil.parseObj(data).getJSONObject("function").getStr("function");
          if (originFunction.equals(latestFunction)) {
            result.add(data);
            isOldData.set(true);
          } else {
            result.add(o);
          }
        });
    if (!isOldData.get()) {
      result.add(data);
    }
    return result;
  }

  protected Map<String, Object> callSaveDeviceInstance(BaseDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getData().getStr("deviceName", downRequest.getDeviceId()))
            .state(DeviceStatus.offline.getCode())
            .iotId(IdUtil.simpleUUID())
            .application(downRequest.getApplicationId())
            .creatorId(downRequest.getAppUnionId())
            .productName(downRequest.getIoTProduct().getName())
            .detail(downRequest.getDetail())
            .gwProductKey(downRequest.getGwProductKey())
            .extDeviceId(downRequest.getData().getStr("extDeviceId"))
            .productKey(downRequest.getProductKey())
            .build();

    if (StrUtil.isNotBlank(downRequest.getData().getStr("latitude"))
        && StrUtil.isNotBlank(downRequest.getData().getStr("longitude"))) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getData().getStr("longitude"),
              downRequest.getData().getStr("latitude")));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getData().getStr("longitude"), downRequest.getData().getStr("latitude"));
      if (supportMapAreas == null) {
        log.info(
            "[HTTP下行][区域查询] 查询区域id为空,lon={},lat={}",
            downRequest.getData().getStr("longitude"),
            downRequest.getData().getStr("latitude"));
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }
    Map<String, Object> config = new HashMap<>();
    finalDown(config, downRequest.getIoTProduct(), downRequest.getCmd(), downRequest.getData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    if (StrUtil.isNotBlank(ThreadLocalCache.localExtDeviceId.get())) {
      ioTDevice.setExtDeviceId(ThreadLocalCache.localExtDeviceId.get());
      ThreadLocalCache.localExtDeviceId.remove();
    }
    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    // 推送设备创建消息
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", ioTDevice.getIotId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
    return result;
  }
}
