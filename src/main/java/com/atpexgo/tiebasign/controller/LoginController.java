package com.atpexgo.tiebasign.controller;

import com.atpexgo.tiebasign.service.LoginService;
import com.atpexgo.tiebasign.service.SignService;
import com.atpexgo.tiebasign.util.Parser;
import com.atpexgo.tiebasign.util.common.Constants;
import com.atpexgo.tiebasign.util.common.CookieUtil;
import com.atpexgo.tiebasign.util.common.EncryptUtil;
import com.atpexgo.tiebasign.util.common.HttpUtil;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 百度登录
 * 登录流程：
 * prepare->check->login
 *               ->login->sendSms->loginWithSmsCode
 * 由于极大几率遇到短信验证码，所以第一种case基本很难遇到
 *
 * TODO
 * 没有必要在后台完成请求登录
 * 考虑到安全性（隐私）和效率，可以将登录过程完全移动至前端
 * 服务端只持久化登录成功后的BDUSS-cookie
 *
 * Created by wudj on 2017/9/28.
 */
@RequestMapping("/baidu")
@Controller
public class LoginController {

    private final SignService signService;

    private final LoginService loginService;

    @Autowired
    public LoginController(SignService signService, LoginService loginService) {
        this.signService = signService;
        this.loginService = loginService;
    }

    @RequestMapping(value = "/prepare", method = RequestMethod.POST)
    public @ResponseBody
    Map prepare(@RequestBody Map<String, Object> param) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String bduss = (String) param.get("BDUSS");
        String ptoken = (String) param.get("PTOKEN");
        String clientid = (String) param.get("clientid");
        if(bduss!=null&&ptoken!=null&&clientid!=null){
            String loginBduss = EncryptUtil.decrypt(bduss)+"|";
            result.putAll(resume(loginBduss,clientid));
            return result;
        }
        CloseableHttpClient httpClient = HttpUtil.buildWebClient();
        HttpClientContext clientContext = HttpClientContext.create();
        result.putAll(this.loginService.wiseuid(httpClient, clientContext));
        result.put("ckeys",setCookie(clientContext.getCookieStore().getCookies()));
        return result;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public @ResponseBody
    Map checkLogin(@RequestBody Map<String, Object> param) throws Exception {
        CloseableHttpClient httpClient = HttpUtil.buildWebClient();
        HttpClientContext clientContext = HttpClientContext.create();
        CookieStore cookieStore = getCookie((List<Map<String, Object>>) param.get("ckeys"));
        clientContext.setCookieStore(cookieStore);
        String username = (String) param.get("username");
        Map<String, Object> result = new HashMap<>();
        String gid = Constants.gid();
        result.put("gid", gid);
        result.putAll(this.loginService.antiplaytoken(httpClient, clientContext));
        result.putAll(this.loginService.logincheck(httpClient, clientContext, username, gid));
        result.put("ckeys",setCookie(clientContext.getCookieStore().getCookies()));
        return result;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody
    Map login(@RequestBody Map<String, Object> param) throws Exception {
        CloseableHttpClient httpClient = HttpUtil.buildWebClient();
        HttpClientContext clientContext = HttpClientContext.create();
        CookieStore cookieStore = getCookie((List<Map<String, Object>>) param.get("ckeys"));
        clientContext.setCookieStore(cookieStore);

        String username = (String) param.get("username");
        String password = (String) param.get("password");
        String vcode = (String) param.get("vcode");
        String gid = (String) param.get("gid");
        String codeStr = (String) param.get("codeStr");
        String serverTime = (String) param.get("serverTime");
        password = Parser.encryptPassword(password, serverTime);
        Map<String, Object> result = new HashMap<>();
        result.putAll(this.loginService.login(httpClient, clientContext, username, password, vcode, gid, codeStr, serverTime));
        result.put("ckeys",setCookie(clientContext.getCookieStore().getCookies()));
        return result;
    }


    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/sendSms", method = RequestMethod.POST)
    public @ResponseBody
    Map sendSMS(@RequestBody Map<String, Object> param) throws Exception {
        CloseableHttpClient httpClient = HttpUtil.buildWebClient();
        HttpClientContext clientContext = HttpClientContext.create();
        CookieStore cookieStore = getCookie((List<Map<String, Object>>) param.get("ckeys"));
        clientContext.setCookieStore(cookieStore);

        Map<String, Object> result = new HashMap<>();
        String gid = (String) param.get("gid");
        String lstr = (String) param.get("lstr");
        String ltoken = (String) param.get("ltoken");
        result.putAll(this.loginService.sendSms(httpClient, clientContext, gid, lstr, ltoken));
        result.put("ckeys",setCookie(clientContext.getCookieStore().getCookies()));
        return result;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/loginWithSmsCode")
    public @ResponseBody
    Map loginWithSmsCode(@RequestBody Map param,HttpServletRequest request,HttpServletResponse response) throws Exception {
        CloseableHttpClient httpClient = HttpUtil.buildWebClient();
        HttpClientContext clientContext = HttpClientContext.create();
        CookieStore cookieStore = getCookie((List<Map<String, Object>>) param.get("ckeys"));
        clientContext.setCookieStore(cookieStore);

        Map<String, Object> result = new HashMap<>();
        String vcode = (String) param.get("vcode");
        String gid = (String) param.get("gid");
        String lstr = (String) param.get("lstr");
        String ltoken = (String) param.get("ltoken");
        result.putAll(this.loginService.loginWithSms(httpClient, clientContext, vcode, gid, lstr, ltoken));
        result.put("ckeys",setCookie(clientContext.getCookieStore().getCookies()));
        if ("0".equals(result.get("errno"))) {
            String loginBduss = (String) result.get("loginBduss");
            result.remove("loginBduss");
            result.putAll(login(loginBduss));
        }
        return result;
    }


    private Map<String, Object> login(String loginBduss) throws IOException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient = HttpUtil.buildCClient();
        HttpClientContext clientContext = HttpClientContext.create();
        return this.loginService.cLogin(httpClient,clientContext,loginBduss);
    }

    private Map<String, Object> resume(String loginBduss,String clientid) throws IOException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient = HttpUtil.buildCClient();
        HttpClientContext clientContext = HttpClientContext.create();
        return this.loginService.cResume(httpClient,clientContext,loginBduss,clientid);
    }

    private List<Cookie> setCookie(List<Cookie> cookies){
       return cookies.stream().map(cookie -> {
           BasicClientCookie c = new BasicClientCookie(EncryptUtil.rightShift(cookie.getName()),EncryptUtil.rightShift(cookie.getValue()));
           c.setDomain(cookie.getDomain());
           c.setPath(cookie.getPath());
           c.setExpiryDate(cookie.getExpiryDate());
           c.setComment(cookie.getComment());
           c.setSecure(cookie.isSecure());
           return c;
       }).collect(Collectors.toList());
    }

    private CookieStore getCookie(List<Map<String,Object>> cookieMap){
        CookieStore cookieStore = new BasicCookieStore();
        if(cookieMap!=null)
            cookieMap.forEach(cookie->{
                BasicClientCookie basicClientCookie = new BasicClientCookie(EncryptUtil.leftShift((String)cookie.get("name")), EncryptUtil.leftShift((String)cookie.get("value")));
                Date date = new Date();
                date.setTime((Long)cookie.get("expiryDate"));
                basicClientCookie.setExpiryDate(date);
                basicClientCookie.setPath((String)cookie.get("path"));
                basicClientCookie.setDomain((String)cookie.get("domain"));
                cookieStore.addCookie(basicClientCookie);
            });
        return cookieStore;
    }

}
