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

package cn.imoulife.protocol.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.imoulife.protocol.config.ImouLifeModuleInfo;
import cn.imoulife.protocol.entity.ImoulifeUPRequest;
import cn.imoulife.protocol.handle.ImoulifeUPHandle;
import cn.universal.core.iot.constant.IotConstant.MessageType;
import cn.universal.core.service.ICodec;
import cn.universal.dm.device.service.AbstractUPService;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * hik
 *
 * @version 1.0
 * @since 2025/8/24 11:32
 */
@Service("lechenUPService")
@Slf4j
public class ImoulifeUPService extends AbstractUPService<ImoulifeUPRequest> implements ICodec {

  @Resource
  private ImouLifeModuleInfo imouLifeModuleInfo;
  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Resource
  private ImoulifeUPHandle imoulifeUPHandle;
  @Resource
  private IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Override
  protected List<ImoulifeUPRequest> convert(String content) {
    List<ImoulifeUPRequest> requests = new ArrayList<>();
    ImoulifeUPRequest request = new ImoulifeUPRequest();
    JSONObject jsonObject = JSONUtil.parseObj(content);
    Map<String, Object> map = new HashMap<>();
    String did = "";
    String deviceId = jsonObject.getStr("deviceId");
    String did2 = jsonObject.getStr("did");
    if (!"".equals(deviceId) && null != deviceId) {
      did = deviceId;
    } else if (!"".equals(did2) && null != did2) {
      did = did2;
    }

    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(IoTDeviceQuery.builder().deviceId(did).thirdPlatform(name()).build());
    if (ioTDeviceDTO == null) {
      log.info("deviceId=[{}],未添加的乐橙设备,暂不处理", jsonObject.getStr("deviceId"));
      return null;
    }
    // 基础信息
    request.setIotId(ioTDeviceDTO.getIotId());
    request.setDeviceName(ioTDeviceDTO.getDeviceName());
    request.setDeviceId(ioTDeviceDTO.getDeviceId());
    request.setTime(System.currentTimeMillis());
    request.setProductKey(ioTDeviceDTO.getProductKey());
    request.setUserUnionId(ioTDeviceDTO.getUserUnionId());
    // 处理消息类型
    String evenType = jsonObject.getStr("msgType");

    // 数据上报，需要编解码，进行编辑码操作

    /*
     * yk---------
     * */
    if (StrUtil.isNotBlank(evenType)) {
      // 进行编辑码操作
      ImoulifeUPRequest newCUR = new ImoulifeUPRequest();
      BeanUtil.copyProperties(request, newCUR);
      newCUR.setIoTDeviceDTO(ioTDeviceDTO);

      newCUR.setEvent(evenType);
      newCUR.setMessageType(MessageType.EVENT);
      // 设置事件元数据
      map.clear();
      map.put("deviceId", did);

      String cid = jsonObject.getStr("cid");
      if (StrUtil.isNotBlank(cid)) {
        map.put("cid", cid);
      }
      String time = jsonObject.getStr("time");
      if (StrUtil.isNotBlank(time)) {
        if (time.contains("T")) {
          time =
              time.substring(0, 4)
                  + "-"
                  + time.substring(4, 6)
                  + "-"
                  + time.substring(6, 8)
                  + " "
                  + time.substring(9, 11)
                  + ":"
                  + time.substring(11, 13)
                  + ":"
                  + time.substring(13, 15);
          map.put("time", time);
        } else {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          String format = sdf.format(new Date(Long.valueOf(time + "000")));
          map.put("time", format);
        }
      }
      String cname = jsonObject.getStr("cname");
      if (StrUtil.isNotBlank(cname)) {
        map.put("reasonDes", cname);
      }
      String desc = jsonObject.getStr("desc");
      if (StrUtil.isNotBlank(desc)) {
        JSONObject descObj = JSONUtil.parseObj(desc);
        if ("electricity".equals(evenType)) {
          map.put("electric", descObj.getStr("electric"));
          map.put("alkElec", descObj.getStr("alkElec"));
          map.put("type", descObj.getStr("type"));
          map.put("battery", descObj.getStr("battery"));
        } else if ("beOpenedDoor".equals(evenType)) {
          map.put("name", descObj.getStr("name"));
          map.put("keyId", descObj.getStr("keyId"));
          map.put("type", descObj.getStr("type"));
        }

        map.put("desc", desc);
      }

      newCUR.setData(map);
      switch (newCUR.getEvent()) {
        case "human":
          newCUR.setEventName("人形检测报警");
          break;
        case "storageRecoverOk":
          newCUR.setEventName("SD卡格式化成功");
          break;
        case "storageRecoverFail":
          newCUR.setEventName("SD卡格式化失败");
          break;
        case "storageAbnormal":
          newCUR.setEventName("SD卡异常");
          break;
        case "upgradeFail":
          newCUR.setEventName("设备升级失败");
          break;
        case "abAlarmSound":
          newCUR.setEventName("异常音报警");
          break;
        case "openCamera":
          newCUR.setEventName("关闭遮罩");
          break;
        case "storageEmpty":
          newCUR.setEventName("无SD卡");
          break;
        case "changeDevName":
          newCUR.setEventName("修改设备名称");
          break;
        case "online":
          ioTDeviceActionAfterService.online(
              ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getDeviceId());
          break;
        case "offline":
          ioTDeviceActionAfterService.offline(
              ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getDeviceId());
          break;
        default:
          newCUR.setEventName("未匹配到对应告警");
      }

      // 设置设备属性和回调地址
      requests.add(newCUR);
      // 处理日志和影子
      doLogMetadataAndShadow(requests, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));
      // 处理事件名称和订阅
      doEventNameAndSubscribe(requests, ioTDeviceDTO);

      return requests;
    }

