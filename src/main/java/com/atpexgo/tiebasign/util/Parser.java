package com.atpexgo.tiebasign.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.atpexgo.tiebasign.util.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.springframework.util.CollectionUtils;
import us.codecraft.xsoup.Xsoup;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wudj on 2017/9/29.
 */
@Slf4j
public class Parser {

    public static Invocable invoke;

    static {
        try {
            ClassLoader classLoader = Test.class.getClassLoader();
            URL resource = classLoader.getResource("static/baidupassEncrypt.js");
            String path = resource.getPath();
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName( "js" );
            FileReader reader = new FileReader(path);
            engine.eval(reader);
            invoke = (Invocable) engine;
        }catch (Exception e){
            log.warn("JS 加载失败",e);
        }
    }

    public static String toString(HttpResponse httpResponse) throws IOException {
        return IOUtils.toString(httpResponse.getEntity().getContent(), Constants.Encoding.UTF_8);
    }

    public static String toString(HttpResponse httpResponse, String charset) throws IOException {
        return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
    }

    public static String toString(HttpResponse httpResponse, Charset charset) throws IOException {
        return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
    }

    public static JSONObject toJsonObject(String content) throws IOException {
        return JSONObject.parseObject(content);
    }

    public static JSONArray toJsonArray(String content) throws IOException {
        return JSONArray.parseArray(content);
    }

    public static Object readJsonPath(String content, String jsonPath) throws IOException {
        Object object = JSON.parse(content);
        JSONPath jsonpath = JSONPath.compile(jsonPath);
        return jsonpath.eval(object);
    }

    public static String readXmlPath(String content,String xpath){
        return Xsoup.select(content,xpath).get();
    }

    public static String readCookieAnyMatch(HttpClientContext context, String cookieKey) {
        List<String> cookieValues = context.getCookieStore().getCookies().stream().filter(cookie -> cookieKey.equals(cookie.getName())).map(Cookie::getValue).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cookieValues)) {
            return cookieValues.get(0);
        }
        return null;
    }

    public static String readFromHtml(String content,String prefix,String suffix) throws IOException {
        return content.replaceAll("([\\s\\S]*)"+prefix,"").replaceAll(suffix+"([\\s\\S]*)","");
    }

    public static List<String> readCookieAllMatch(HttpClientContext context, String cookieKey) {
        return context.getCookieStore().getCookies().stream().filter(cookie -> cookieKey.equals(cookie.getName())).map(Cookie::getValue).collect(Collectors.toList());
    }

    public static BufferedImage readBufferedImage(HttpResponse response) throws IOException {
        return ImageIO.read(response.getEntity().getContent());
    }

    public static String encryptPassword(String password,String serverTime) throws ScriptException, NoSuchMethodException {
        return (String) invoke.invokeFunction("getPass",password,serverTime);
    }
}
