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

package cn.ctaiot.protocol.service;

import cn.ctaiot.protocol.config.CTAIoTConstant.CTAIoTMessageType;
import cn.ctaiot.protocol.config.CTAIoTConstant.CommandRespons;
import cn.ctaiot.protocol.config.CTAIoTModuleInfo;
import cn.ctaiot.protocol.entity.CTAIoTDownRequest;
import cn.ctaiot.protocol.entity.CTAIoTUPRequest;
import cn.ctaiot.protocol.handle.CTAIoTUPHandle;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.iot.constant.IotConstant.DeviceNode;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.iot.constant.IotConstant.MessageType;
import cn.universal.core.iot.exception.BizException;
import cn.universal.core.iot.message.UPRequest;
import cn.universal.core.service.ICodec;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.AbstractUPService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 电信物联平台上行消息处理
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 11:19
 */
@Service("ctaIoTUPService")
@Slf4j
public class CTAIoTUPService extends AbstractUPService<CTAIoTUPRequest> implements ICodec {

  @Resource
  private CTAIoTModuleInfo ctaIoTModuleInfo;

  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource
  private CTAIoTUPHandle ctAIoTUPHandle;

  @Resource
  private CTAIoTDownService ctAIoTDownService;

  @Autowired
  private ICodecService codecService;

