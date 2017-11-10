package com.atpexgo.tiebasign.service;

import com.atpexgo.tiebasign.util.Parser;
import com.atpexgo.tiebasign.util.common.Constants;
import com.atpexgo.tiebasign.util.common.EncryptUtil;
import com.atpexgo.tiebasign.util.common.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * 百度登录
 * Created by wudj on 2017/9/29.
 */
@Service
public class LoginService {


    public Map<String,Object> wiseuid(CloseableHttpClient client, HttpClientContext context) throws IOException {
        Map<String,Object> result = new HashMap<>();
        HttpResponse response = client.execute(HttpUtil.getRequest(Constants.Api.wiseUid(), null), context);
        String content = Parser.toString(response);
        String bdcm = Parser.readFromHtml(content, "\"bdcm\" : \"", "\",\\s+\"token\" ");
        String uid = Parser.readFromHtml(content, "\"uid\" : \"", "\",\\s+\"type\" ");
        String wiseid = Parser.readCookieAnyMatch(context,Constants.CookieKey.BAIDU_WISE_UID);
        result.put("bdcm",bdcm);
        result.put("uid",uid);
        result.put("wiseid",wiseid);
        return result;
    }


    public Map<String,Object> antiplaytoken(CloseableHttpClient client, HttpClientContext context) throws IOException {
        String content = Parser.toString(client.execute(HttpUtil.getRequest(Constants.Api.antiToken(), null), context));
        String serverTime = (String) Parser.readJsonPath(content, Constants.JsonPath.ANTI_TOKEN_TIME_JSONPATH);
        Map<String,Object> result = new HashMap<>();
        result.put("errno",Parser.readJsonPath(content,"$.errInfo.no"));
        result.put("msg",Parser.readJsonPath(content,"$.errInfo.msg"));
        result.put("serverTime",serverTime);
        return result;
    }

    public Map<String,Object> logincheck(CloseableHttpClient client, HttpClientContext context, String username, String gid) throws IOException {
        HttpResponse response = client.execute(HttpUtil.getRequest(Constants.Api.loginCheck(gid, username), null), context);
        String content = Parser.toString(response);
        String codeString = (String) Parser.readJsonPath(content, Constants.JsonPath.CODESTR_JSONPATH);
        Map<String,Object> result = new HashMap<>();
        result.put("errno",Parser.readJsonPath(content,"$.errInfo.no"));
        result.put("msg",Parser.readJsonPath(content,"$.errInfo.msg"));
        result.put("codeString",codeString);
        return result;
    }

    @Deprecated
    public void genImage(CloseableHttpClient client, HttpClientContext context, String codeStr) throws IOException {
        HttpResponse response = client.execute(HttpUtil.getRequest(Constants.Api.genImage(codeStr), null), context);
        BufferedImage image = Parser.readBufferedImage(response);
    }

    public Map<String,Object> login(CloseableHttpClient client, HttpClientContext context, String username, String password, String vcode, String gid, String codeStr, String serverTime) throws IOException {
        HttpUriRequest loginRequest = HttpUtil.postRequest(Constants.Api.login(), Constants.Api.loginParam(username, password, vcode, gid, codeStr, serverTime));
        HttpResponse response = client.execute(loginRequest, context);
        Map<String,Object> result = new HashMap<>();
        String content = Parser.toString(response);
        String errCode = (String) Parser.readJsonPath(content,"$.errInfo.no");
        result.put("errno",errCode);
        result.put("msg",Parser.readJsonPath(content,"$.errInfo.msg"));
        if("400023".equals(errCode)) {
            result.put("lstr", Parser.readJsonPath(content, "$.data.lstr"));
            result.put("ltoken", Parser.readJsonPath(content, "$.data.ltoken"));
            result.put("phone", Parser.readJsonPath(content, "$.data.phone"));
        }
        return result;
    }

