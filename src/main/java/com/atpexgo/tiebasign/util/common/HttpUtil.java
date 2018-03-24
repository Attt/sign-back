package com.atpexgo.tiebasign.util.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wudj on 2017/9/29.
 */

@Slf4j
public class HttpUtil {


    private static Map<String, CloseableHttpClient> HTTP_CLIENT_MAP = new ConcurrentHashMap<>();

    private static PoolingHttpClientConnectionManager manager;

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

            Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslSocketFactory)
                    .build();

            manager = new PoolingHttpClientConnectionManager(reg);
            manager.setDefaultMaxPerRoute(200);
            manager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(60000).build());
            manager.setMaxTotal(200);
        } catch (Exception e) {
            log.warn("warn happened init PoolingHttpClientConnectionManager ", e);
        }

    }

    /**
     * web端client, chrome的UA
     *
     * @return
     */
    public static CloseableHttpClient buildWebClient() {
        // 使用 fiddler 抓包
        // HttpHost proxy = new HttpHost("127.0.0.1", 8888);
        return HttpClientBuilder.create().setRedirectStrategy(new DefaultRedirectStrategy()).setConnectionManager(manager).setUserAgent("Mozilla/5.0 (Wndows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").build();
    }

    /**
     * 移动端client, 百度贴吧手机客户端7.9.2的UA
     *
     * @return
     */
    public static CloseableHttpClient buildCClient() {
        // 使用 fiddler 抓包
        // HttpHost proxy = new HttpHost("127.0.0.1", 8888);
        return HttpClientBuilder.create().setRedirectStrategy(new DefaultRedirectStrategy()).setConnectionManager(manager).setUserAgent("bdtb for Android 7.9.2").build();
    }

    /**
     * 获取client
     *
     * @param ua
     * @return
     */
    private static CloseableHttpClient getClient(String ua) {
        return HttpClientBuilder.create().setUserAgent(ua).setRedirectStrategy(new DefaultRedirectStrategy()).build();
    }

    /**
     * @param url
     * @param param
     * @return
     */
    public static HttpUriRequest getRequest(String url, String param) {
        return getRequest(url, param, Constants.Encoding.UTF_8);
    }

    /**
     * @param url
     * @param param
     * @param charset
     * @return
     */
    public static HttpUriRequest getRequest(String url, String param, String charset) {
        return getRequest(url, param, Charset.forName(charset));
    }

    /**
     * @param url
     * @param param
     * @param charset
     * @return
     */
    public static HttpUriRequest getRequest(String url, String param, Charset charset) {
        RequestBuilder requestBuilder = builderCommonConfig(RequestBuilder.get(url), charset);
        if (param != null)
            requestBuilder.setEntity(new StringEntity(param, ContentType.APPLICATION_FORM_URLENCODED));
        return requestBuilder.build();
    }

    /**
     * @param url
     * @param param
     * @return
     */
    public static HttpUriRequest postRequest(String url, List<NameValuePair> param) {
        return postRequest(url, param, Constants.Encoding.UTF_8);
    }

    /**
     * @param url
     * @param param
     * @param charset
     * @return
     */
    public static HttpUriRequest postRequest(String url, List<NameValuePair> param, String charset) {
        return postRequest(url, param, Charset.forName(charset));
    }

    /**
     * @param url
     * @param param
     * @param charset
     * @return
     */
    public static HttpUriRequest postRequest(String url, List<NameValuePair> param, Charset charset) {
        RequestBuilder requestBuilder = builderCommonConfig(RequestBuilder.post(url), charset);
        if (param != null)
            requestBuilder.addParameters(param.toArray(new NameValuePair[param.size()]));
        return requestBuilder.build();
    }

    private static RequestBuilder builderCommonConfig(RequestBuilder requestBuilder, Charset charset) {
        requestBuilder.setCharset(charset);
        requestBuilder.setConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build());
        return requestBuilder;
    }


    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("SSLv3");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

}
