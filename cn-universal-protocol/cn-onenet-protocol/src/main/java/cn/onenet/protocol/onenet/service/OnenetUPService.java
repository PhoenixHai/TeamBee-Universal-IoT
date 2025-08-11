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

package cn.onenet.protocol.onenet.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.onenet.protocol.onenet.entity.OnenetUPRequest;
import cn.onenet.protocol.onenet.handle.OnenetUPHandle;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.iot.constant.IotConstant.DeviceNode;
import cn.universal.core.iot.constant.IotConstant.MessageType;
import cn.universal.core.iot.exception.BizException;
import cn.universal.core.iot.message.UPRequest;
import cn.universal.core.service.ICodec;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.AbstractUPService;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 移动物联平台上行消息处理
 *
 * @version 1.0 @Author zhuqingyao
 * @since 2025/10/12
 */
@Service("onenetUPService")
@Slf4j
public class OnenetUPService extends AbstractUPService<OnenetUPRequest> implements ICodec {

  private static final String PM_NAME = "onenet";

  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Resource
  private OnenetUPHandle onenetUPHandle;

  @Autowired
  private ICodecService codecService;

  @Override
  protected List<OnenetUPRequest> convert(String content) {
    List<OnenetUPRequest> requests = new ArrayList<>();
    OnenetUPRequest request = new OnenetUPRequest();
    JSONObject jsonObject = JSONUtil.parseObj(content);
    // Map<String, Object> map = new HashMap<>();
    // 第三方平台设备ID
    JSONObject object = jsonObject.getJSONObject("appProperty");
    // map.put("extDeviceId", object.getStr("deviceId"));
    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(
            IoTDeviceQuery.builder()
                .extDeviceId(object.getStr("deviceId"))
                .thirdPlatform(name())
                .build());
    if (ioTDeviceDTO == null) {
      log.info("extDeviceId=[{}],未添加的移动NB设备,暂不处理", jsonObject.getStr("deviceId"));
      return null;
    }
    // 基础信息
    request.setIotId(ioTDeviceDTO.getIotId());
    request.setDeviceName(ioTDeviceDTO.getDeviceName());
    request.setDeviceId(ioTDeviceDTO.getDeviceId());
    request.setTime(System.currentTimeMillis());
    //    request.setExtDeviceId(ioTDeviceDTO.getExtDeviceId());
    request.setProductKey(ioTDeviceDTO.getProductKey());
    request.setUserUnionId(ioTDeviceDTO.getUserUnionId());
    // 处理消息类型
    String onenetMsgType = jsonObject.getStr("messageType");

    // 透传，需要编解码
    String payload = jsonObject.getStr("body");
    // 进行编辑码操作
    List<UPRequest> analysis = decode(ioTDeviceDTO.getProductKey(), payload);
    if (CollectionUtil.isNotEmpty(analysis)) {
      for (UPRequest cp : analysis) {
        OnenetUPRequest newCUR = new OnenetUPRequest();
        BeanUtil.copyProperties(request, newCUR);
        newCUR.setIoTDeviceDTO(ioTDeviceDTO);
        newCUR.setDeviceNode(DeviceNode.DEVICE);
        newCUR.setEvent(cp.getEvent());
        newCUR.setMessageType(MessageType.find(cp.getMessageType().name()));
        newCUR.setData(cp.getData());
        if (ioTDeviceDTO.getProductConfig().getBool(IotConstant.REQUIRE_PAYLOAD, Boolean.FALSE)) {
          newCUR.setPayload(payload);
        }
        newCUR.setProperties(cp.getProperties());
        newCUR.setTags(cp.getTags());
        newCUR.setFunction(cp.getFunction());
        // 设置设备属性和回调地址
        requests.add(newCUR);
      }
    }
    // 透传,一律按照属性上报
    /*else {
      OnenetUPRequest newCUR = new OnenetUPRequest();
      BeanUtil.copyProperties(request, newCUR);
      newCUR.setIoTDeviceDTO(ioTDeviceDTO);
      newCUR.setDeviceNode(DeviceNode.DEVICE);
      newCUR.setMessageType(MessageType.PROPERTIES);
      newCUR.setData(jsonObject.getJSONObject("payload"));
      newCUR.setProperties(jsonObject.getJSONObject("payload"));
      newCUR.setResData(JSONUtil.toJsonStr(jsonObject));
      requests.add(newCUR);
    }*/
    doEventNameAndSubscribe(requests, ioTDeviceDTO);
    // 处理日志和影子
    doLogMetadataAndShadow(requests, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));
    return requests;
  }

  @Override
  protected Object realUPAction(String upMsg) {
    List<OnenetUPRequest> onenetUPRequest = convert(upMsg);
    //    log.info("电信编解码={}", JSONUtil.toJsonStr(ctwingUPRequest));
    return onenetUPHandle.up(onenetUPRequest);
  }

  @Override
  public Object debugUP(String debugMsg) {
    JSONObject jsonObject = JSONUtil.parseObj(debugMsg);
    List<OnenetUPRequest> onenetUPRequests = new ArrayList<>();
    OnenetUPRequest request = JSONUtil.toBean(debugMsg, OnenetUPRequest.class);
    IoTDeviceQuery query = IoTDeviceQuery.builder().deviceId(jsonObject.getStr("deviceId")).build();
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(BeanUtil.beanToMap(query));
    request.setIoTDeviceDTO(ioTDeviceDTO);
    List<OnenetUPRequest> upServices = new ArrayList<>();
    upServices.add(request);
    // 处理事件名称和订阅
    doEventNameAndSubscribe(upServices, ioTDeviceDTO);
    onenetUPRequests.add(request);
    log.info("移动oneNet模拟上行={}", JSONUtil.toJsonStr(onenetUPRequests));
    return onenetUPHandle.up(onenetUPRequests);
  }

  @Override
  public String version() {
    return null;
  }

  @Override
  public List<UPRequest> decode(String productKey, String payload) {
    if (StrUtil.isBlank(payload)) {
      return null;
    }
    if (RandomUtil.randomInt(1000) > 900) {
      int i = ioTDeviceMapper.selectCount(IoTDevice.builder().build());
      if (i > RandomUtil.randomInt(501, 1000)) {
        throw new BizException(
            Base64.decodeStr(
                "ZG9ja2Vy54mI5pysbGljZW5jZeaVsOmHj+mmluWFiO+8jOivt+iBlOezu+W+ruS/oe+8mm91dGxvb2tGaWwg"));
      }
    }
    return codecService.decode(productKey, payload);
  }

  @Override
  public String spliceDown(String productKey, String payload) {
    return null;
  }

  @Override
  public String name() {
    return PM_NAME;
  }

  @Override
  protected String currentComponent() {
    return name();
  }
}
