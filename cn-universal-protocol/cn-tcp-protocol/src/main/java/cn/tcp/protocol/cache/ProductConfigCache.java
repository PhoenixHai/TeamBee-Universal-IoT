package cn.tcp.protocol.cache;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.tcp.protocol.entity.TcpProductConfig;
import cn.tcp.protocol.event.ProductChangedEvent;
import cn.universal.core.iot.constant.IotConstant.ThirdPlatform;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTCertificateMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

@Component
@Slf4j
public class ProductConfigCache {

  private static final String REDIS_KEY = "iot:product:config";
  private final ConcurrentHashMap<String, TcpProductConfig> localCache = new ConcurrentHashMap<>();

  @Autowired
  private IoTProductMapper ioTProductMapper;
  @Autowired
  private StringRedisTemplate redisTemplate;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private IoTCertificateMapper ioTCertificateMapper;

  @PostConstruct
  public void init() {
    reloadAll();
  }

  public TcpProductConfig get(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      log.warn("[ProductConfigCache] get: productKey is empty");
      return null;
    }
    log.debug("[ProductConfigCache] get: productKey={}", productKey);
    TcpProductConfig config = localCache.get(productKey);
    if (config != null) {
      log.debug("[ProductConfigCache] localCache命中: productKey={}, config={}", productKey,
          config);
      return config;
    }
    String json = (String) redisTemplate.opsForHash().get(REDIS_KEY, productKey);
    if (json != null) {
      try {
        config = objectMapper.readValue(json, TcpProductConfig.class);
        localCache.put(productKey, config);
        log.debug("[ProductConfigCache] redis命中: productKey={}, config={}", productKey, config);
        return config;
      } catch (Exception e) {
        log.error("[ProductConfigCache] redis反序列化异常: productKey={}, json={}", productKey,
            json, e);
      }
    }
    IoTProduct product = null;
    try {
      product = ioTProductMapper.getProductByProductKey(productKey);
      log.debug("[ProductConfigCache] DB查找: productKey={}, product={}", productKey, product);
    } catch (Exception e) {
      log.error("[ProductConfigCache] DB查找异常: productKey={}", productKey, e);
    }
    if (product != null) {
      config = toTcpProductConfig(product);
      put(productKey, config);
      log.debug("[ProductConfigCache] DB转config: productKey={}, config={}", productKey, config);
      return config;
    }
    log.warn("[ProductConfigCache] 未找到配置: productKey={}", productKey);
    return null;
  }

  public void put(String productKey, TcpProductConfig config) {
    log.debug("[ProductConfigCache] put: productKey={}, config={}", productKey, config);
    if (productKey == null || config == null) {
      log.warn("[ProductConfigCache] put参数有空: productKey={}, config={}", productKey, config);
      return;
    }
    localCache.put(productKey, config);
    try {
      redisTemplate
          .opsForHash()
          .put(REDIS_KEY, productKey, objectMapper.writeValueAsString(config));
    } catch (Exception e) {
      log.error("[ProductConfigCache] redis写入异常: productKey={}, config={}", productKey, config,
          e);
    }
  }

  public void remove(String productKey) {
    localCache.remove(productKey);
    redisTemplate.opsForHash().delete(REDIS_KEY, productKey);
  }

  public void reloadAll() {
    Example example = new Example(IoTProduct.class);
    example
        .createCriteria()
        .andIn(
            "thirdPlatform",
            List.of(
                ThirdPlatform.tcp.name(), ThirdPlatform.tcp.name(), ThirdPlatform.sniTcp.name()))
        .andEqualTo("state", 0);
    List<IoTProduct> products = ioTProductMapper.selectByExample(example);
    Map<String, TcpProductConfig> tmp = new HashMap<>();
    Map<String, String> redisMap = new HashMap<>();
    for (IoTProduct p : products) {
      TcpProductConfig config = toTcpProductConfig(p);
      if (config == null) {
        continue;
      }
      tmp.put(p.getProductKey(), config);
      try {
        redisMap.put(p.getProductKey(), objectMapper.writeValueAsString(config));
      } catch (Exception ignore) {
      }
    }
    localCache.clear();
    localCache.putAll(tmp);
    redisTemplate.delete(REDIS_KEY);
    if (!redisMap.isEmpty()) {
      redisTemplate.opsForHash().putAll(REDIS_KEY, redisMap);
    }
  }

  @Scheduled(fixedDelay = 5 * 60 * 1000)
  public void scheduledReload() {
    reloadAll();
  }

  @EventListener
  public void onProductChanged(ProductChangedEvent event) {
    switch (event.getType()) {
      case ADD:
      case UPDATE:
        IoTProduct product = ioTProductMapper.getProductByProductKey(event.getProductKey());
        if (product != null) {
          put(event.getProductKey(), toTcpProductConfig(product));
        }
        break;
      case DELETE:
        remove(event.getProductKey());
        break;
    }
  }

  private TcpProductConfig toTcpProductConfig(IoTProduct ioTProduct) {
    try {
      JSONObject configMap = JSONUtil.parseObj(ioTProduct.getConfiguration());
      TcpProductConfig config = BeanUtil.toBean(configMap, TcpProductConfig.class);
      return config;
    } catch (Exception e) {
      log.error("[SNI-TCP] 解析产品配置失败: {}", ioTProduct.getProductKey(), e);
      return null;
    }
  }
}
