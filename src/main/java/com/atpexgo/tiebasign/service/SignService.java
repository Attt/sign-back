package com.atpexgo.tiebasign.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wudj on 2017/9/28.
 */
@Service
public class SignService {

    private static final Logger log = LoggerFactory.getLogger(SignService.class);

    public Map sign(String bduss) throws Exception {

        CloseableHttpClient client = getClient("bdtb for Android 7.9.2");
        //login
        String tbs = login(client,bduss);

        //get forum
        List<NameValuePair> getForumParams = new ArrayList<>();
        getForumParams.add(new BasicNameValuePair("BDUSS",bduss));
        getForumParams.add(new BasicNameValuePair("_client_id","wappc_1506582322531_89"));
        getForumParams.add(new BasicNameValuePair("_client_type","2"));
        getForumParams.add(new BasicNameValuePair("_client_version","7.9.2"));
        getForumParams.add(new BasicNameValuePair("_phone_imei","030020030096050"));
        getForumParams.add(new BasicNameValuePair("from","mini_baidu_appstore"));
        getForumParams.add(new BasicNameValuePair("like_forum","1"));
        getForumParams.add(new BasicNameValuePair("recommend","0"));
        getForumParams.add(new BasicNameValuePair("timestamp",String.valueOf(System.currentTimeMillis())));
        getForumParams.add(new BasicNameValuePair("topic","0"));

        String postData = getForumParams.stream().map(nameValuePair -> nameValuePair.getName()+"="+nameValuePair.getValue()).collect(StringBuilder::new,StringBuilder::append,StringBuilder::append).toString();

        NameValuePair sig = new BasicNameValuePair("sign",getSign(postData));getForumParams.add(sig);
        HttpUriRequest request = getPostRequest("http://c.tieba.baidu.com/c/f/forum/forumrecommend",getForumParams);

        String resp = getContent("UTF-8",client.execute(request));

        JSONObject respJson = JSONObject.parseObject(resp);
        JSONArray likeForums = respJson.getJSONArray("like_forum");

        for(int i = 0;i<likeForums.size();i++){
            try {
                JSONObject likeforumJson = (JSONObject) likeForums.get(i);
                String name = likeforumJson.getString("forum_name");
                String id = likeforumJson.getString("forum_id");
                String level = likeforumJson.getString("level_id");
                log.info("开始签到{},贴吧id{},当前等级:{}",name,id,level);
//
//                List<NameValuePair> pageParams = new ArrayList<>();
//                pageParams.add(new BasicNameValuePair("BDUSS",bduss));
//                pageParams.add(new BasicNameValuePair("_client_id","wappc_1506582322531_89"));
//                pageParams.add(new BasicNameValuePair("_client_type","2"));
//                pageParams.add(new BasicNameValuePair("_client_version","7.9.2"));
//                pageParams.add(new BasicNameValuePair("_phone_imei","030020030096050"));
//                pageParams.add(new BasicNameValuePair("from","mini_baidu_appstore"));
//                pageParams.add(new BasicNameValuePair("kw",new String(name.getBytes("ISO-8859-1"),"UTF-8")));
//                pageParams.add(new BasicNameValuePair("pn","1"));
//                pageParams.add(new BasicNameValuePair("q_type","2"));
//                pageParams.add(new BasicNameValuePair("rn","25"));
//                pageParams.add(new BasicNameValuePair("scr_dip","96"));
//                pageParams.add(new BasicNameValuePair("scr_h","900"));
//                pageParams.add(new BasicNameValuePair("scr_w","1200"));
//                pageParams.add(new BasicNameValuePair("st_type","tb_forumlist"));
//                pageParams.add(new BasicNameValuePair("timestamp",String.valueOf(System.currentTimeMillis())));
////                pageParams.add(new BasicNameValuePair("with_group","0"));
//
//                String pagePd = pageParams.stream().map(nameValuePair -> nameValuePair.getName()+"="+nameValuePair.getValue()).collect(StringBuilder::new,StringBuilder::append,StringBuilder::append).toString();
//                NameValuePair si = new BasicNameValuePair("sign",getSign(pagePd));pageParams.add(si);
//
//                HttpUriRequest pageRequest = getPostRequest("http://c.tieba.baidu.com/c/f/frs/page",pageParams);
//                String pageResp = getContent("ISO-8859-1",client.execute(pageRequest));
//                JSONObject pageRespJson = JSONObject.parseObject(pageResp);
//                if("0".equals(pageRespJson.getString("error_code"))) {
//                    JSONObject anti = pageRespJson.getJSONObject("anti");
//                    String tbs = anti.getString("tbs");

                    List<NameValuePair> signParams = new ArrayList<>();
                    signParams.add(new BasicNameValuePair("BDUSS", bduss));
                    signParams.add(new BasicNameValuePair("_client_id", "wappc_1506582322531_89"));
                    signParams.add(new BasicNameValuePair("_client_type", "2"));
                    signParams.add(new BasicNameValuePair("_client_version", "7.9.2"));
                    signParams.add(new BasicNameValuePair("_phone_imei", "030020030096050"));
//                    signParams.add(new BasicNameValuePair("fid", id));
                    signParams.add(new BasicNameValuePair("from", "mini_baidu_appstore"));
                    NameValuePair kw = new BasicNameValuePair("kw", name);
                    signParams.add(kw);
                    NameValuePair _tbs = new BasicNameValuePair("tbs", tbs);
                    signParams.add(_tbs);
                    NameValuePair tsp = new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis()));
                    signParams.add(tsp);
                    NameValuePair signn = new BasicNameValuePair("sign", getSign(signParams.stream().map(nameValuePair -> nameValuePair.getName() + "="+nameValuePair.getValue()).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString()));
                    signParams.add(signn);

                    HttpUriRequest signRequest = getPostRequest("http://c.tieba.baidu.com/c/c/forum/sign", signParams);
                    String sigResp = getContent("UTF-8", client.execute(signRequest));
                    JSONObject sigRespJson = JSONObject.parseObject(sigResp);
                    if ("0".equals(sigRespJson.getString("error_code"))) {
                        log.info("{} 签到成功",name);
                    }else if("160002".equals(sigRespJson.getString("error_code"))){
                        log.info("{} 已经签到",name);
                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new HashMap();
    }


    private String login(CloseableHttpClient client,String bduss) throws IOException, NoSuchAlgorithmException {
        List<NameValuePair> loginParams = new ArrayList<>();
        loginParams.add(new BasicNameValuePair("_client_id","wappc_1506582322531_89"));
        loginParams.add(new BasicNameValuePair("_client_type","2"));
        loginParams.add(new BasicNameValuePair("_client_version","7.9.2"));
        loginParams.add(new BasicNameValuePair("_phone_imei","030020030096050"));
        loginParams.add(new BasicNameValuePair("bdusstoken",bduss+"|"));
        loginParams.add(new BasicNameValuePair("channel_id",""));
        loginParams.add(new BasicNameValuePair("channel_uid",""));
        loginParams.add(new BasicNameValuePair("from","mini_baidu_appstore"));
        loginParams.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));

