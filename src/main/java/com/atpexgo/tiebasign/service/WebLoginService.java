package com.atpexgo.tiebasign.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by wudj on 2017/9/28.
 */
@Deprecated
@Service
public class WebLoginService {

    private static Map<String, HttpClientContext> contextMap = new ConcurrentHashMap<>();

    public String login(String account, String password) throws Exception {

        HttpClientContext context = getContext(account);
        Triple<String, String, String> preparedInfo = prepare(context);
        String reCode = login(account, password,preparedInfo.getLeft(),preparedInfo.getMiddle(),preparedInfo.getRight(), context);//尝试登录
        if ("0".equals(reCode)) {
//                System.out.println("Cookie:");
            List<String> bdussList = context.getCookieStore().getCookies().stream().filter(cookie -> "BDUSS".equals(cookie.getName())).map(Cookie::getValue).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bdussList)){
                return "登录失败";
            }else {
                return bdussList.get(0);
            }
        } else {
            return "登录失败";
        }
//            if ("3".equals(reCode) || "6".equals(reCode) || "257".equals(reCode)) {
//                getVerifyImg();//获取验证码
//                System.out.println("verify code:");
//                BufferedReader reader3 = new BufferedReader(new InputStreamReader(System.in));
//                String code = reader3.readLine();
//                if (!LoginAndCheckValCode(code)) {//检查验证码
//                    /* 重试 */
//                    System.out.println("retry? \n 1)retry\n 2)quit\n other)quit");
//                    BufferedReader reader4 = new BufferedReader(new InputStreamReader(System.in));
//                    String i = reader4.readLine();
//                    if ("1".equals(i)) {
//                        reGetCodestr();//重新获取验证字符串
//                        System.out.println("retrying...");
//                    } else {
//                        System.out.println("bye)");
//                        break;
//                    }
//                }
//            }
    }

    private HttpClientContext getContext(String account) {
        if (contextMap.containsKey(account)) {
            return contextMap.get(account);
        } else {
            HttpClientContext context = createContext();
            contextMap.put(account, context);
            return context;
        }
    }

    private HttpClientContext createContext() {
        return HttpClientContext.create();
    }


    /**
     * 获取client
     *
     * @return
     */
    private CloseableHttpClient getClient(String ua) {
        return HttpClientBuilder.create().setUserAgent(ua).build();
    }

    private HttpUriRequest getGetRequest(String url, String param) {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setCookieSpec(CookieSpecs.DEFAULT);
        RequestConfig rConfig = builder.build();
        RequestBuilder requestBuilder = RequestBuilder.get(url);
        requestBuilder.setCharset(Charset.forName("UTF-8"));
        requestBuilder.setConfig(rConfig);
        if (param != null) {
            StringEntity entity = new StringEntity(param, ContentType.APPLICATION_FORM_URLENCODED);
            requestBuilder.setEntity(entity);
        }
        return requestBuilder.build();
    }


    private HttpUriRequest getPostRequest(String url, List<NameValuePair> param) {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setCookieSpec(CookieSpecs.DEFAULT);
        RequestConfig rConfig = builder.build();
        RequestBuilder requestBuilder = RequestBuilder.post(url);
        requestBuilder.setCharset(Charset.forName("UTF-8"));
        requestBuilder.setConfig(rConfig);
        if (param != null) {
            param.forEach(requestBuilder::addParameter);
        }
        return requestBuilder.build();
    }

    private String getCallback(String prefix) {

        return prefix + Integer.toString((int) Math.floor(2147483648L * Math.random()), 36);
    }

    /**
     * gid
     * this.guideRandom = function() {
     * return "xxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function(e) {
     * var t = 16 * Math.random() | 0
     * , n = "x" == e ? t : 3 & t | 8;
     * return n.toString(16)
     * }).toUpperCase()
     * }()
     *
     * @return
     */
    private String getGid() {
        String platString = "xxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";

        return platString.chars().mapToObj(c -> {
            String d = String.valueOf((char) c);
            int t = (int) (16 * Math.random()) | 0;
            if (c == 'x') {
                d = Integer.toHexString(t).toUpperCase();
            } else if (c == 'y') {
                d = Integer.toHexString(3 & t | 8).toUpperCase();
            }
            return d;
        }).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    private String getContent(String charset, HttpResponse httpResponse) throws IOException {
        return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
    }

    private BufferedImage getImg(HttpResponse httpResponse) throws IOException {
        return ImageIO.read(httpResponse.getEntity().getContent());
    }

    private Triple<String, String, String> prepare(HttpClientContext context) throws IOException {

        // get TOKEN
        getClient("Mozilla/5.0 (Wndows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest("https://www.baidu.com", null), context);

//        List<String> fps = context.getCookieStore().getCookies().stream().filter(cookie -> "FP_UID".equals(cookie.getName())).map(Cookie::getValue).collect(Collectors.toList());
//        if (!CollectionUtils.isEmpty(fps)) {
//            fpUid = fps.get(0);
//            System.out.println("FP_UID is " + fpUid);
//        }

        long time = System.currentTimeMillis();
        String _callback = getCallback("bd__cbs__");
        String _gid = getGid();
        String fetchToken = "https://passport.baidu.com/v2/api/?getapi&tpl=netdisk&subpro=netdisk_web&apiver=v3&tt=" + time + "&class=login&gid=" + _gid + "&logintype=basicLogin&callback=" + _callback;

        String resultJsonString = getContent("UTF-8", getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest(fetchToken, null), context));

        JSONObject result = readJsonObjectFromJsonp(resultJsonString, _callback);

        String token = result.getJSONObject("data").getString("token");


        // getRsakey
        _callback = getCallback("bd__cbs__");
        time = System.currentTimeMillis();
        String fetchRsakey = "https://passport.baidu.com/v2/getpublickey?token=" + token + "&tpl=netdisk&subpro=netdisk_web&apiver=v3&tt=" + time + "&gid=" + getGid() + "&callback=" + _callback;

        resultJsonString = getContent("UTF-8", getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest(fetchRsakey, null), context));

        result = readJsonObjectFromJsonp(resultJsonString, _callback);


        String rsakey = result.getString("key");
        String pubkey = result.getString("pubkey");

        return new ImmutableTriple<>(pubkey, rsakey, token);

    }


