package com.atpexgo.tiebasign.util.common;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CookieUtil {


    public static void addCookie(HttpServletRequest request,HttpServletResponse response, List<org.apache.http.cookie.Cookie> cookies) {
        cookies.forEach(c -> doAddCookie(request,response, EncryptUtil.rightShift(c.getName()), EncryptUtil.encrypt(c.getValue()), (int) c.getExpiryDate().getTime() / 1000, c.getPath()));
    }

    public static void addCookie(HttpServletRequest request,HttpServletResponse response, String name, String value, int seconds, String path) {
        doAddCookie(request,response, EncryptUtil.rightShift(name), EncryptUtil.encrypt(value), seconds, path);
    }

    public static CookieStore readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        CookieStore cookieStore = new BasicCookieStore();
        if (cookies != null)
            Arrays.stream(cookies).forEach(ck -> {
                BasicClientCookie c = new BasicClientCookie(EncryptUtil.leftShift(ck.getName()), EncryptUtil.decrypt(ck.getValue()));
                c.setDomain(ck.getDomain());
                c.setPath(ck.getPath());
                Date eD = new Date();
                eD.setTime(ck.getMaxAge() * 1000);
                c.setExpiryDate(eD);
                cookieStore.addCookie(c);
            });
        return cookieStore;
    }

    /**
     * 创建cookie，并将新cookie添加到“响应对象”response中。
     *
     * @param response
     */
    private static void doAddCookie(HttpServletRequest request,HttpServletResponse response, String name, String value, Integer seconds, String path) {
        Cookie cookie = new Cookie(name, value);//创建新cookie
        cookie.setMaxAge(seconds);// 设置存在时间为5分钟
        cookie.setPath(path);//设置作用域
        StringBuffer url = request.getRequestURL();
        String domain = url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
        cookie.setDomain(domain);
        response.addCookie(cookie);//将cookie添加到response的cookie数组中返回给客户端
    }

    /**
     * 修改cookie，可以根据某个cookie的name修改它（不只是name要与被修改cookie一致，path、domain必须也要与被修改cookie一致）
     *
     * @param request
     * @param response
     */
    private static void editCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, Integer seconds) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> findCookie = Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findAny();
            if (findCookie.isPresent()) {
                //如果发现与指定cookieName相同的cookie，就修改相关数据
                Cookie cookie = findCookie.get();
                cookie.setValue(value);//修改value
                cookie.setMaxAge(seconds);// 修改存活时间
                response.addCookie(cookie);//将修改过的cookie存入response，替换掉旧的同名cookie
            }
        }
    }


    /**
     * 删除cookie
     *
     * @param request
     * @param response
     */
    private static void delCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> findCookie = Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findAny();
            if (findCookie.isPresent()) {
                //如果找到同名cookie，就将value设置为null，将存活时间设置为0，再替换掉原cookie，这样就相当于删除了。
                Cookie cookie = findCookie.get();
                cookie.setValue(null);
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }
}
