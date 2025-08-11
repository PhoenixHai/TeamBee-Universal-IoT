package cn.universal.plugins.protocolapi.starter;

import cn.universal.core.iot.engine.MagicResourceLoader;
import cn.universal.core.iot.engine.MagicScript;
import cn.universal.core.iot.engine.MagicScriptEngine;
import cn.universal.core.iot.engine.exception.MagicScriptRuntimeException;
import cn.universal.core.iot.engine.functions.DynamicModuleImport;
import cn.universal.core.iot.engine.functions.ExtensionMethod;
import cn.universal.core.iot.engine.parsing.ast.statement.AsyncCall;
import cn.universal.core.iot.engine.reflection.JavaReflection;
import cn.universal.plugins.protocolapi.backup.service.MagicBackupService;
import cn.universal.plugins.protocolapi.backup.service.MagicDatabaseBackupService;
import cn.universal.plugins.protocolapi.backup.web.MagicBackupController;
import cn.universal.plugins.protocolapi.core.annotation.MagicModule;
import cn.universal.plugins.protocolapi.core.config.Backup;
import cn.universal.plugins.protocolapi.core.config.Constants;
import cn.universal.plugins.protocolapi.core.config.MagicAPIProperties;
import cn.universal.plugins.protocolapi.core.config.MagicConfiguration;
import cn.universal.plugins.protocolapi.core.config.MagicFunction;
import cn.universal.plugins.protocolapi.core.config.MagicPluginConfiguration;
import cn.universal.plugins.protocolapi.core.config.Resource;
import cn.universal.plugins.protocolapi.core.config.ResponseCode;
import cn.universal.plugins.protocolapi.core.config.Security;
import cn.universal.plugins.protocolapi.core.config.WebSocketSessionManager;
import cn.universal.plugins.protocolapi.core.exception.MagicAPIException;
import cn.universal.plugins.protocolapi.core.handler.MagicCoordinationHandler;
import cn.universal.plugins.protocolapi.core.handler.MagicDebugHandler;
import cn.universal.plugins.protocolapi.core.handler.MagicWebSocketDispatcher;
import cn.universal.plugins.protocolapi.core.handler.MagicWorkbenchHandler;
import cn.universal.plugins.protocolapi.core.interceptor.AuthorizationInterceptor;
import cn.universal.plugins.protocolapi.core.interceptor.DefaultAuthorizationInterceptor;
import cn.universal.plugins.protocolapi.core.interceptor.RequestInterceptor;
import cn.universal.plugins.protocolapi.core.interceptor.ResultProvider;
import cn.universal.plugins.protocolapi.core.logging.LoggerManager;
import cn.universal.plugins.protocolapi.core.model.DataType;
import cn.universal.plugins.protocolapi.core.model.MagicEntity;
import cn.universal.plugins.protocolapi.core.model.Plugin;
import cn.universal.plugins.protocolapi.core.resource.DatabaseResource;
import cn.universal.plugins.protocolapi.core.resource.ResourceAdapter;
import cn.universal.plugins.protocolapi.core.service.MagicAPIService;
import cn.universal.plugins.protocolapi.core.service.MagicDynamicRegistry;
import cn.universal.plugins.protocolapi.core.service.MagicNotifyService;
import cn.universal.plugins.protocolapi.core.service.MagicResourceService;
import cn.universal.plugins.protocolapi.core.service.MagicResourceStorage;
import cn.universal.plugins.protocolapi.core.service.impl.DefaultMagicAPIService;
import cn.universal.plugins.protocolapi.core.service.impl.DefaultMagicResourceService;
import cn.universal.plugins.protocolapi.core.service.impl.RequestMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.core.servlet.MagicRequestContextHolder;
import cn.universal.plugins.protocolapi.core.web.MagicResourceController;
import cn.universal.plugins.protocolapi.core.web.MagicWorkbenchController;
import cn.universal.plugins.protocolapi.core.web.RequestHandler;
import cn.universal.plugins.protocolapi.datasource.model.MagicDynamicDataSource;
import cn.universal.plugins.protocolapi.datasource.service.DataSourceEncryptProvider;
import cn.universal.plugins.protocolapi.datasource.web.MagicDataSourceController;
import cn.universal.plugins.protocolapi.function.service.FunctionMagicDynamicRegistry;
import cn.universal.plugins.protocolapi.jsr223.LanguageProvider;
import cn.universal.plugins.protocolapi.modules.DynamicModule;
import cn.universal.plugins.protocolapi.utils.Mapping;
import cn.universal.plugins.protocolapi.utils.WebUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * magic-api自动配置类
 *
 * @author mxd
 */