//    private void reGetCodestr(HttpClientContext context) throws IOException {
//        //regetcodestr
//        String _callback = getCallback("bd__cbs__");
//        String fetchVerifySign = "https://passport.baidu.com/v2/?reggetcodestr&apiver=v3&callback=" + _callback + "&fr=login&token=" + token + "&tpl=netdisk&tt=" + String.valueOf(System.currentTimeMillis());
//        String resultJsonString = getContent("UTF-8", getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest(fetchVerifySign, null), context));
//        JSONObject result = readJsonObjectFromJsonp(resultJsonString, _callback);
//        verifyStr = result.getJSONObject("data").getString("verifyStr");
//    }
//
//    private void getVerifyImg(HttpClientContext context) throws IOException {
//        //getVerifyImg
//        String fetchImg = "https://passport.baidu.com/cgi-bin/genimage?" + verifyStr;
//
//        BufferedImage verifyImg = getImg(getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest(fetchImg, null), context));
//
//        verifyImg = createAsciiPic(verifyImg);
//
//        ImageIO.write(verifyImg, "jpg", new File("D:\\baidu验证码\\验证码.jpg"));
//    }
//
//    private BufferedImage createAsciiPic(final BufferedImage image) {
//        final String base = "@#&$%*o!;.";
//        for (int y = 0; y < image.getHeight(); y += 2) {
//            for (int x = 0; x < image.getWidth(); x++) {
//                final int pixel = image.getRGB(x, y);
//                final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
//                final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
//                final int index = Math.round(gray * (base.length() + 1) / 255);
//                System.out.print(index >= base.length() ? " " : String.valueOf(base.charAt(index)));
//            }
//            System.out.println();
//        }
//        return image;
//    }
//
//
//    private boolean LoginAndCheckValCode(String code, HttpClientContext context) throws Exception {
//        String _callback = getCallback("bd__cbs__");
//        String checkCode = "https://passport.baidu.com/v2/?checkvcode&token=" + token + "&tpl=netdisk&subpro=netdisk_web&apiver=v3&tt=" + String.valueOf(System.currentTimeMillis()) + "&verifycode=" + code + "&codestring=" + verifyStr + "&callback=" + _callback;
//        String resultJsonString = getContent("UTF-8", getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getGetRequest(checkCode, null), context));
//        JSONObject result = readJsonObjectFromJsonp(resultJsonString, _callback);
//        String errinfo = result.getJSONObject("errInfo").getString("no");
//        if ("0".equals(errinfo)) {
//            valCode = code;
//            return true;
//        } else {
//            valCode = null;
//            return false;
//        }
//    }

    private String login(String account, String password, String pubkey, String rsakey, String token, HttpClientContext context) throws Exception {

        //login

        String login = "https://passport.baidu.com/v2/api/?login";
        String _callback = getCallback("parent.bd__pcbs__");

        List<NameValuePair> param = new ArrayList<>();
        NameValuePair apiver = new BasicNameValuePair("apiver", "v3");
        param.add(apiver);
        NameValuePair staticpage = new BasicNameValuePair("staticpage", "https://passport.baidu.com/static/passpc-account/html/v3Jump.html");
        param.add(staticpage);
        NameValuePair charset = new BasicNameValuePair("charset", "UTF-8");
        param.add(charset);
        NameValuePair _token = new BasicNameValuePair("token", token);
        param.add(_token);
        NameValuePair tpl = new BasicNameValuePair("tpl", "netdisk");
        param.add(tpl);
        NameValuePair _subpro = new BasicNameValuePair("subpro", "netdisk_web");
        param.add(_subpro);
        NameValuePair _gid = new BasicNameValuePair("gid", getGid());
        param.add(_gid);
        NameValuePair tt = new BasicNameValuePair("tt", String.valueOf(System.currentTimeMillis()));
        param.add(tt);
        NameValuePair codestring = new BasicNameValuePair("codestring", "");
        param.add(codestring);

        NameValuePair safeflg = new BasicNameValuePair("safeflg", "0");
        param.add(safeflg);
        NameValuePair u = new BasicNameValuePair("u", "https://pan.baidu.com/disk/home");
        param.add(u);
        NameValuePair isPhone = new BasicNameValuePair("isPhone", "");
        param.add(isPhone);
        NameValuePair detect = new BasicNameValuePair("detect", "1");
        param.add(detect);
        NameValuePair quick_user = new BasicNameValuePair("quick_user", "0");
        param.add(quick_user);
        NameValuePair logintype = new BasicNameValuePair("logintype", "basicLogin");
        param.add(logintype);
        NameValuePair logLoginType = new BasicNameValuePair("logLoginType", "pc_loginBasic");
        param.add(logLoginType);
        NameValuePair _idc = new BasicNameValuePair("idc", "");
        param.add(_idc);
        NameValuePair loginmerge = new BasicNameValuePair("loginmerge", "true");
        param.add(loginmerge);
        NameValuePair foreignusername = new BasicNameValuePair("foreignusername", "");
        param.add(foreignusername);
        NameValuePair _username = new BasicNameValuePair("username", account);
        param.add(_username);
        NameValuePair _password = new BasicNameValuePair("password", new String(Base64Utils.encode(encryptByPublicKey(password.getBytes(), pubkey))));
        param.add(_password);
        NameValuePair mem_pass = new BasicNameValuePair("mem_pass", "on");
        param.add(mem_pass);
        NameValuePair _rsakey = new BasicNameValuePair("rsakey", rsakey);
        param.add(_rsakey);
        NameValuePair crypttype = new BasicNameValuePair("crypttype", "12");
        param.add(crypttype);
        NameValuePair _ppui_logintime = new BasicNameValuePair("ppui_logintime", String.valueOf(3467902L));
        param.add(_ppui_logintime);
        NameValuePair countrycode = new BasicNameValuePair("countrycode", "");
        param.add(countrycode);

        /* FIXME 百度登录验证改版->增加了fp_info/fp_uid/dv参数,获取方法未知,此处写死,但并没有卵用
         *
         */
//        NameValuePair _fpUid = new BasicNameValuePair("fp_uid", fpUid);
//        param.add(_fpUid);
//        NameValuePair fpInfo = new BasicNameValuePair("fp_info", fpUid + "002~~~" + blades);
//        param.add(fpInfo);
//        NameValuePair _dv = new BasicNameValuePair("dv", dv);
//        param.add(_dv);
        NameValuePair callback = new BasicNameValuePair("callback", _callback);
        param.add(callback);


        String resultString = getContent("UTF-8", getClient("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36").execute(getPostRequest(login, param), context));
        String returnCode = readErrNoFromLoginReturn(resultString);
//        String codeStr = readCodeStrFromLoginReturn(resultString);
//        String msg = getDescribe(returnCode);
        return returnCode;
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥
     * @return
     * @throws Exception
     */
    private byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {

        StringReader stringReader = new StringReader(publicKey);
        PemReader reader = new PemReader(stringReader);
        PemObject pemObject = reader.readPemObject();

        Key publicK = new RSAPublicKeyImpl(pemObject.getContent());
        // 对数据加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 117) {
                cache = cipher.doFinal(data, offSet, 117);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 117;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    private JSONObject readJsonObjectFromJsonp(String str, String callbackName) {
        return JSONObject.parseObject(str.replaceAll("\\)", "").replaceAll(callbackName + "\\(", ""));
    }

    private String readErrNoFromLoginReturn(String str) {
        str = str.replaceAll("([\\s\\S]*)err_no=", "");
        str = str.replaceAll("&callback=parent([\\s\\S]*)", "");
        return str;
    }

    private String readCodeStrFromLoginReturn(String str) {
        str = str.replaceAll("([\\s\\S]*)&codeString=", "");
        str = str.replaceAll("&userName=([\\s\\S]*)", "");
        return str;
    }


//    private String getDescribe(String code) {
//        if ("0".equals(code))
//            return "登录成功";
//        if (!errInfo.containsKey(code))
//            return "未知错误";
//        JSONObject info = errInfo.getJSONObject(code);
//        return info.toJSONString();
//    }

}
