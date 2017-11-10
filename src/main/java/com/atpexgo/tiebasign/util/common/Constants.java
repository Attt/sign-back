package com.atpexgo.tiebasign.util.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wudj on 2017/9/29.
 */
public class Constants {

    private static final String[] strDigits = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};


    public static class Encoding {
        public static final Charset UTF_8 = Charset.forName("utf-8");
        public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
        public static final Charset GBK = Charset.forName("GBK");
        public static final Charset GB2312 = Charset.forName("gb2312");
    }

    public static class JsonPath {
        public static final String ANTI_TOKEN_TIME_JSONPATH = "$.time";
        public static final String BDCM_JSONPATH = "$.bdcm";
        public static final String UID_JSONPATH = "$.uid";
        public static final String CODESTR_JSONPATH = "$.data.codeString";
    }

    public static class CookieKey {
        public static final String BAIDUID = "BAIDUID";
        public static final String BAIDU_WISE_UID = "BAIDU_WISE_UID";
    }

    public static class Api {
        public static String loginCheck(String gid, String username) {
            return getUrl(Url.LOGIN_CHECK_API, gid, String.valueOf(System.currentTimeMillis()), username);
        }

        public static String antiToken() {
            return getUrl(Url.ANTI_TOKEN_API, String.valueOf(System.currentTimeMillis()));
        }

        public static String wiseUid() {
            return getUrl(Url.WISE_UID_API);
        }

        public static String genImage(String codeStr) {
            return getUrl(Url.GEN_IMAGE_API, codeStr, String.valueOf(System.currentTimeMillis()));
        }

        public static String login() {
            return getUrl(Url.LOGIN_API, String.valueOf(System.currentTimeMillis()));
        }

        public static List<NameValuePair> loginParam(String username, String password,String vcode, String gid, String codeStr, String serverTime) {
            return getParam(Param.LOGIN_PARAM, username, password,vcode, gid, codeStr, serverTime);
        }

        public static String sendSms(String gid,String lstr,String ltoken) throws UnsupportedEncodingException {
            lstr = URLEncoder.encode(lstr,"utf-8");
            return getUrl(Url.SEND_SMS_API, String.valueOf(System.currentTimeMillis()),gid,lstr,ltoken);
        }

        public static String smsLogin(){
            return getUrl(Url.SMS_LOGIN_API,String.valueOf(System.currentTimeMillis()));
        }

        public static List<NameValuePair> smsLoginParam(String vcode,String gid,String lstr,String ltoken) {
            return getParam(Param.SMS_LOGIN_PARAM,vcode,gid,lstr,ltoken);
        }

        public static String cLogin(){
            return getUrl(Url.C_LOGIN_API);
        }

        public static List<NameValuePair> cLoginParam(String bdusstoken) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            List<NameValuePair> param = getParam(Param.C_LOGIN_PARAM,bdusstoken,String.valueOf(System.currentTimeMillis()));
            param.add(new BasicNameValuePair("sign",getSign(param)));
            return param;
        }


        public static List<NameValuePair> cResumeParam(String clientId,String bdusstoken) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            List<NameValuePair> param = getParam(Param.C_RESUME_PARAM,clientId,bdusstoken,String.valueOf(System.currentTimeMillis()));
            param.add(new BasicNameValuePair("sign",getSign(param)));
            return param;
        }
    }

    private static class Url {
        private static final String WISE_UID_API = "https://wappass.baidu.com/passport/login?clientfrom=native&tpl=tb&login_share_strategy=silent&client=ios&adapter=3&act=implicit&loginLink=0&smsLoginLink=0&lPFastRegLink=0&lPlayout=0&is_voice_sms=1";
        private static final String ANTI_TOKEN_API = "https://wappass.baidu.com/wp/api/security/antireplaytoken?tpl=tb&v={0}";
        private static final String LOGIN_CHECK_API = "https://wappass.baidu.com/wp/api/login/check?clientfrom=native&gid={0}&tt={1}&username={2}&tpl=tb";
        private static final String GEN_IMAGE_API = "https://wappass.baidu.com/cgi-bin/genimage?{0}&v={1}";
        private static final String LOGIN_API = "https://wappass.baidu.com/wp/api/login?v={0}&cv=170601";
        private static final String SEND_SMS_API = "https://wappass.baidu.com/wp/login/sec?ajax=1&v={0}&vcode=&clientfrom=native&tpl=tb&login_share_strategy=silent&client=ios&adapter=3&act=implicit&loginLink=0&smsLoginLink=1&lPFastRegLink=0&lPlayout=0&is_voice_sms=1&lang=zh-cn&action=login&loginmerge=1&isphone=0&dialogVerifyCode=&dialogVcodestr=&dialogVcodesign=&gid={1}&authtoken=&showtype=phone&lstr={2}&ltoken={3}&u=";
        private static final String SMS_LOGIN_API = "https://wappass.baidu.com/wp/login/sec?type=2&v={0}";
        private static final String C_LOGIN_API = "http://c.tieba.baidu.com/c/s/login";
    }

    private static class Param {
        private static final String LOGIN_PARAM = "username={0}&code=&password={1}&verifycode={2}&clientfrom=native&tpl=tb&login_share_strategy=silent&client=ios&adapter=3&act=implicit&loginLink=0&smsLoginLink=1&lPFastRegLink=0&lPlayout=0&is_voice_sms=1&lang=zh-cn&action=login&loginmerge=1&isphone=0&dialogVerifyCode=&dialogVcodestr=&dialogVcodesign=&gid={3}&authtoken=&vcodestr={4}&countrycode=&servertime={5}&logLoginType=sdk_login&passAppHash=&passAppVersion=&cv=170601&faceLoginCase=none";
        private static final String SMS_LOGIN_PARAM = "vcode={0}&clientfrom=native&tpl=tb&login_share_strategy=silent&client=ios&adapter=3&act=implicit&loginLink=0&smsLoginLink=1&lPFastRegLink=0&lPlayout=0&is_voice_sms=1&lang=zh-cn&action=login&loginmerge=1&isphone=0&dialogVerifyCode=&dialogVcodestr=&dialogVcodesign=&gid={1}&authtoken=&showtype=phone&lstr={2}&ltoken={3}&u=";
        private static final String C_LOGIN_PARAM = "_client_type=2&_client_version=7.9.2&_phone_imei=030094308005220&bdusstoken={0}&channel_id=&channel_uid=&from=mini_baidu_appstore&timestamp={1}";
        private static final String C_RESUME_PARAM = "_client_id={0}&_client_type=2&_client_version=7.9.2&_phone_imei=030094308005220&bdusstoken={1}&channel_id=&channel_uid=&from=mini_baidu_appstore&timestamp={2}";

    }

    public static String gid() {
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

    /**
     * 计算uwp版的提交sign,需要bduss
     * @return
     */
    private static String getSign(List<NameValuePair> loginParams) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String postData = loginParams.stream().map(nameValuePair -> nameValuePair.getName()+"="+nameValuePair.getValue()).collect(StringBuilder::new,StringBuilder::append,StringBuilder::append).toString();
        String originalSign = postData.replaceAll("&","").concat("tiebaclient!!!");
        DigestUtils.md5(originalSign);
        return byteToString(DigestUtils.md5(originalSign));
    }


    private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();

        for(int i = 0; i < bByte.length; ++i) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }

        return sBuffer.toString();
    }


    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if(bByte < 0) {
            iRet = bByte + 256;
        }

        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return strDigits[iD1] + strDigits[iD2];
    }

    /**
     * 接口参数合并生成请求URL
     *
     * @param rawApi 接口
     * @param params 参数
     * @return
     */
    private static String getUrl(String rawApi, String... params) {
        if (params.length != 0) {
            for (int i = 0; i < params.length; i++) {
                rawApi = rawApi.replaceAll("\\{" + i + "}", params[i]);
            }
        }
        return rawApi;
    }

    /**
     * 生成POST参数
     *
     * @param rawParam 接口
     * @param params   参数
     * @return
     */
    private static List<NameValuePair> getParam(String rawParam, String... params) {
        if (params.length != 0) {
            for (int i = 0; i < params.length; i++) {
                rawParam = rawParam.replaceAll("\\{" + i + "}", params[i]);
            }
        }

        String[] splitParam = rawParam.split("&");
        List<NameValuePair> postParam = new ArrayList<>();
        for (String prm : splitParam) {
            String[] kv = prm.split("=");
            NameValuePair pair = new BasicNameValuePair(kv[0], kv.length<2?"":kv[1]);
            postParam.add(pair);
        }
        return postParam;
    }
}
