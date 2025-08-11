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

package cn.onenet.protocol.onenet.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 〈一句话功能简述〉<br>
 * 〈MQTT接收消息处理〉 @Author Aleo
 *
 * @create 2018/6/4
 * @since 1.0.0
 */
@Configuration
@Slf4j
public class OnenetMqttConfig {

  /*@Resource(name = "onenetUPService")
  private IUP iup;
  @Value("${ctaiot.protocol.broker}")
  private String broker;
  @Value("${ctaiot.protocol.userId}")
  private String userId;
  @Value("${ctaiot.protocol.accessKey}")
  private String accessKey;

  @PostConstruct
  public void init() {

    System.out.println("移动mqtt启动了");
    // 应用长连接服务地址
    String broker = "network://183.0.0.1:8883";

    // 安全策略参数，获取用户OneNET用户userId和accessKey，均可在用户中心查看
    String userId = "198133";
    String accessKey = "GGS7ZwrTZM8o8kUAEjkm65XmEB9VEAaBKZTMWAUR7mn+jJqdpAeKGRPE0HorpNChPbohlq0WMdZg5Xf0b+mBfw==";

    // 产品id
    String productId = "0hk37X24eu";
    // 设备名称
    String deviceName = "one";

    // 命令下发，物模型属性功能点标识符
    String identifier1 = "temp";
    String identifier2 = "humi";

    // 命令下发值，物模型属性功能点数据类型对应value
    Integer value1 = 30;
    Float value2 = 25.5f;

    try {
      CustomMqttClient client = new CustomMqttClient(broker, userId, accessKey, new CustomMqttCallback());

      // 发起连接
      client.connect();
      // 订阅topic
      client.subscribe(productId, deviceName);

      // 发布属性设置消息
      client.pubProperty(productId, deviceName, identifier1, value1);

      // 发布期望值设置消息
      client.pubDesired(productId, deviceName, identifier2, value2);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }


  public class CustomMqttCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
      System.out.println("连接丢失，可自己实现重新连接");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
      System.out.println("接收到订阅消息:" + message);
      System.out.println("消息内容:" + new String(message.getPayload()));
      iup.up(new String(message.getPayload())) ;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
      System.out.println("发送完成:" + token.isComplete());
    }
  }


  public class CustomMqttClient {

    private String broker;
    private String userName;
    private String password;

    private MqttClient mqttClient;
    private MqttCallback mqttCallback;
    private MqttConnectOptions options;

    public CustomMqttClient(String broker
        , String userId
        , String accessKey
        , MqttCallback mqttCallback) throws MqttException {
      this.broker = broker;
      this.userName = "user/" + userId;
      this.password = getAuthorization(userId, accessKey);
      this.mqttCallback = mqttCallback;
      initClient();
      initOptions();
    }

    private void initClient() throws MqttException {
      MqttClient mqttClient = new MqttClient(this.broker, uuid(), new MemoryPersistence());
      // 配置回调
      mqttClient.setCallback(this.mqttCallback);
      this.mqttClient = mqttClient;
    }

    private void initOptions() {
      // MQTT连接项
      MqttConnectOptions options = new MqttConnectOptions();
      options.setUserName(this.userName);
      options.setPassword(this.password.toCharArray());
      // 连接超时时间
      options.setConnectionTimeout(30);
      // 发送或接收消息之间的最大间隔时间
      options.setKeepAliveInterval(60);
      // 不保留对话
      options.setCleanSession(true);
      // 设置mqtt版本
      options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
      this.options = options;
    }

    public void connect() throws MqttException {
      this.mqttClient.connect(this.options);
    }

    public void disconnect() throws MqttException {
      this.mqttClient.disconnect();
    }

    public void subscribe(String productId, String deviceName) throws MqttException {
      // 设备上行topic（包括设备生命周期状态变更、属性、事件功能点及命令下发响应）
      String[] upTopics = {
          "$sys/" + productId + "/" + deviceName + "/thing/action",
          "$sys/" + productId + "/" + deviceName + "/thing/property",
          "$sys/" + productId + "/" + deviceName + "/thing/event",
          "$sys/" + productId + "/" + deviceName + "/thing/property/set/reply"
      };
      this.mqttClient.subscribe(upTopics);
    }

    public void pubProperty(String productId, String deviceName, String identifier, Object value) throws MqttException {
      // 设备属性设置topic
      String propertyTopic = "$sys/" + productId + "/" + deviceName + "/thing/property/set";
      publish(propertyTopic, getPayload(identifier, value));
    }

    public void pubDesired(String productId, String deviceName, String identifier, Object value) throws MqttException {
      // 设备期望值设置
      String desiredTopic = "$sys/" + productId + "/" + deviceName + "/thing/property/desired/set";
      publish(desiredTopic, getPayload(identifier, value));
    }

    private void publish(String topic, String payload) throws MqttException {
      this.mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
    }

    public void close() throws MqttException {
      this.mqttClient.close();
    }

    private String getPayload(String identifier, Object value) {
      return "{\"data\":{\"params\":{\"" + identifier + "\":" + value + "},\"version\":\"1.0\"},\"requestId\":\"" +
      uuid() + "\"}";
    }

    private String uuid() {
      return UUID.randomUUID().toString().replace("-", "");
    }

    private String getAuthorization(String userId, String accessKey) {
      long et = LocalDateTime.now().plusHours(12).toInstant(ZoneOffset.of("+8")).getEpochSecond();
      String method = "sha1";
      String res = "userid/" + userId;
      String version = "2020-05-29";

      String sign = null;
      String forSign = et + "\n" + method + "\n" + res + "\n" + version;
      try {
        sign = new String(Base64.getEncoder().encode(HmacEncrypt(forSign
            , Base64.getDecoder().decode(accessKey)
            , method)));
      } catch (NoSuchAlgorithmException | InvalidKeyException e) {
        e.printStackTrace();
      }
      if (StringUtil.isEmpty(sign)) {
        return null;
      }
      Map<String, Object> paramsMap = new HashMap<>();
      paramsMap.put("et", et);
      paramsMap.put("method", method);
      paramsMap.put("version", version);
      try {
        paramsMap.put("sign", URLEncoder.encode(sign, "utf-8"));
        paramsMap.put("res", URLEncoder.encode(res, "utf-8"));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      return JSONUtil.toJsonStr(paramsMap);
    }
  }

  private static byte[] HmacEncrypt(String data, byte[] key, String signatureMethod)
      throws NoSuchAlgorithmException, InvalidKeyException {
    //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
    SecretKeySpec signinKey = null;
    signinKey = new SecretKeySpec(key,
        "Hmac" + signatureMethod.toUpperCase());

    //生成一个指定 Mac 算法 的 Mac 对象
    Mac mac = Mac.getInstance("Hmac" + signatureMethod.toUpperCase());

    //用给定密钥初始化 Mac 对象
    mac.init(signinKey);

    //完成 Mac 操作
    return mac.doFinal(data.getBytes());
  }

  private String mapToParam(Map<String, Object> map) {
    Set<String> keys = map.keySet();
    StringBuilder builder = new StringBuilder();
    keys.forEach(key ->
        builder.append(key)
            .append("=")
            .append(map.get(key))
            .append("&"));
    builder.deleteCharAt(builder.length() - 1);
    return builder.toString();
  }

  private boolean isBlank(String str) {
    return null == str || "".equals(str.trim());
  }*/
}
