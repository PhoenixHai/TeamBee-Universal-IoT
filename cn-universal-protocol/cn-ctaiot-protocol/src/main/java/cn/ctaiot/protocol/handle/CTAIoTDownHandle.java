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

package cn.ctaiot.protocol.handle;

import cn.ctaiot.protocol.config.CTAIoTModuleInfo;
import cn.ctaiot.protocol.entity.CTAIoTDownRequest;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.iot.constant.IotConstant.DeviceStatus;
import cn.universal.core.iot.constant.IotConstant.ERROR_CODE;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.core.iot.util.DingTalkUtil;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IoTProductAction;
import cn.universal.persistence.base.IotDownAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import com.ctg.ag.sdk.biz.AepDeviceCommandClient;
import com.ctg.ag.sdk.biz.AepDeviceCommandLwmProfileClient;
import com.ctg.ag.sdk.biz.AepDeviceManagementClient;
import com.ctg.ag.sdk.biz.AepProductManagementClient;
import com.ctg.ag.sdk.biz.AepPublicProductManagementClient;
import com.ctg.ag.sdk.biz.aep_device_command_lwm_profile.CreateCommandLwm2mProfileRequest;
import com.ctg.ag.sdk.biz.aep_device_command_lwm_profile.CreateCommandLwm2mProfileResponse;
import com.ctg.ag.sdk.biz.aep_device_management.CreateDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.CreateDeviceResponse;
import com.ctg.ag.sdk.biz.aep_device_management.DeleteDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.DeleteDeviceResponse;
import com.ctg.ag.sdk.biz.aep_device_management.UpdateDeviceRequest;
import com.ctg.ag.sdk.biz.aep_device_management.UpdateDeviceResponse;
import com.ctg.ag.sdk.biz.aep_product_management.CreateProductRequest;
import com.ctg.ag.sdk.biz.aep_product_management.CreateProductResponse;
import com.ctg.ag.sdk.biz.aep_product_management.DeleteProductRequest;
import com.ctg.ag.sdk.biz.aep_product_management.DeleteProductResponse;
import com.ctg.ag.sdk.biz.aep_product_management.UpdateProductRequest;
import com.ctg.ag.sdk.biz.aep_product_management.UpdateProductResponse;
import com.ctg.ag.sdk.biz.aep_public_product_management.InstantiateProductRequest;
import com.ctg.ag.sdk.biz.aep_public_product_management.InstantiateProductResponse;
import com.ctg.ag.sdk.biz.aep_public_product_management.QueryAllPublicProductListRequest;
import com.ctg.ag.sdk.biz.aep_public_product_management.QueryAllPublicProductListResponse;
import jakarta.annotation.Resource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 电信下行实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/20 08:41
 */
@Component
@Slf4j
public class CTAIoTDownHandle extends IotDownAdapter<CTAIoTDownRequest> {

  @Resource
  private CTAIoTModuleInfo ctaIoTModuleInfo;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource(name = "ioTProductActionService")
  private IoTProductAction ioTProductAction;

  @Resource
  private AepDeviceManagementClient aepDeviceManagementClient;

  @Resource
  private AepProductManagementClient aepProductManagementClient;

  @Resource
  private AepPublicProductManagementClient aepPublicProductManagementClient;

  @Resource
  private AepDeviceCommandClient aepDeviceCommandClient;

  @Resource
  private AepDeviceCommandLwmProfileClient aepDeviceCommandLwmProfileClient;

  @Resource
  private IoTProductMapper ioTProductMapper;
  @Resource
  private IoTDeviceMapper ioTDeviceMapper;
  @Resource
  private SupportMapAreasMapper supportMapAreasMapper;

  @Resource
  private IoTDeviceService iotDeviceService;

  String havICCID =
      """
          {
            "tags": [],
            "events": [
              {
                "id": "online",
                "name": "上线",
                "valueType": {
                  "type": "string"
                }
              },
              {
                "id": "offline",
                "name": "下线",
                "valueType": {
                  "type": "string"
                }
              }
            ],
            "functions": [],
            "properties": [
              {
                "id": "iccid",
                "mode": "r",
                "name": "ICCID",
                "valueType": {
                  "type": "string"
                },
                "description": "SIM卡ICCID"
              }
            ]
          }
          """;

