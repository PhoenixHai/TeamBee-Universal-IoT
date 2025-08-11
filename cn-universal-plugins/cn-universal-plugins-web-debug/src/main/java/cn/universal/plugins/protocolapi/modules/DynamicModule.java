package cn.universal.plugins.protocolapi.modules;

import cn.universal.core.iot.engine.MagicScriptContext;
import java.beans.Transient;

public interface DynamicModule<T> {

  @Transient
  T getDynamicModule(MagicScriptContext context);
}