@Configuration
@ConditionalOnClass({RequestMappingHandlerMapping.class})
@EnableConfigurationProperties(MagicAPIProperties.class)
@Import({
    MagicServletConfiguration.class,
    MagicJsonAutoConfiguration.class,
    ApplicationUriPrinter.class,
    MagicModuleConfiguration.class,
    MagicDynamicRegistryConfiguration.class
})
@EnableWebSocket
@AutoConfigureAfter(MagicPluginConfiguration.class)
public class MagicAPIAutoConfiguration implements WebMvcConfigurer, WebSocketConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(MagicAPIAutoConfiguration.class);

  /**
   * 请求拦截器
   */
  private final ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider;

  /**
   * 自定义的类型扩展
   */
  private final ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider;

  /**
   * 内置的消息转换
   */
  private final ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider;

  private final ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider;

  /**
   * 自定义的函数
   */
  private final ObjectProvider<List<MagicFunction>> magicFunctionsProvider;

  private final ObjectProvider<List<MagicPluginConfiguration>> magicPluginsProvider;

  private final ObjectProvider<MagicNotifyService> magicNotifyServiceProvider;

  private final ObjectProvider<List<MagicDynamicRegistry<? extends MagicEntity>>>
      magicDynamicRegistriesProvider;

  private final ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>>
      magicResourceStoragesProvider;

  private final ObjectProvider<DataSourceEncryptProvider> dataSourceEncryptProvider;

  private final MagicAPIProperties properties;

  private final ApplicationContext applicationContext;

  private boolean registerMapping = false;

  private boolean registerWebsocket = false;

  @Autowired
  @Lazy
  private RequestMappingHandlerMapping requestMappingHandlerMapping;

  public MagicAPIAutoConfiguration(
      MagicAPIProperties properties,
      ObjectProvider<List<RequestInterceptor>> requestInterceptorsProvider,
      ObjectProvider<List<ExtensionMethod>> extensionMethodsProvider,
      ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConvertersProvider,
      ObjectProvider<List<MagicFunction>> magicFunctionsProvider,
      ObjectProvider<List<MagicPluginConfiguration>> magicPluginsProvider,
      ObjectProvider<MagicNotifyService> magicNotifyServiceProvider,
      ObjectProvider<AuthorizationInterceptor> authorizationInterceptorProvider,
      ObjectProvider<DataSourceEncryptProvider> dataSourceEncryptProvider,
      ObjectProvider<List<MagicDynamicRegistry<? extends MagicEntity>>>
          magicDynamicRegistriesProvider,
      ObjectProvider<List<MagicResourceStorage<? extends MagicEntity>>>
          magicResourceStoragesProvider,
      ApplicationContext applicationContext) {
    this.properties = properties;
    this.requestInterceptorsProvider = requestInterceptorsProvider;
    this.extensionMethodsProvider = extensionMethodsProvider;
    this.httpMessageConvertersProvider = httpMessageConvertersProvider;
    this.magicFunctionsProvider = magicFunctionsProvider;
    this.magicPluginsProvider = magicPluginsProvider;
    this.magicNotifyServiceProvider = magicNotifyServiceProvider;
    this.authorizationInterceptorProvider = authorizationInterceptorProvider;
    this.dataSourceEncryptProvider = dataSourceEncryptProvider;
    this.magicDynamicRegistriesProvider = magicDynamicRegistriesProvider;
    this.magicResourceStoragesProvider = magicResourceStoragesProvider;
    this.applicationContext = applicationContext;
  }

  @Bean
  @ConditionalOnMissingBean(cn.universal.plugins.protocolapi.core.resource.Resource.class)
  @ConditionalOnProperty(prefix = "magic-api", name = "resource.type", havingValue = "database")
  public cn.universal.plugins.protocolapi.core.resource.Resource magicDatabaseResource(
      MagicDynamicDataSource magicDynamicDataSource) {
    Resource resourceConfig = properties.getResource();
    if (magicDynamicDataSource.isEmpty()) {
      throw new MagicAPIException(
          "当前未配置数据源，如已配置，请引入 spring-boot-starter-jdbc 后在试!");
    }
    MagicDynamicDataSource.DataSourceNode dataSourceNode =
        magicDynamicDataSource.getDataSource(resourceConfig.getDatasource());
    return new DatabaseResource(
        new JdbcTemplate(dataSourceNode.getDataSource()),
        resourceConfig.getTableName(),
        resourceConfig.getPrefix(),
        resourceConfig.isReadonly());
  }

  @Bean
  @ConditionalOnMissingBean(cn.universal.plugins.protocolapi.core.resource.Resource.class)
  @ConditionalOnProperty(
      prefix = "magic-api",
      name = "resource.type",
      havingValue = "file",
      matchIfMissing = true)
  public cn.universal.plugins.protocolapi.core.resource.Resource magicResource()
      throws IOException {
    Resource resourceConfig = properties.getResource();
    return ResourceAdapter.getResource(resourceConfig.getLocation(), resourceConfig.isReadonly());
  }

  @Bean
  @ConditionalOnMissingBean(MagicBackupService.class)
  @ConditionalOnProperty(prefix = "magic-api", name = "backup.enable", havingValue = "true")
  public MagicBackupService magicDatabaseBackupService(
      MagicDynamicDataSource magicDynamicDataSource) {
    Backup backupConfig = properties.getBackup();
    MagicDynamicDataSource.DataSourceNode dataSourceNode =
        magicDynamicDataSource.getDataSource(backupConfig.getDatasource());
    return new MagicDatabaseBackupService(
        new JdbcTemplate(dataSourceNode.getDataSource()), backupConfig.getTableName());
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String web = properties.getWeb();
    if (web != null && !registerMapping) {
      registerMapping = true;
      // 当开启了UI界面时，收集日志
      LoggerManager.createMagicAppender();
      // 配置静态资源路径
      registry.addResourceHandler(web + "/**").addResourceLocations("classpath:/magic-editor/");
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public MagicResourceService magicResourceService(
      cn.universal.plugins.protocolapi.core.resource.Resource workspace) {
    return new DefaultMagicResourceService(
        workspace, magicResourceStoragesProvider.getObject(), applicationContext);
  }

  @Bean
  @ConditionalOnMissingBean(MagicNotifyService.class)
  public MagicNotifyService magicNotifyService() {
    logger.info(
        "未配置集群通知服务，本实例不会推送通知，集群环境下可能会有问题，如需开启，请引用magic-api-plugin-cluster插件");
    return magicNotify -> {
    };
  }

  /**
   * 注入API调用Service
   */
  @Bean
  @ConditionalOnMissingBean
  public MagicAPIService magicAPIService(
      ResultProvider resultProvider,
      MagicResourceService magicResourceService,
      MagicRequestContextHolder magicRequestContextHolder,
      RequestMagicDynamicRegistry requestMagicDynamicRegistry,
      FunctionMagicDynamicRegistry functionMagicDynamicRegistry) {
    WebUtils.magicRequestContextHolder = magicRequestContextHolder;
    return new DefaultMagicAPIService(
        resultProvider,
        properties.getInstanceId(),
        magicResourceService,
        requestMagicDynamicRegistry,
        functionMagicDynamicRegistry,
        properties.isThrowException(),
        properties.getPrefix(),
        magicRequestContextHolder,
        applicationContext);
  }

  /**
   * 注册模块、类型扩展
   */
  private void setupMagicModules(
      List<ExtensionMethod> extensionMethods, List<LanguageProvider> languageProviders) {
    // 设置脚本import时 class加载策略
    MagicResourceLoader.setClassLoader(
        (className) -> {
          try {
            return applicationContext.getBean(className);
          } catch (Exception e) {
            Class<?> clazz = null;
            try {
              clazz = Class.forName(className);
              return applicationContext.getBean(clazz);
            } catch (Exception ex) {
              if (clazz == null) {
                throw new MagicScriptRuntimeException(new ClassNotFoundException(className));
              }
              return clazz;
            }
          }
        });
    MagicResourceLoader.addScriptLanguageLoader(
        language ->
            languageProviders.stream()
                .filter(it -> it.support(language))
                .findFirst()
                .<BiFunction<Map<String, Object>, String, Object>>map(
                    languageProvider ->
                        (context, script) -> {
                          try {
                            return languageProvider.execute(language, script, context);
                          } catch (Exception e) {
                            throw new MagicAPIException(e.getMessage(), e);
                          }
                        })
                .orElse(null));
    logger.info("注册模块:{} -> {}", "log", Logger.class);
    MagicResourceLoader.addModule(
        "log",
        new DynamicModuleImport(
            Logger.class,
            context ->
                LoggerFactory.getLogger(Objects.toString(context.getScriptName(), "Unknown"))));
    List<String> importModules = properties.getAutoImportModuleList();
    applicationContext
        .getBeansWithAnnotation(MagicModule.class)
        .values()
        .forEach(
            module -> {
              String moduleName =
                  AnnotationUtils.findAnnotation(module.getClass(), MagicModule.class).value();
              logger.info("注册模块:{} -> {}", moduleName, module.getClass());
              if (module instanceof DynamicModule) {
                MagicResourceLoader.addModule(
                    moduleName,
                    new DynamicModuleImport(
                        module.getClass(), ((DynamicModule<?>) module)::getDynamicModule));
              } else {
                MagicResourceLoader.addModule(moduleName, module);
              }
            });
    MagicResourceLoader.getModuleNames().stream()
        .filter(importModules::contains)
        .forEach(
            moduleName -> {
              logger.info("自动导入模块：{}", moduleName);
              MagicScriptEngine.addDefaultImport(
                  moduleName, MagicResourceLoader.loadModule(moduleName));
            });
    properties
        .getAutoImportPackageList()
        .forEach(
            importPackage -> {
              logger.info("自动导包：{}", importPackage);
              MagicResourceLoader.addPackage(importPackage);
            });
    extensionMethods.forEach(
        extension ->
            extension
                .supports()
                .forEach(
                    support -> {
                      logger.info("注册扩展:{} -> {}", support, extension.getClass());
                      JavaReflection.registerMethodExtension(support, extension);
                    }));
  }

  @Bean
  public MagicConfiguration magicConfiguration(
      List<LanguageProvider> languageProviders,
      cn.universal.plugins.protocolapi.core.resource.Resource magicResource,
      ResultProvider resultProvider,
      MagicResourceService magicResourceService,
      MagicAPIService magicAPIService,
      MagicNotifyService magicNotifyService,
      RequestMagicDynamicRegistry requestMagicDynamicRegistry,
      @Autowired(required = false) MagicBackupService magicBackupService)
      throws NoSuchMethodException {
    logger.info("magic-api工作目录:{}", magicResource);
    AsyncCall.setThreadPoolExecutorSize(properties.getThreadPoolExecutorSize());
    DataType.DATE_PATTERNS = properties.getDatePattern();
    MagicScript.setCompileCache(properties.getCompileCacheSize());
    // 设置响应结果的code值
    ResponseCode responseCodeConfig = properties.getResponseCode();
    Constants.RESPONSE_CODE_SUCCESS = responseCodeConfig.getSuccess();
    Constants.RESPONSE_CODE_INVALID = responseCodeConfig.getInvalid();
    Constants.RESPONSE_CODE_EXCEPTION = responseCodeConfig.getException();
    // 设置模块和扩展方法
    setupMagicModules(
        extensionMethodsProvider.getIfAvailable(Collections::emptyList), languageProviders);
    MagicConfiguration configuration = new MagicConfiguration();
    configuration.setMagicAPIService(magicAPIService);
    configuration.setMagicNotifyService(magicNotifyService);
    configuration.setInstanceId(properties.getInstanceId());
    configuration.setMagicResourceService(magicResourceService);
    configuration.setMagicDynamicRegistries(magicDynamicRegistriesProvider.getObject());
    configuration.setMagicBackupService(magicBackupService);
    Security security = properties.getSecurity();
    configuration.setDebugTimeout(properties.getDebug().getTimeout());
    configuration.setHttpMessageConverters(
        httpMessageConvertersProvider.getIfAvailable(Collections::emptyList));
    configuration.setResultProvider(resultProvider);
    configuration.setThrowException(properties.isThrowException());
    configuration.setEditorConfig(properties.getEditorConfig());
    configuration.setWorkspace(magicResource);
    configuration.setAuthorizationInterceptor(authorizationInterceptorProvider.getObject());
    // 注册函数
    this.magicFunctionsProvider
        .getIfAvailable(Collections::emptyList)
        .forEach(JavaReflection::registerFunction);
    // 向页面传递配置信息时不传递用户名密码，增强安全性
    security.setUsername(null);
    security.setPassword(null);
    requestMagicDynamicRegistry.setHandler(
        new RequestHandler(configuration, requestMagicDynamicRegistry));
    List<MagicPluginConfiguration> pluginConfigurations =
        magicPluginsProvider.getIfAvailable(Collections::emptyList);
    List<Plugin> plugins =
        pluginConfigurations.stream()
            .map(MagicPluginConfiguration::plugin)
            .collect(Collectors.toList());
    // 构建UI请求处理器
    String base = properties.getWeb();
    Mapping mapping = Mapping.create(requestMappingHandlerMapping, base);
    MagicWorkbenchController magicWorkbenchController =
        new MagicWorkbenchController(configuration, properties, plugins);
    if (base != null) {
      configuration.setEnableWeb(true);
      mapping
          .registerController(magicWorkbenchController)
          .registerController(new MagicResourceController(configuration))
          .registerController(new MagicDataSourceController(configuration))
          .registerController(new MagicBackupController(configuration));
      pluginConfigurations.forEach(it -> it.controllerRegister().register(mapping, configuration));
    }
    // 注册接收推送的接口
    if (StringUtils.isNotBlank(properties.getSecretKey())) {
      mapping.register(
          mapping.paths(properties.getPushPath()).methods(RequestMethod.POST).build(),
          magicWorkbenchController,
          MagicWorkbenchController.class.getDeclaredMethod(
              "receivePush", MultipartFile.class, String.class, Long.class, String.class));
    }
    // 设置拦截器信息
    this.requestInterceptorsProvider
        .getIfAvailable(Collections::emptyList)
        .forEach(
            interceptor -> {
              logger.info("注册请求拦截器：{}", interceptor.getClass());
              configuration.addRequestInterceptor(interceptor);
            });
    // 打印banner
    if (this.properties.isBanner()) {
      configuration.printBanner(plugins.stream().map(Plugin::getName).collect(Collectors.toList()));
    }
    if (magicBackupService == null) {
      logger.error("当前备份设置未配置，强烈建议配置备份设置，以免代码丢失。");
    }
    // 备份清理
    if (properties.getBackup().isEnable()
        && properties.getBackup().getMaxHistory() > 0
        && magicBackupService != null) {
      long interval = properties.getBackup().getMaxHistory() * 86400000L;
      // 1小时执行1次
      new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "magic-api-clean-task"))
          .scheduleAtFixedRate(
              () -> {
                try {
                  long count =
                      magicBackupService.removeBackupByTimestamp(
                          System.currentTimeMillis() - interval);
                  if (count > 0) {
                    logger.info("已删除备份记录{}条", count);
                  }
                } catch (Exception e) {
                  logger.error("删除备份记录时出错", e);
                }
              },
              1,
              1,
              TimeUnit.HOURS);
    }
    return configuration;
  }

  @Bean
  @ConditionalOnMissingBean
  public AuthorizationInterceptor authorizationInterceptor(MagicAPIProperties properties) {
    Security security = properties.getSecurity();
    return new DefaultAuthorizationInterceptor(security.getUsername(), security.getPassword());
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
    String web = properties.getWeb();
    MagicNotifyService magicNotifyService = magicNotifyServiceProvider.getObject();
    WebSocketSessionManager.setMagicNotifyService(magicNotifyService);
    if (web != null && !registerWebsocket) {
      registerWebsocket = true;
      MagicWebSocketDispatcher dispatcher =
          new MagicWebSocketDispatcher(
              properties.getInstanceId(),
              magicNotifyService,
              Arrays.asList(
                  new MagicDebugHandler(),
                  new MagicCoordinationHandler(),
                  new MagicWorkbenchHandler(authorizationInterceptorProvider.getObject())));
      WebSocketHandlerRegistration registration =
          webSocketHandlerRegistry.addHandler(dispatcher, web + "/console");
      if (properties.isSupportCrossDomain()) {
        registration.setAllowedOrigins("*");
      }
    }
  }
}