  /**
   * https://www.ctwing.cn/dyts/105
   *
   * @param content 电信原始内容
   */
  @Override
  protected List<CTAIoTUPRequest> convert(String content) {
    List<CTAIoTUPRequest> requests = new ArrayList<>();
    JSONObject jsonObject = JSONUtil.parseObj(content);
    // Map<String, Object> map = new HashMap<>();
    // 第三方平台设备ID
    // map.put("extDeviceId", jsonObject.getStr("deviceId"));
    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(
            IoTDeviceQuery.builder()
                .extDeviceId(jsonObject.getStr("deviceId"))
                .thirdPlatform(name())
                .build());
    if (ioTDeviceDTO == null) {
      log.warn("[CT-AIoT上行][设备未匹配] deviceId={} content={}", jsonObject.getStr("deviceId"),
          content);
      return null;
    }
    // 如果是透传
    if (Boolean.TRUE.equals(
        ioTDeviceDTO.getProductConfig().getBool(IotConstant.DATA_PASS_THROUGH))) {
      // 设备影子
      doLogMetadataAndShadow(requests, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));
      return passThrough(requests, ioTDeviceDTO, jsonObject);
    }
    // 处理消息类型
    String ctMsgType = jsonObject.getStr("messageType");
    // 非透传，需要编解码
    switch (CTAIoTMessageType.valueOf(ctMsgType)) {
      // 上下线事件
      case deviceOnlineOfflineReport:
        requests = onOffLineReport(requests, ioTDeviceDTO, jsonObject);
        break;
      // 设备数据变化
      case dataReport:
        requests = dataReport(requests, ioTDeviceDTO, jsonObject);
        break;
      // 事件内容
      case eventReport:
        requests = eventReport(requests, ioTDeviceDTO, jsonObject);
        break;
      // 设备命令响应
      case commandResponse:
        requests = commandResponse(requests, ioTDeviceDTO, jsonObject);
        break;
      // TUP合并数据变化
      case dataReportTupUnion:
        // 忽略合并数据变化，避免重复业务处理
        requests = null;
        log.info("[CT-AIoT上行][TUP合并数据变化] dataReportTupUnion, requests=null, content={}",
            content);
        break;
      default:
        log.warn("[CT-AIoT上行][未匹配数据] content={}", content);
    }
    // 处理事件名称和订阅
    doEventNameAndSubscribe(requests, ioTDeviceDTO);
    // 设备影子
    doLogMetadataAndShadow(requests, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));
    // 处理电信设备回复
    reply(requests);
    //    处理电信iccid
    // doSaveNbIccid(requests,ioTDeviceDTO);
    return requests;
  }

  //
  private List<CTAIoTUPRequest> reply(List<CTAIoTUPRequest> requests) {
    if (CollectionUtil.isNotEmpty(requests)) {
      requests.stream()
          .filter(s -> ObjectUtil.isNotNull(s.getReplyPayload()))
          .map(
              s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("messageType", "FUNCTIONS");
                map.put("function", "reply");
                CTAIoTDownRequest down = new CTAIoTDownRequest();
                down.setCmd(DownCmd.DEV_FUNCTION);
                IoTProduct ioTProduct = getProduct(s.getProductKey());
                down.getCtwingRequestData()
                    .setConfiguration(JSONUtil.parseObj(ioTProduct.getConfiguration()));
                down.setProductKey(s.getProductKey());
                down.setIoTProduct(ioTProduct);
                down.setDeviceId(s.getDeviceId());
                down.setFunction(map);
                String replyPayload = s.getReplyPayload();
                // 是对象，通常是有profile的电信标准物模型
                if (JSONUtil.isTypeJSON(replyPayload)) {
                  Map<String, Object> function = JSONUtil.parseObj(replyPayload);
                  down.setFunction(function);
                  // 16进制
                } else {
                  down.setPayload(s.getReplyPayload());
                }
                return down;
              })
          .forEach(
              d -> {
                ctAIoTDownService.down(d);
              });
    }
    return requests;
  }

  private void buildRequests(
      List<CTAIoTUPRequest> requests,
      IoTDeviceDTO ioTDeviceDTO,
      String payload,
      List<CTAIoTUPRequest> analysis,
      JSONObject json) {
    if (CollectionUtil.isNotEmpty(analysis)) {
      for (CTAIoTUPRequest cp : analysis) {
        CTAIoTUPRequest newCUR = new CTAIoTUPRequest();
        // 基础信息
        newCUR.setIotId(ioTDeviceDTO.getIotId());
        newCUR.setDeviceName(ioTDeviceDTO.getDeviceName());
        newCUR.setDeviceId(ioTDeviceDTO.getDeviceId());
        newCUR.setTime(System.currentTimeMillis());
        newCUR.setProductKey(ioTDeviceDTO.getProductKey());
        newCUR.setUserUnionId(ioTDeviceDTO.getUserUnionId());
        newCUR.setDeviceNode(DeviceNode.DEVICE);
        newCUR.setIoTDeviceDTO(ioTDeviceDTO);
        newCUR.setEvent(cp.getEvent());
        newCUR.setReplyPayload(cp.getReplyPayload());
        newCUR.setMessageType(MessageType.find(cp.getMessageType().name()));
        newCUR.setData(cp.getData());
        if (ioTDeviceDTO.getProductConfig().getBool(IotConstant.REQUIRE_PAYLOAD, Boolean.FALSE)) {
          newCUR.setPayload(payload);
        }
        newCUR.setProperties(cp.getProperties());
        newCUR.setTags(cp.getTags());
        newCUR.setFunction(cp.getFunction());
        newCUR.setDebug("true".equals(json.getStr("debug")));
        // 设置设备属性和回调地址
        requests.add(newCUR);
      }
    }
  }

  /**
   * TUP合并数据变化
   */
  private List<CTAIoTUPRequest> dataReportTupUnion(
      List<CTAIoTUPRequest> requests, IoTDeviceDTO ioTDeviceDTO, JSONObject json) {
    Object payload = json.get("payload");
    List<CTAIoTUPRequest> analysis =
        codecService.decode(
            ioTDeviceDTO.getProductKey(), JSONUtil.toJsonStr(payload), CTAIoTUPRequest.class);
    buildRequests(requests, ioTDeviceDTO, JSONUtil.toJsonStr(payload), analysis, json);
    return requests;
  }

  /**
   * 设备命令响应
   */
  private List<CTAIoTUPRequest> commandResponse(
      List<CTAIoTUPRequest> requests, IoTDeviceDTO ioTDeviceDTO, JSONObject json) {
    String commandId = json.getStr("taskId");
    JSONObject result = json.getJSONObject("result");
    if (result != null) {
      String resultCode = result.getStr("resultCode");
      String resultDetail = result.getStr("resultDetail");
      log.info(
          "ctaiot 指令回复,imei={},taskId={},指令回复={}", ioTDeviceDTO.getDeviceId(), commandId,
          result);
      if (CommandRespons.DELIVERED.name().equals(resultCode)
          || CommandRespons.SUCCESSFUL.name().equals(resultCode)) {
        ioTDeviceLifeCycle.commandResp(ioTDeviceDTO, commandId, result);
      }
    }
    return null;
  }

  /**
   * 数据上报
   */
  private List<CTAIoTUPRequest> dataReport(
      List<CTAIoTUPRequest> requests, IoTDeviceDTO ioTDeviceDTO, JSONObject json) {
    Object payload = json.get("payload");
    List<CTAIoTUPRequest> analysis =
        codecService.decode(
            ioTDeviceDTO.getProductKey(), JSONUtil.toJsonStr(payload), CTAIoTUPRequest.class);
    buildRequests(requests, ioTDeviceDTO, JSONUtil.toJsonStr(payload), analysis, json);
    return requests;
  }

  /**
   * 事件上报
   */
  private List<CTAIoTUPRequest> eventReport(
      List<CTAIoTUPRequest> requests, IoTDeviceDTO ioTDeviceDTO, JSONObject json) {
    Object payload = json.get("eventContent");
    List<CTAIoTUPRequest> analysis =
        codecService.decode(
            ioTDeviceDTO.getProductKey(), JSONUtil.toJsonStr(payload), CTAIoTUPRequest.class);
    buildRequests(requests, ioTDeviceDTO, JSONUtil.toJsonStr(payload), analysis, json);
    return requests;
  }

  /**
   * 设备上下线。2023-1-20 使用设备生命周期变动
   */
  private List<CTAIoTUPRequest> onOffLineReport(
      List<CTAIoTUPRequest> rts, IoTDeviceDTO dib, JSONObject json) {
    return null;
  }

  private List<CTAIoTUPRequest> passThrough(
      List<CTAIoTUPRequest> requests, IoTDeviceDTO ioTDeviceDTO, JSONObject json) {
    CTAIoTUPRequest newCUR = new CTAIoTUPRequest();
    // 基础信息
    newCUR.setIotId(ioTDeviceDTO.getIotId());
    newCUR.setDeviceName(ioTDeviceDTO.getDeviceName());
    newCUR.setDeviceId(ioTDeviceDTO.getDeviceId());
    newCUR.setTime(System.currentTimeMillis());
    newCUR.setProductKey(ioTDeviceDTO.getProductKey());
    newCUR.setUserUnionId(ioTDeviceDTO.getUserUnionId());
    newCUR.setMessageType(MessageType.EVENT);
    newCUR.setIoTDeviceDTO(ioTDeviceDTO);
    newCUR.setDeviceNode(DeviceNode.DEVICE);
    newCUR.setMessageType(MessageType.PROPERTIES);
    newCUR.setProperties(json.getJSONObject("payload"));
    requests.add(newCUR);
    return requests;
  }

  @Override
  protected String realUPAction(String upMsg) {
    return ctAIoTUPHandle.up(convert(upMsg));
  }

  @Override
  public void debugAsyncUP(String debugMsg) {
    JSONObject jsonObject = JSONUtil.parseObj(debugMsg);
    List<CTAIoTUPRequest> CTAIoTUPRequests = new ArrayList<>();
    if ("hex".equals(jsonObject.getStr("type"))) {
      Map<String, String> map = new HashMap<>();
      String payload = "";
      try {
        payload = Base64Encoder.encode(jsonObject.getStr("payload"));
      } catch (Exception e) {
        log.error("base64 编码失败", e);
        throw new BizException("base64 编码失败,请仔细检查");
      }
      IoTDevice dev = new IoTDevice();
      dev.setDeviceId(jsonObject.getStr("deviceId"));
      IoTDevice ioTDevice = ioTDeviceMapper.selectOne(dev);
      map.put("APPdata", payload);
      jsonObject.set("debug", true);
      jsonObject.set("messageType", "dataReport");
      jsonObject.set("payload", map);
      jsonObject.set("deviceId", ioTDevice.getExtDeviceId());
      CTAIoTUPRequests = convert(jsonObject.toString());
    } else {
      CTAIoTUPRequest request = JSONUtil.toBean(debugMsg, CTAIoTUPRequest.class);
      IoTDeviceQuery query =
          IoTDeviceQuery.builder().deviceId(jsonObject.getStr("deviceId")).build();
      IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(BeanUtil.beanToMap(query));
      request.setIoTDeviceDTO(ioTDeviceDTO);
      List<CTAIoTUPRequest> upServices = new ArrayList<>();
      upServices.add(request);
      // 处理事件名称和订阅
      doEventNameAndSubscribe(upServices, ioTDeviceDTO);
      CTAIoTUPRequests.add(request);
    }
    log.info("电信模拟上行={}", JSONUtil.toJsonStr(CTAIoTUPRequests));
    ctAIoTUPHandle.up(CTAIoTUPRequests);
  }

  //  /**
  //   * nb设备存储iccid
  //   *
  //   * @param upRequests
  //   * @param ioTDeviceDTO
  //   */
  //  protected void doSaveNbIccid(List<? extends UPRequest> upRequests,
  //      IoTDeviceDTO ioTDeviceDTO) {
  //    JSONObject configuration = JSONUtil.parseObj(ioTDeviceDTO.getDevConfiguration());
  //    String icc = configuration.getStr("iccid");
  //    String iccidLast = null;
  //    for (UPRequest ur : upRequests
  //    ) {
  //      JSONObject object = JSONUtil.parseObj(ur);
  //      JSONObject properties = JSONUtil.parseObj(object.getStr("properties"));
  //      String ICCID = properties.getStr("ICCID");
  //      String iccid = properties.getStr("iccid");
  //      if(ICCID!=null && !ICCID.equals(icc)){
  //        iccidLast=ICCID;
  //      }else if(iccid!=null && !iccid.equals(icc)){
  //        iccidLast=iccid;
  //      }
  //      configuration.set("iccid",iccidLast);
  //      Map<String,String> map = new HashMap<>();
  //      map.put("deviceId",ioTDeviceDTO.getDeviceId());
  //      map.put("configuration",configuration.toString());
  //      ioTDeviceMapper.updateDevConfiguration(map);
  //    }
  //  }

  @Override
  public String version() {
    return "1.0";
  }

  @Override
  public List<UPRequest> decode(String productKey, String payload) {
    if (StrUtil.isBlank(payload)) {
      return null;
    }
    return codecService.decode(productKey, payload);
  }

  @Override
  public String name() {
    return ctaIoTModuleInfo.getCode();
  }

  @Override
  protected String currentComponent() {
    return name();
  }
}