        String postData = loginParams.stream().map(nameValuePair -> nameValuePair.getName()+"="+nameValuePair.getValue()).collect(StringBuilder::new,StringBuilder::append,StringBuilder::append).toString();

        NameValuePair sig = new BasicNameValuePair("sign",getSign(postData));loginParams.add(sig);
        HttpUriRequest request = getPostRequest("http://c.tieba.baidu.com/c/s/login",loginParams);

        String resp = getContent("UTF-8",client.execute(request));

        JSONObject respJson = JSONObject.parseObject(resp);

        if("0".equals(respJson.getString("error_code"))){
            return respJson.getJSONObject("anti").getString("tbs");
        }else {
            return "登录失败";
        }
    }

    private String getContent(String charset, HttpResponse httpResponse) throws IOException {
        String cc =  IOUtils.toString(httpResponse.getEntity().getContent(), charset);
        httpResponse.getEntity().getContent().close();
        return cc;
    }

    /**
     * 获取client
     *
     * @return
     */
    private CloseableHttpClient getClient(String ua) {
        return HttpClientBuilder.create().setUserAgent(ua).build();
    }


    private HttpUriRequest getPostRequest(String url, List<NameValuePair> param) {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
        builder.setProxy(new HttpHost("127.0.0.1", 8888));
        RequestConfig rConfig = builder.build();
        RequestBuilder requestBuilder = RequestBuilder.post(url);
        requestBuilder.setConfig(rConfig);
        requestBuilder.setCharset(Charset.forName("UTF-8"));
        if (param != null) {
            param.forEach(requestBuilder::addParameter);
        }
        HttpUriRequest request =  requestBuilder.build();
        request.addHeader("Accept-Encoding","gzip");
        request.addHeader("Cookie","ka=open");
        request.addHeader("net","3");
        request.addHeader("Content-Type","application/x-www-form-urlencoded");
        request.addHeader("Pragma","no-cache");
        return request;
    }


    /**
     * 计算uwp版的提交sign,需要bduss
     * @return
     */
    private String getSign(String postData) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String originalSign = postData.replaceAll("&","").concat("tiebaclient!!!");
        DigestUtils.md5(originalSign);
        return byteToString(DigestUtils.md5(originalSign));
    }

    private final String[] strDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};


    private String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if(bByte < 0) {
            iRet = bByte + 256;
        }

        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    private String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();

        for(int i = 0; i < bByte.length; ++i) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }

        return sBuffer.toString();
    }
}