  public R down(CTAIoTDownRequest downRequest) {
    if (downRequest == null || downRequest.getCmd() == null) {
      log.warn("[CT-AIoT下行][参数异常] 下行对象为空,不处理 downRequest={}", downRequest);
      return R.error("电信物联下行对象为空");
    }
    R preR = preDown(downRequest.getIoTProduct(), downRequest.getCtwingRequestData(), downRequest);
    if (Objects.nonNull(preR)) {
      return preR;
    }
    R r = null;
    switch (downRequest.getCmd()) {
      case DEV_ADD:
        r = devAdd(downRequest);
        break;
      case DEV_DEL:
        r = devDel(downRequest);
        break;
      case DEV_UPDATE:
        r = devUpdate(downRequest);
        break;
      case DEV_FUNCTION:
        r = devConfig(downRequest);
        break;
      case PRO_ADD:
        r = proAdd(downRequest);
        break;
      case PRO_DEL:
        r = proDel(downRequest);
        break;
      case PRO_UPDATE:
        r = proUpdate(downRequest);
        break;
      case PUBPRO_GET:
        r = pubproGet(downRequest);
        break;
      case PUBPRO_ADD:
        r = pubproAdd(downRequest);
        break;
      default:
        log.info("[CT-AIoT下行][未匹配到方法] cmd={}", downRequest.getCmd());
    }
    return r;
  }