    public Map<String,Object> sendSms(CloseableHttpClient client,HttpClientContext context,String gid,String lstr,String ltoken) throws IOException {
        HttpUriRequest loginRequest = HttpUtil.getRequest(Constants.Api.sendSms(gid,lstr,ltoken),null);
        HttpResponse response = client.execute(loginRequest, context);
        Map<String,Object> result = new HashMap<>();
        String content = Parser.toString(response);
        result.put("errno",Parser.readJsonPath(content,"$.errInfo.no"));
        result.put("msg",Parser.readJsonPath(content,"$.errInfo.msg"));
        return result;
    }

    public Map<String,Object> loginWithSms(CloseableHttpClient client,HttpClientContext context,String vcode,String gid,String lstr,String ltoken) throws Exception {
        HttpUriRequest loginRequest = HttpUtil.postRequest(Constants.Api.smsLogin(),Constants.Api.smsLoginParam(vcode,gid,lstr,ltoken));
        HttpResponse response = client.execute(loginRequest, context);
        String content = Parser.toString(response);
        Map<String,Object> result = new HashMap<>();
        String errCode = (String) Parser.readJsonPath(content,"$.errInfo.no");
        result.put("errno",errCode);
        result.put("msg",Parser.readJsonPath(content,"$.errInfo.msg"));
        if("0".equals(errCode)){
            String loginProxy = (String) Parser.readJsonPath(content, "$.data.loginProxy");
            result.put("loginProxy", loginProxy);

            // 代理登录
            HttpUriRequest loginProxyRequest = HttpUtil.getRequest(loginProxy,null);
            HttpResponse loginProxyResponse = client.execute(loginProxyRequest, context);

            content = Parser.toString(loginProxyResponse);
            errCode = (String) Parser.readJsonPath(content,"$.errInfo.no");
            if("0".equals(errCode)){
                // 获取用户信息
                String xml = (String) Parser.readJsonPath(content,"$.data.xml");
                String bduss = Parser.readXmlPath(xml,"//client/data/res/bduss/text()");
                String ptoken = Parser.readXmlPath(xml,"//client/data/res/ptoken/text()");
                String loginBduss = bduss + "|" +ptoken;
                result.put("bduss", EncryptUtil.encrypt(bduss));
                result.put("ptoken",EncryptUtil.encrypt(ptoken));
                result.put("loginBduss",loginBduss);
            }
        }
        return result;
    }

    public Map<String,Object> cLogin(CloseableHttpClient client,HttpClientContext context,String bdusstoken) throws IOException, NoSuchAlgorithmException {
        HttpUriRequest loginRequest = HttpUtil.postRequest(Constants.Api.cLogin(),Constants.Api.cLoginParam(bdusstoken));
        HttpResponse response = client.execute(loginRequest, context);
        String content = Parser.toString(response);
        String tbs = (String) Parser.readJsonPath(content,"$.anti.tbs");
        String displayName = (String) Parser.readJsonPath(content,"$.user.name");
        String portraitKey = (String) Parser.readJsonPath(content,"$.user.portrait");
        Map<String,Object> result = new HashMap<>();
        result.put("name",displayName);
        result.put("portrait","http://himg.bdimg.com/sys/portrait/item/"+portraitKey);
        result.put("tbs",tbs);
        return result;
    }

    public Map<String,Object> cResume(CloseableHttpClient client,HttpClientContext context,String bdusstoken,String clientID) throws IOException, NoSuchAlgorithmException {
        HttpUriRequest loginRequest = HttpUtil.postRequest(Constants.Api.cLogin(),Constants.Api.cResumeParam(clientID,bdusstoken));
        HttpResponse response = client.execute(loginRequest, context);
        String content = Parser.toString(response);
        String tbs = (String) Parser.readJsonPath(content,"$.anti.tbs");
        String displayName = (String) Parser.readJsonPath(content,"$.user.name");
        String portraitKey = (String) Parser.readJsonPath(content,"$.user.portrait");
        Map<String,Object> result = new HashMap<>();
        result.put("name",displayName);
        result.put("portrait","http://himg.bdimg.com/sys/portrait/item/"+portraitKey);
        result.put("tbs",tbs);
        return result;
    }

}
