import cn.hutool.core.util.HexUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class PersistentTLSTcpClient {

  private static final String HOST = "68831a1f38fc106a8dac3c13.aleo.xin";
  private static final int PORT = 38883;
  private static final String SNI_NAME = "68831a1f38fc106a8dac3c13.aleo.xin";
  private static final String HEX_DATA = "A5A5000101070105DC08F004B07A5A5A";

  public static void main(String[] args) {
    try {
      SSLContext sslContext = createTrustAllContext();
      SSLSocketFactory socketFactory = sslContext.getSocketFactory();

      while (true) { // 外层重连循环
        try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(HOST, PORT)) {
          System.out.println("TCP连接已建立");

          // 设置SNI扩展
          SSLParameters params = new SSLParameters();
          params.setServerNames(java.util.Arrays.asList(new SNIHostName(SNI_NAME)));
          socket.setSSLParameters(params);

          socket.startHandshake();
          System.out.println("TLS握手成功! 协议: " + socket.getSession().getProtocol());

          // 启动独立线程接收响应
          new Thread(new ResponseReceiver(socket)).start();

          // 监听控制台输入，回车自动发送[2,3](@ref)
          try (BufferedReader consoleReader =
              new BufferedReader(new InputStreamReader(System.in))) {
            while (!socket.isClosed()) {
              System.out.print("输入消息 (直接回车发送默认HEX数据，输入'exit'退出): ");
              String input = consoleReader.readLine();

              if ("exit".equalsIgnoreCase(input)) {
                System.out.println("关闭连接...");
                socket.close();
                break;
              }

              // 直接回车时发送预设HEX数据[3](@ref)
              sendHexData(socket.getOutputStream(), HEX_DATA);
            }
          }
        } catch (SSLException e) {
          System.err.println("TLS错误: " + e.getMessage());
        } catch (IOException e) {
          System.err.println("连接异常，5秒后重连...");
          Thread.sleep(5000); // 等待后重连[4](@ref)
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // 发送HEX数据的方法
  private static void sendHexData(OutputStream output, String hexData) throws IOException {
    byte[] hexBytes = HexFormat.of().parseHex(hexData);
    output.write(hexBytes);
    output.flush();
    System.out.println("已发送: " + hexData + " (长度: " + hexBytes.length + "字节)");
  }

  // 响应接收线程[2,4](@ref)
  static class ResponseReceiver implements Runnable {

    private final SSLSocket socket;

    public ResponseReceiver(SSLSocket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (InputStream input = socket.getInputStream()) {
        byte[] buffer = new byte[1024];
        while (!socket.isClosed()) {
          int bytesRead = input.read(buffer); // 阻塞读取[5](@ref)
          if (bytesRead == -1) {
            System.out.println("\n连接已被服务器关闭");
            socket.close();
            break;
          }

          // HEX格式输出
          String hexResponse = HexFormat.of().formatHex(buffer, 0, bytesRead);
          System.out.println("\n收到响应(HEX): " + hexResponse.toUpperCase());

          // 文本格式输出（如果可读）
          String textResponse = HexUtil.decodeHexStr(hexResponse);
          //          System.out.println("文本格式: " + textResponse);
        }
      } catch (IOException e) {
        if (!socket.isClosed()) {
          System.err.println("接收异常: " + e.getMessage());
        }
      }
    }
  }

  // 信任所有证书（仅测试用）[7](@ref)
  private static SSLContext createTrustAllContext()
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
    sslContext.init(
        null,
        new TrustManager[]{
            new X509TrustManager() {
              public X509Certificate[] getAcceptedIssuers() {
                return null;
              }

              public void checkClientTrusted(X509Certificate[] certs, String authType) {
              }

              public void checkServerTrusted(X509Certificate[] certs, String authType) {
              }
            }
        },
        new SecureRandom());
    return sslContext;
  }
}