  /**
   * 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001
   */
  private R devConfig(CTAIoTDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size == 0) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getName());
    }
    IoTProduct ioTProduct = IoTProduct.builder().productKey(downRequest.getProductKey()).build();
    IoTDevice ioTDeviceOne = ioTDeviceMapper.selectOne(ioTDevice);
    IoTProduct ioTProductOne = ioTProductMapper.selectOne(ioTProduct);

    R r = callGlobalFunction(ioTProductOne, ioTDeviceOne, downRequest);
    if (Objects.nonNull(r)) {
      return r;
    }

    Map<String, Object> devConfig = new HashMap<>();
    Map<String, Object> content = new HashMap<>();
    Map<String, Object> command = new HashMap<>();
    String downParaType =
        downRequest.getCtwingRequestData().getConfiguration().get("downParaType").toString();
    // 判断是否是DTU子设备
    if (!ioTProductOne.getDeviceNode().equals("DTU_SUB_DEVICE")) {
      devConfig.put("deviceId", ioTDeviceOne.getExtDeviceId());
      devConfig.put(
          "operator",
          downRequest.getCtwingRequestData().getConfiguration().getOrDefault("operator", "univ"));
      devConfig.put(
          "productId", downRequest.getCtwingRequestData().getConfiguration().get("productId"));
      // 设备指令缓存时长和报文数据类型(字符串、16进制)，默认使用产品配置的
      if (downRequest.getCtwingRequestData().getConfiguration().containsKey("ttl")) {
        devConfig.put("ttl", downRequest.getCtwingRequestData().getConfiguration().get("ttl"));
      } else {
        devConfig.put("ttl", downRequest.getCtwingRequestData().getTtl());
      }
      // downParaType字段，0为TCP和LWM2M协议透传下发(统一合并指令下发)
      if ("0".equals(downParaType)) {
        if (downRequest.getCtwingRequestData().getConfiguration().containsKey("dataType")) {
          // dataType;下发指令数据类型：1-字符串，2-16进制
          content.put(
              "dataType", downRequest.getCtwingRequestData().getConfiguration().get("dataType"));
        } else {
          content.put("dataType", downRequest.getCtwingRequestData().getDataType());
        }
        content.put("payload", downRequest.getPayload());
      }
      // downParaType字段，1为MQTT、LWM2M协议非透传(统一合并指令下发)
      if ("1".equals(downParaType)) {
        content.put("serviceIdentifier", downRequest.getFunction().get("function"));
        content.put("params", downRequest.getFunction().get("data"));
      }
      if ("2".equals(downParaType)) {
        content.put("payload", downRequest.getFunction().get("data"));
      }
      CreateCommandLwm2mProfileRequest requestCommand = new CreateCommandLwm2mProfileRequest();
      // downParaType字段，profile为lwm2m协议有profile指令下发接口
      if ("profile".equals(downParaType)) {
        command.put("serviceId", downRequest.getFunction().get("serviceType"));
        command.put("method", downRequest.getFunction().get("function"));
        command.put("paras", downRequest.getFunction().get("data"));
        devConfig.put("command", command);
        requestCommand.setParamMasterKey(
            downRequest.getCtwingRequestData().getConfiguration().get("masterKey"));
        requestCommand.setBody(StrUtil.bytes(JSONUtil.toJsonStr(devConfig)));
      }
      devConfig.put("content", content);
      com.ctg.ag.sdk.biz.aep_device_command.CreateCommandRequest request =
          new com.ctg.ag.sdk.biz.aep_device_command.CreateCommandRequest();
      request.setParamMasterKey(
          downRequest.getCtwingRequestData().getConfiguration().get("masterKey")); // single value
      request.setBody(StrUtil.bytes(JSONUtil.toJsonStr(devConfig))); // 具体格式见前面请求body说明
      try {
        // 开始发送电信
        JSONObject result = new JSONObject();
        if ("0".equals(downParaType) || "1".equals(downParaType)) {
          com.ctg.ag.sdk.biz.aep_device_command.CreateCommandResponse resp =
              aepDeviceCommandClient.CreateCommand(request);
          String resultBody = StrUtil.str(resp.getBody(), Charset.defaultCharset());
          log.info(
              "功能下发，请求电信,参数={} 返回={}",
              JSONUtil.toJsonStr(devConfig),
              resp == null ? "" : resultBody);
          result = JSONUtil.parseObj(resultBody);
        } else if ("profile".equals(downParaType)) {
          CreateCommandLwm2mProfileResponse resp =
              aepDeviceCommandLwmProfileClient.CreateCommandLwm2mProfile(requestCommand);
          String resultBody = StrUtil.str(resp.getBody(), Charset.defaultCharset());
          log.info(
              "profile 功能下发，请求电信,参数={} 返回={}",
              JSONUtil.toJsonStr(devConfig),
              resp == null ? "" : resultBody);
          result = JSONUtil.parseObj(resultBody);
        }
        int res = result.getInt("code");
        if (result != null && res == 0) {
          // 保存指令下发日志---开始
          JSONObject commandBody = result.getJSONObject("result");
          commandBody.set("function", downRequest.getFunction().getOrDefault("function", ""));
          IoTDeviceDTO ioTDeviceDTO =
              iotDeviceService.selectDevInstanceBO(
                  downRequest.getProductKey(), downRequest.getDeviceId());
          ioTDeviceLifeCycle.command(ioTDeviceDTO, commandBody.getStr("commandId"), commandBody);
          if (result.isNull("imei")) {
            JSONObject results = result.getJSONObject("result");
            results.set("imei", ioTDevice.getDeviceId());
          }
          // 保存指令下发日志---结束
          return R.ok(result);
        } // 参数为空
        if (result != null && res == 1304) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_NULL.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_NULL.getName());
        } // 参数解析失败
        if (result != null && res == 8802) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_FAIL.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_FAIL.getName());
        } // 指令下发内容与服务模型参数范围不匹配
        if (result != null && res == 1810) {
          return R.error(
              ERROR_CODE.DEV_PARA_RANGE_ERROR.getCode(), ERROR_CODE.DEV_PARA_RANGE_ERROR.getName());
        } // 设备未激活
        if (result != null && res == 1314) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_STATE_ERROR.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_STATE_ERROR.getName());
        } // 参数校验失败
        if (result != null && res == 8803) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_ERROR.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_PARA_ERROR.getName());
        } // MasterKey不匹配
        if (result != null && res == 2010105) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_MASTERKEY_ERROR.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_MASTERKEY_ERROR.getName());
        } // 指令应为偶数
        if (result != null && res == 1350) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_MESSAGE_ERROR.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_MESSAGE_ERROR.getName());
        } // 下发指令不能为空
        if (result != null && res == 1316) {
          return R.error(
              ERROR_CODE.DEV_CONFIG_DEVICE_NULL.getCode(),
              ERROR_CODE.DEV_CONFIG_DEVICE_NULL.getName());
        }
      } catch (Exception e) {
        log.error("下发电信nb设备配置异常", e);
        DingTalkUtil.send("下发电信nb设备配置异常" + ExceptionUtil.getSimpleMessage(e));
      }
      return R.error(ERROR_CODE.DEV_CONFIG_ERROR.getCode(), ERROR_CODE.DEV_CONFIG_ERROR.getName());
    }
    return R.error(ERROR_CODE.DEV_CONFIG_ERROR.getCode(), ERROR_CODE.DEV_CONFIG_ERROR.getName());
  }

  /**
   * 设备添加
   */
  private R devAdd(CTAIoTDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size > 0) {
      // 设备已经存在
      return R.error(
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getCode(),
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getName());
    }
    Map<String, Object> devAdd = new HashMap<>();
    devAdd.put("productId", downRequest.getCtwingRequestData().getConfiguration().get("productId"));
    devAdd.put("deviceName", downRequest.getCtwingRequestData().getDeviceName());
    devAdd.put("deviceSn", downRequest.getDeviceId());
    devAdd.put("imei", downRequest.getDeviceId());
    // 操作者，必填
    devAdd.put(
        "operator",
        downRequest.getCtwingRequestData().getConfiguration().getOrDefault("operator", "univ"));

    Map<String, Object> other = new HashMap<>();
    // 0.自动订阅 1.取消自动订阅，必填;
    other.put("autoObserver", 0);
    devAdd.put("other", other);

    CreateDeviceRequest request = new CreateDeviceRequest();
    request.setParamMasterKey(
        downRequest.getCtwingRequestData().getConfiguration().get("masterKey")); // single value
    request.setBody(StrUtil.bytes(JSONUtil.toJsonStr(devAdd))); // 具体格式见前面请求body说明
    try {
      // 开始发送电信
      CreateDeviceResponse resp = aepDeviceManagementClient.CreateDevice(request);
      String ctResult = StrUtil.str(resp.getBody(), Charset.defaultCharset());
      log.info("设备添加，请求电信,参数={} 返回={}", downRequest, ctResult);
      JSONObject result = JSONUtil.parseObj(ctResult);
      if (result != null && result.getInt("code") == 0) {
        Map<String, Object> saveResult =
            saveDevInstance(downRequest, result.getJSONObject("result").getStr("deviceId"));
        // 设备生命周期-创建
        ioTDeviceLifeCycle.create(
            downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);
        return R.ok(saveResult);
      } else {
        return R.error(ERROR_CODE.DEV_ADD_ERROR.getCode(), result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("添加电信nb设备异常", e);
      DingTalkUtil.send("电信nb设备添加异常" + ExceptionUtil.getSimpleMessage(e));
    }
    return R.error(ERROR_CODE.DEV_ADD_ERROR.getCode(), ERROR_CODE.DEV_ADD_ERROR.getName());
  }

  private Map<String, Object> saveDevInstance(CTAIoTDownRequest downRequest, String extDeviceId) {
    String iotId = IdUtil.simpleUUID();
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .detail(downRequest.getDetail())
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getCtwingRequestData().getDeviceName())
            .iotId(iotId)
            .state(DeviceStatus.offline.getCode())
            .creatorId(downRequest.getAppUnionId())
            .productName(downRequest.getIoTProduct().getName())
            .productKey(downRequest.getProductKey())
            .gwProductKey(downRequest.getGwProductKey())
            .application(downRequest.getApplicationId())
            .extDeviceId(extDeviceId)
            .build();

    if (StrUtil.isNotBlank(downRequest.getCtwingRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getCtwingRequestData().getLongitude())) {

      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getCtwingRequestData().getLongitude(),
              downRequest.getCtwingRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getCtwingRequestData().getLongitude(),
              downRequest.getCtwingRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getCtwingRequestData().getLongitude(),
            downRequest.getCtwingRequestData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }
    Map<String, Object> config = new HashMap<>();
    config.put(
        "imei",
        StrUtil.isNotBlank(downRequest.getDeviceId())
            ? downRequest.getDeviceId()
            : downRequest.getCtwingRequestData().getImei());
    config.put("meterNo", downRequest.getCtwingRequestData().getMeterNo());
    finalDown(
        config,
        downRequest.getIoTProduct(),
        downRequest.getCmd(),
        downRequest.getCtwingRequestData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", iotId);
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
    return result;
  }

  @Override
  public void Rule() {
  }

  /**
   * 删除电信平台设备 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001 Aleo
   */
  private R devDel(CTAIoTDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    DeleteDeviceRequest request = new DeleteDeviceRequest();
    request.setParamMasterKey(
        downRequest.getCtwingRequestData().getConfiguration().get("masterKey")); // single value
    request.setParam(
        "productId", downRequest.getCtwingRequestData().getConfiguration().get("productId"));
    List<Map> mapList = new ArrayList<>();
    String[] split = ioTDevice.getDeviceId().split(",");
    List<IoTDevice> ioTDeviceList = new ArrayList<>();
    for (int i = 0; i < split.length; i++) {
      ioTDevice.setDeviceId(split[i]);
      IoTDevice ioTDeviceOne = ioTDeviceMapper.selectOne(ioTDevice);
      ioTDeviceList.add(ioTDeviceOne);
      Map<String, Object> result = new HashMap<>();
      result.put("deviceId", ioTDeviceOne.getDeviceId());
      result.put("deviceName", ioTDeviceOne.getDeviceName());
      result.put("productName", ioTDeviceOne.getProductName());
      mapList.add(result);
      int size = ioTDeviceMapper.selectCount(ioTDevice);
      if (size == 0) {
        // 设备不存在
        return R.error(
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getCode(),
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getName());
      }
    }
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < ioTDeviceList.size() - 1; i++) {
      buffer.append(ioTDeviceList.get(i).getExtDeviceId() + ",");
    }
    buffer.append(ioTDeviceList.get(ioTDeviceList.size() - 1).getExtDeviceId());
    request.setParam("deviceIds", buffer);
    try {
      // 开始发送电信
      DeleteDeviceResponse resp = aepDeviceManagementClient.DeleteDevice(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("设备删除请求电信,参数={} 返回={}", request, result);
      if (result != null && result.getInt("code") == 0) {
        log.info("del iot instance {}", JSONUtil.toJsonStr(ioTDeviceList));
        deleteDevInstance(ioTDeviceList, downRequest);
        return R.ok(mapList);
      } else {
        JSONArray jsonArray = JSONUtil.parseArray(result.getStr("result"));
        JSONObject object = JSONUtil.parseObj(jsonArray.get(0));
        String errorMsg = object.getStr("reason");
        return R.error(errorMsg);
      }
    } catch (Exception e) {
      log.error("删除电信nb设备异常", e);
      DingTalkUtil.send("删除电信nb设备异常" + ExceptionUtil.getSimpleMessage(e));
    }
    return R.ok();
  }

  /**
   * 删除本地数据库设备 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001 Aleo
   */
  private void deleteDevInstance(List<IoTDevice> ioTDeviceList, DownRequest downRequest) {
    for (IoTDevice dev : ioTDeviceList) {
      // 设备生命周期-删除
      Map<String, Object> objectMap = new HashMap<>();
      objectMap.put("iotId", dev.getIotId());
      ioTDeviceLifeCycle.delete(iotDeviceService.selectDevInstanceBO(objectMap), downRequest);
      iotDeviceService.delDevInstance(dev.getIotId());
    }
  }

  /**
   * 修改电信平台设备 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001
   */
  private R devUpdate(CTAIoTDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size == 0) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getName());
    }

    UpdateDeviceRequest request = new UpdateDeviceRequest();
    IoTDevice ioTDeviceOne = ioTDeviceMapper.selectOne(ioTDevice);

    request.setParam("deviceId", ioTDeviceOne.getExtDeviceId());
    Map<String, Object> devUpdate = new HashMap<>();
    devUpdate.put(
        "productId", downRequest.getCtwingRequestData().getConfiguration().get("productId"));
    devUpdate.put("deviceName", downRequest.getCtwingRequestData().getDeviceName());

    // 操作者，必填
    devUpdate.put(
        "operator",
        downRequest.getCtwingRequestData().getConfiguration().getOrDefault("operator", "univ"));

    ioTDeviceOne.setDeviceName(downRequest.getCtwingRequestData().getDeviceName());

    request.setParamMasterKey(
        downRequest.getCtwingRequestData().getConfiguration().get("masterKey")); // single value
    request.setBody(StrUtil.bytes(JSONUtil.toJsonStr(devUpdate))); // 具体格式见前面请求body说明
    try {
      // 开始发送电信
      UpdateDeviceResponse resp = aepDeviceManagementClient.UpdateDevice(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("请求电信更新,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        Map<String, Object> updateResult = updateDevInstance(ioTDeviceOne, downRequest);
        // 设备生命周期-修改
        ioTDeviceLifeCycle.update(ioTDeviceOne.getIotId());
        return R.ok(updateResult);
      }
    } catch (Exception e) {
      log.error("添加电信nb设备异常", e);
    }
    return R.ok("ok", downRequest.getIoTProduct().getId());
  }

  /**
   * 修改本地数据库设备 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001 Aleo
   */
  private Map<String, Object> updateDevInstance(
      IoTDevice ioTDevice, CTAIoTDownRequest downRequest) {

    if (StrUtil.isNotBlank(downRequest.getCtwingRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getCtwingRequestData().getLongitude())) {

      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getCtwingRequestData().getLongitude(),
              downRequest.getCtwingRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getCtwingRequestData().getLongitude(),
              downRequest.getCtwingRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getCtwingRequestData().getLongitude(),
            downRequest.getCtwingRequestData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("deviceId", ioTDevice.getDeviceId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    ioTDevice.setDetail(downRequest.getDetail());
    ioTDeviceMapper.updateByPrimaryKey(ioTDevice);
    return result;
  }

  /**
   * 产品添加
   */
  private R proAdd(CTAIoTDownRequest downRequest) {
    Map<String, Object> proAdd = new HashMap<>();
    proAdd.put("productName", downRequest.getCtwingRequestData().getProductName());
    //    proAdd.put("productType", downRequest.getctwingRequestData().getProductType());
    //    proAdd.put("secondaryType", downRequest.getctwingRequestData().getSecondaryType());
    //    proAdd.put("thirdType", downRequest.getctwingRequestData().getThirdType());

    proAdd.put("productType", "智慧城市");
    proAdd.put("secondaryType", "消防安全");
    proAdd.put("thirdType", "灭火器");

    proAdd.put("nodeType", 1); // 设备
    proAdd.put("accessType", 1); // 设备直连
    proAdd.put("networkType", 3); // NB-IOT
    proAdd.put("productProtocol", 3); // LWM2W
    proAdd.put("dataEncryption", 5); // 明文
    proAdd.put("authType", 4); // IMEI认证
    proAdd.put("endpointFormat", 1); // IMEI
    proAdd.put("tupDeviceModel", downRequest.getCtwingRequestData().getTupDeviceModel());
    proAdd.put("tupIsThrough", downRequest.getCtwingRequestData().getTupIsThrough());
    proAdd.put("payloadFormat", downRequest.getCtwingRequestData().getPayloadFormat());
    proAdd.put("powerModel", downRequest.getCtwingRequestData().getPowerModel());
    proAdd.put("lwm2mEdrxTime", downRequest.getCtwingRequestData().getLwm2mEdrxTime());
    CreateProductRequest request = new CreateProductRequest();
    request.setBody(StrUtil.bytes(JSONUtil.toJsonStr(proAdd)));
    try {
      // 开始发送电信
      CreateProductResponse resp = aepProductManagementClient.CreateProduct(request);
      String ctResult = StrUtil.str(resp.getBody(), Charset.defaultCharset());
      JSONObject result = JSONUtil.parseObj(ctResult);
      log.info("产品添加，请求电信,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        // 产品生命周期
        IoTProduct ioTProduct = saveProduct(result, downRequest);
        ioTProductAction.create(ioTProduct.getProductKey(), ioTProduct.getCreatorId());
        return R.ok(1);
      } else {
        return R.error(result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("添加电信产品异常", e);
    }
    return R.error("新增电信产品异常");
  }

  /**
   * 公共产品添加
   */
  private R pubproAdd(CTAIoTDownRequest downRequest) {
    InstantiateProductRequest request = new InstantiateProductRequest();
    Map<String, Object> map = new HashMap<>();
    Integer publicProductId = downRequest.getCtwingRequestData().getPublicProductId();
    map.put("publicProductId", publicProductId);
    request.setBody(JSONUtil.parseObj(map).toString().getBytes());
    try {
      // 开始发送电信
      InstantiateProductResponse resp =
          aepPublicProductManagementClient.InstantiateProduct(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("公共产品添加，请求电信,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        // 产品生命周期
        IoTProduct ioTProduct = savePublicProduct(result, downRequest);
        ioTProductAction.create(ioTProduct.getProductKey(), ioTProduct.getCreatorId());
        return R.ok(1);
      } else {
        return R.error(result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("添加电信公共产品异常", e);
    }
    return R.error("添加电信公共产品异常");
  }

  /**
   * 本地添加产品
   */
  private IoTProduct saveProduct(JSONObject jsonObject, CTAIoTDownRequest downRequest) {
    JSONObject json = jsonObject.getJSONObject("result");
    JSONObject configuration = new JSONObject();
    configuration.set("masterKey", json.getStr("apiKey"));
    configuration.set("productId", json.getInt("productId"));
    configuration.set("powerModel", json.getInt("powerModel"));
    configuration.set("payloadFormat", downRequest.getCtwingRequestData().getPayloadFormat());
    configuration.set("lwm2mEdrxTime", downRequest.getCtwingRequestData().getLwm2mEdrxTime());
    configuration.set("ttl", 86400);
    configuration.set("operator", "univ");
    configuration.set("dataType", 2);
    configuration.set("downParaType", 0);
    configuration.set("requirePayload", true);
    IoTProduct ioTProduct =
        IoTProduct.builder()
            .productId(downRequest.getCtwingRequestData().getTupDeviceModel())
            .productKey(IdUtil.objectId())
            .thirdPlatform(ctaIoTModuleInfo.getCode())
            .deviceNode("DEVICE")
            .name(downRequest.getCtwingRequestData().getProductName())
            .creatorId(downRequest.getCreatorId())
            .transportProtocol("NB-iot")
            .createTime(new Date().getTime())
            .companyNo(downRequest.getCompanyNo())
            .productSecret(json.getStr("apiKey"))
            .classifiedId(downRequest.getClassifiedId())
            .classifiedName(downRequest.getClassifiedName())
            .metadata(havICCID)
            .state(IotConstant.NORMAL.byteValue())
            .storePolicy("mysql")
            .configuration(configuration.toString())
            .thirdDownRequest(JSONUtil.toJsonStr(downRequest))
            .build();
    ioTProductMapper.insertSelective(ioTProduct);
    return ioTProduct;
  }

  /**
   * 本地添加公共产品
   */
  private IoTProduct savePublicProduct(JSONObject jsonObject, CTAIoTDownRequest downRequest) {
    JSONObject json = jsonObject.getJSONObject("result");
    JSONObject configuration = new JSONObject();
    configuration.set("masterKey", json.getStr("masterKey"));
    configuration.set("productId", json.getInt("productId"));
    configuration.set("powerModel", json.getInt("powerModel"));
    configuration.set("payloadFormat", downRequest.getCtwingRequestData().getPayloadFormat());
    configuration.set("lwm2mEdrxTime", downRequest.getCtwingRequestData().getLwm2mEdrxTime());
    configuration.set("ttl", 86400);
    configuration.set("operator", "univ");
    configuration.set("dataType", 2);
    configuration.set("downParaType", 0);
    configuration.set("requirePayload", true);
    IoTProduct ioTProduct =
        IoTProduct.builder()
            .productId(downRequest.getCtwingRequestData().getProductId())
            .productKey(IdUtil.objectId())
            .thirdPlatform(ctaIoTModuleInfo.getCode())
            .deviceNode("DEVICE")
            .name(downRequest.getClassifiedName())
            .creatorId(downRequest.getCreatorId())
            .transportProtocol("NB-iot")
            .createTime(new Date().getTime())
            .companyNo(downRequest.getCompanyNo())
            .classifiedId(downRequest.getClassifiedId())
            .classifiedName(downRequest.getClassifiedName())
            .metadata(havICCID)
            .state(IotConstant.NORMAL.byteValue())
            .storePolicy("mysql")
            .configuration(configuration.toString())
            .thirdDownRequest(JSONUtil.toJsonStr(downRequest))
            .build();
    ioTProductMapper.insertSelective(ioTProduct);
    return ioTProduct;
  }

  /**
   * 删除电信产品
   */
  private R proDel(CTAIoTDownRequest downRequest) {
    DeleteProductRequest request = new DeleteProductRequest();
    request.setParamMasterKey(downRequest.getCtwingRequestData().getMasterKey());
    // single value
    request.setParam("productId", downRequest.getCtwingRequestData().getProductId());
    try {
      // 开始发送电信
      DeleteProductResponse resp = aepProductManagementClient.DeleteProduct(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("电信产品删除，请求电信,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        // 清除缓存
        ioTProductAction.delete(downRequest.getProductKey());
        log.info("del iot instance {}", downRequest.getCtwingRequestData().getProductId());
        return R.ok(deleteDevProduct(downRequest.getProductKey()));
      } else {
        return R.error(result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("删除电信产品异常", e);
    }
    return R.error("删除电信产品异常");
  }

  /**
   * 删除本地数据库产品
   */
  private int deleteDevProduct(String productKey) {
    IoTProduct ioTProduct = IoTProduct.builder().productKey(productKey).build();
    return ioTProductMapper.delete(ioTProduct);
  }

  /**
   * 修改电信产品
   */
  private R proUpdate(CTAIoTDownRequest downRequest) {

    UpdateProductRequest request = new UpdateProductRequest();
    Map<String, Object> devUpdate = new HashMap<>();
    devUpdate.put("powerModel", downRequest.getCtwingRequestData().getPowerModel());
    devUpdate.put("productName", downRequest.getCtwingRequestData().getProductName());
    devUpdate.put("productId", downRequest.getCtwingRequestData().getProductId());
    devUpdate.put("endpointFormat", 1);
    String lwm2mEdrxTime = downRequest.getCtwingRequestData().getLwm2mEdrxTime();
    if (lwm2mEdrxTime != null) {
      devUpdate.put("lwm2mEdrxTime", lwm2mEdrxTime);
    }
    request.setBody(StrUtil.bytes(JSONUtil.toJsonStr(devUpdate)));
    try {
      // 开始发送电信
      UpdateProductResponse resp = aepProductManagementClient.UpdateProduct(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("电信产品更新，请求电信,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        log.info("update iot instance {}", downRequest.getCtwingRequestData().getProductId());
        return R.ok(updateDevProduct(downRequest.getProductKey(), downRequest));
      } else {
        return R.error(result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("修改电信产品异常", e);
    }
    return R.error("修改电信产品异常");
  }

  /**
   * 修改本地数据库产品
   */
  private int updateDevProduct(String productKey, CTAIoTDownRequest downRequest) {
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    ioTProduct.setClassifiedId(downRequest.getCtwingRequestData().getClassifiedId());
    ioTProduct.setName(downRequest.getCtwingRequestData().getProductName());
    JSONObject configuration = JSONUtil.parseObj(ioTProduct.getConfiguration());
    configuration.set("powerModel", downRequest.getCtwingRequestData().getPowerModel());
    configuration.set("lwm2mEdrxTime", downRequest.getCtwingRequestData().getLwm2mEdrxTime());
    ioTProduct.setName(downRequest.getCtwingRequestData().getProductName());
    ioTProduct.setConfiguration(configuration.toString());
    int count = ioTProductMapper.updateDevProduct(ioTProduct);
    log.info("修改产品，数据更新条数={}", count);
    ioTProductAction.update(ioTProduct);
    return count;
  }

  /**
   * 查询电信公共产品
   */
  private R pubproGet(CTAIoTDownRequest downRequest) {
    QueryAllPublicProductListRequest request = new QueryAllPublicProductListRequest();
    request.setParamSearchValue(downRequest.getCtwingRequestData().getSearchValue());
    request.setParamPageNow(downRequest.getCtwingRequestData().getPageNow());
    request.setParamPageSize(downRequest.getCtwingRequestData().getPageSize());

    try {
      // 开始发送电信
      QueryAllPublicProductListResponse resp =
          aepPublicProductManagementClient.QueryAllPublicProductList(request);
      JSONObject result = JSONUtil.parseObj(StrUtil.str(resp.getBody(), Charset.defaultCharset()));
      log.info("电信产品查询，请求电信,参数={} 返回={}", downRequest, result);
      if (result != null && result.getInt("code") == 0) {
        return R.ok(result.getStr("result"));
      } else {
        return R.error(result.getStr("msg"));
      }
    } catch (Exception e) {
      log.error("查询电信公共产品异常", e);
    }
    return R.error("查询电信公共产品异常");
  }
}
