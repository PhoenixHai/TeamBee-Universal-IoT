package cn.universal.plugins.protocolapi.core.service.impl;

import static cn.universal.plugins.protocolapi.core.config.JsonCodeConstants.REQUEST_PATH_CONFLICT;

import cn.universal.core.iot.engine.MagicResourceLoader;
import cn.universal.core.iot.engine.MagicScriptContext;
import cn.universal.core.iot.engine.exception.MagicExitException;
import cn.universal.core.iot.engine.runtime.ExitValue;
import cn.universal.core.iot.engine.runtime.function.MagicScriptLambdaFunction;
import cn.universal.plugins.protocolapi.core.config.MagicConfiguration;
import cn.universal.plugins.protocolapi.core.event.FileEvent;
import cn.universal.plugins.protocolapi.core.event.GroupEvent;
import cn.universal.plugins.protocolapi.core.exception.InvalidArgumentException;
import cn.universal.plugins.protocolapi.core.model.ApiInfo;
import cn.universal.plugins.protocolapi.core.service.AbstractMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.core.service.MagicResourceStorage;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletRequest;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletResponse;
import cn.universal.plugins.protocolapi.core.web.RequestHandler;
import cn.universal.plugins.protocolapi.utils.Mapping;
import cn.universal.plugins.protocolapi.utils.PathUtils;
import cn.universal.plugins.protocolapi.utils.ScriptManager;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class RequestMagicDynamicRegistry extends AbstractMagicDynamicRegistry<ApiInfo> {

  private final Mapping mapping;

  private Object handler;

  private final Method method =
      RequestHandler.class.getDeclaredMethod(
          "invoke",
          MagicHttpServletRequest.class,
          MagicHttpServletResponse.class,
          Map.class,
          Map.class,
          Map.class);

  private static final Logger logger = LoggerFactory.getLogger(RequestMagicDynamicRegistry.class);

  private final boolean allowOverride;

  private final String prefix;

  public RequestMagicDynamicRegistry(
      MagicResourceStorage<ApiInfo> magicResourceStorage,
      Mapping mapping,
      boolean allowOverride,
      String prefix)
      throws NoSuchMethodException {
    super(magicResourceStorage);
    this.mapping = mapping;
    this.allowOverride = allowOverride;
    this.prefix = StringUtils.defaultIfBlank(prefix, "") + "/";
    MagicResourceLoader.addFunctionLoader(this::lookupLambdaFunction);
  }

  private Object lookupLambdaFunction(MagicScriptContext context, String name) {
    int index = name.indexOf(":");
    if (index > -1) {
      String method = name.substring(0, index);
      String path = name.substring(index + 1);
      ApiInfo info =
          getMapping(method.toUpperCase() + ":" + PathUtils.replaceSlash(this.prefix + path));
      if (info != null) {
        String scriptName = MagicConfiguration.getMagicResourceService().getScriptName(info);
        return (MagicScriptLambdaFunction)
            (variables, args) -> {
              MagicScriptContext newContext = new MagicScriptContext();
              Map<String, Object> varMap = new LinkedHashMap<>(context.getRootVariables());
              varMap.putAll(variables.getVariables(context));
              newContext.setScriptName(scriptName);
              newContext.putMapIntoContext(varMap);
              Object value = ScriptManager.executeScript(info.getScript(), newContext);
              if (value instanceof ExitValue) {
                throw new MagicExitException((ExitValue) value);
              }
              return value;
            };
      }
    }
    return null;
  }

  public void setHandler(Object handler) {
    this.handler = handler;
  }

  @EventListener(condition = "#event.type == 'api'")
  public void onFileEvent(FileEvent event) {
    processEvent(event);
  }

  @EventListener(condition = "#event.type == 'api'")
  public void onGroupEvent(GroupEvent event) {
    processEvent(event);
  }

  public ApiInfo getApiInfoFromRequest(MagicHttpServletRequest request) {
    String mappingKey =
        Objects.toString(request.getMethod(), "GET").toUpperCase()
            + ":"
            + request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return getMapping(mappingKey);
  }

  @Override
  public boolean register(MappingNode<ApiInfo> mappingNode) {
    String mappingKey = mappingNode.getMappingKey();
    int index = mappingKey.indexOf(":");
    String requestMethod = mappingKey.substring(0, index);
    String path = mappingKey.substring(index + 1);
    RequestMappingInfo requestMappingInfo =
        mapping.paths(path).methods(RequestMethod.valueOf(requestMethod.toUpperCase())).build();
    if (mapping.getHandlerMethods().containsKey(requestMappingInfo)) {
      if (!allowOverride) {
        logger.error("接口[{}({})]与应用冲突，无法注册", mappingNode.getEntity().getName(), mappingKey);
        throw new InvalidArgumentException(
            REQUEST_PATH_CONFLICT.format(mappingNode.getEntity().getName(), mappingKey));
      }
      logger.warn("取消注册应用接口:{}", requestMappingInfo);
      // 取消注册原接口
      mapping.unregister(requestMappingInfo);
    }
    logger.debug("注册接口[{}({})]", mappingNode.getEntity().getName(), mappingKey);
    mapping.register(requestMappingInfo, handler, method);
    mappingNode.setMappingData(requestMappingInfo);
    return true;
  }

  @Override
  protected void unregister(MappingNode<ApiInfo> mappingNode) {
    logger.debug("取消注册接口[{}({})]", mappingNode.getEntity().getName(), mappingNode.getMappingKey());
    mapping.unregister((RequestMappingInfo) mappingNode.getMappingData());
  }
}
