package cn.universal.plugins.protocolapi.servlet;

import cn.universal.plugins.protocolapi.core.servlet.MagicCookie;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MagicJakartaHttpServletResponse implements MagicHttpServletResponse {

  private final HttpServletResponse response;

  public MagicJakartaHttpServletResponse(HttpServletResponse response) {
    this.response = response;
  }

  @Override
  public void setHeader(String name, String value) {
    response.setHeader(name, value);
  }

  @Override
  public void addHeader(String name, String value) {
    response.addHeader(name, value);
  }

  @Override
  public void sendRedirect(String location) throws IOException {
    response.sendRedirect(location);
  }

  @Override
  public void addCookie(MagicCookie cookie) {
    response.addCookie(cookie.getCookie());
  }

  @Override
  public void setContentType(String contentType) {
    response.setContentType(contentType);
  }

  @Override
  public void setCharacterEncoding(String characterEncoding) {
    response.setCharacterEncoding(characterEncoding);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return response.getOutputStream();
  }

  @Override
  public Collection<String> getHeaderNames() {
    return response.getHeaderNames();
  }

  @Override
  public <T> T getResponse() {
    return (T) response;
  }

  public static class ArgumentsResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return parameter.getParameterType() == MagicHttpServletResponse.class;
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory)
        throws Exception {
      return new MagicJakartaHttpServletResponse(
          webRequest.getNativeResponse(HttpServletResponse.class));
    }
  }
}