    /*
     * yk end ----------
     * */

    ImoulifeUPRequest newCUR = new ImoulifeUPRequest();
    BeanUtil.copyProperties(request, newCUR);
    newCUR.setIoTDeviceDTO(ioTDeviceDTO);
    newCUR.setMessageType(MessageType.PROPERTIES);

    // 设置事件元数据
    map.clear();
    map.put("deviceId", did);
    map.put("onLine", jsonObject.getStr("onLine"));
    newCUR.setProperties(map);

    // 设置设备属性和回调地址
    requests.add(newCUR);
    // 处理日志和影子
    doLogMetadataAndShadow(requests, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));

    doEventNameAndSubscribe(requests, ioTDeviceDTO);
    return requests;
  }

  @Override
  protected Object realUPAction(String upMsg) {
    List<ImoulifeUPRequest> imoulifeUPRequests = convert(upMsg);
    log.info("乐橙解码={}", JSONUtil.toJsonStr(imoulifeUPRequests));
    return imoulifeUPHandle.up(imoulifeUPRequests);
  }

  @Override
  public void debugAsyncUP(String debugMsg) {
    JSONObject jsonObject = JSONUtil.parseObj(debugMsg);
    List<ImoulifeUPRequest> imoulifeUPRequests = new ArrayList<>();
    if ("hex".equals(jsonObject.getStr("type"))) {
      jsonObject.set("deviceId", jsonObject.getStr("deviceId"));
      imoulifeUPRequests = convert(jsonObject.toString());
    } else {
      ImoulifeUPRequest request = JSONUtil.toBean(debugMsg, ImoulifeUPRequest.class);
      IoTDeviceQuery query =
          IoTDeviceQuery.builder().deviceId(jsonObject.getStr("deviceId")).build();
      IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(BeanUtil.beanToMap(query));
      request.setIoTDeviceDTO(ioTDeviceDTO);
      List<ImoulifeUPRequest> upServices = new ArrayList<>();
      upServices.add(request);
      // 处理事件名称和订阅
      doEventNameAndSubscribe(upServices, ioTDeviceDTO);
      imoulifeUPRequests.add(request);
    }
    log.info("乐橙模拟上行={}", JSONUtil.toJsonStr(imoulifeUPRequests));
    imoulifeUPHandle.up(imoulifeUPRequests);
  }

  @Override
  public String version() {
    return null;
  }

  @Override
  public String name() {
    return imouLifeModuleInfo.getCode();
  }

  @Override
  protected String currentComponent() {
    return name();
  }
}
