package controllers;

import helper.*;
import models.ElcukRecord;
import models.Privilege;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.MimeTypes;
import play.Logger;
import play.data.binding.As;
import play.mvc.Util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


/**
 * 用户登陆限制
 * User: wyattpan
 * Date: 1/13/12
 * Time: 12:42 AM
 */
public class Login extends Secure.Security {

    /**
     * 登陆的用户 Cache
     */
    private static final Map<String, User> USER_CACHE = new ConcurrentHashMap<>();

    /**
     * 登陆
     */
    static boolean authenticate(String username, String password) {
        //全部转为小写
        username = username.toLowerCase();
        /**
         * 1. 判断是否拥有此用户; 使用公司邮箱 @easyacceu.com
         * 2. 判断用户登陆是否正常
         */
        User user = User.findByUserName(username);
        if(user == null) return false;
        boolean iscorrect = user.authenticate(password);
        String domain = models.OperatorConfig.getVal("domain");
        if(iscorrect) {
            Duration leftTime = Duration.standardDays(30);
            int timeInSeconds = leftTime.toStandardSeconds().getSeconds();
            response.setCookie("username", username, domain, "/", timeInSeconds, false);
            response.setCookie("usermd5", User.userMd5(username), domain, "/", timeInSeconds, false);

            response.setCookie("kod_name", "elcuk2", domain, "/", timeInSeconds, false);
            response.setCookie("kod_token", Webs.md5(User.userMd5("elcuk2")), domain, "/", timeInSeconds, false);
            response.setCookie("kod_user_language", "zh_CN", domain, "/", timeInSeconds, false);
            response.setCookie("kod_user_online_version", "check-at-1418867695", domain, "/", timeInSeconds, false);
            new ElcukRecord("login", J.json(
                    GTs.MapBuilder.map("Username", username).put("Ip", request.remoteAddress)
                            .put("UserAgent", request.headers.get("user-agent").toString()).
                            put("Date", Dates.date2DateTime(DateTime.now())).build()
            ), username, null).save();
        }
        return iscorrect;
    }

    static boolean check(String profile) {
        User user = current();
        if(user == null) return false;
        if("root_user".equals(user.username)) return true;
        Set<Privilege> privileges = Privilege.privileges(user.username, user.roles);
        return CollectionUtils.find(privileges, new PrivilegePrediect(profile.toLowerCase())) != null;
    }

    /**
     * 在用户登出以前做处理
     */
    static void onDisconnect() {
        String domain = models.OperatorConfig.getVal("domain");
        try {
            Login.current().logout();
            response.setCookie("username", "", domain, "/", 0, false);
            response.setCookie("usermd5", "", domain, "/", 0, false);

            response.setCookie("kod_name", "", domain, "/", 0, false);
            response.setCookie("kod_token", "", domain, "/", 0, false);
            response.setCookie("kod_user_language", "", domain, "/", 0, false);
            response.setCookie("kod_user_online_version", "", domain, "/", 0, false);
        } catch(NullPointerException e) {
            Logger.warn("Current User is null. No Cookie.");
        }
    }

    @SuppressWarnings("unchecked")
    @Util
    public static User current() {
        /**
         * 初始化:
         * 将用户信息缓存到 User Cache.
         */
        // 初始化用户缓存中的用户;
        String username = Secure.Security.connected();
        if(StringUtils.isNotBlank(username)) {
            if(USER_CACHE.get(username) == null) {
                User user = User.findByUserName(username.toLowerCase());
                user.login();
                USER_CACHE.put(user.username, user);
            }
            return USER_CACHE.get(username.toLowerCase());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Util
    public static String currentUserName() {
        return Secure.Security.connected();
    }

    @SuppressWarnings("unchecked")
    @Util
    public static User updateUserCache(User user) {
        USER_CACHE.put(user.username, user);
        return USER_CACHE.get(user.username);
    }

    /**
     * 清除 Users Cache 中的某一个用户的缓存
     *
     * @param user
     */
    @Util
    public static void clearUserCache(User user) {
        USER_CACHE.remove(user.username);
    }

    @Util
    public static boolean isUserLogin(User user) {
        return USER_CACHE.containsKey(user.username);
    }


    /**
     * 过滤权限
     */
    static class PrivilegePrediect implements Predicate {
        private String actionName;

        PrivilegePrediect(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public boolean evaluate(Object o) {
            Privilege pri = (Privilege) o;
            return StringUtils.equals(this.actionName, pri.name);
        }
    }

    /**
     * 产生 FBA 的 2维 码(不能拥有权限判断)
     *
     * @param shipmentId FBA 的 shipmentId
     * @throws java.io.IOException
     */
    public static void code128(String shipmentId) throws IOException {
        // 将 barcode4j 使用 zxing 代替, 例子: http://aboutyusata.blogspot.hk/2012/10/generate-code128-qrcode-pdf417-barcode.html
        // 核心使用到两个 API: MatrixToImageWriter, Code128Writer
        Code128Bean bean = new Code128Bean();

        // 尽可能调整到与 Amazon 的规格一样
        bean.setModuleWidth(0.11888);
        bean.setHeight(8.344);
        // 控制值内容, CODESET_A 不允许有小写
        bean.setCodeset(Code128Constants.CODESET_ALL);
        bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);

        String fileName = String.format("%s.jpeg", shipmentId);
        Logger.info("Server Image: %s", fileName);
        File file = new File(Constant.TMP, fileName);
        file.delete(); // 删除原来的, 再写新的
        OutputStream out = new FileOutputStream(file);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, MimeTypes.MIME_JPEG, 600,
                BufferedImage.TYPE_BYTE_BINARY, false, 0);
        bean.generateBarcode(canvas, shipmentId);
        canvas.finish();
        out.close();
        response.setContentTypeIfNotSet(MimeTypes.MIME_JPEG);
        renderBinary(file.toURI().toURL().openStream());
    }

    public static void fnSkuCode128(String fnSku) throws IOException {
        // 将 barcode4j 使用 zxing 代替, 例子: http://aboutyusata.blogspot.hk/2012/10/generate-code128-qrcode-pdf417-barcode.html
        // 核心使用到两个 API: MatrixToImageWriter, Code128Writer
        Code128Bean bean = new Code128Bean();

        // 尽可能调整到与 Amazon 的规格一样
        bean.setModuleWidth(0.11888);
        bean.setHeight(8.344);
        // 控制值内容, CODESET_A 不允许有小写
        bean.setCodeset(Code128Constants.CODESET_ALL);
        bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);

        String fileName = String.format("%s.jpeg", fnSku);
        File file = new File(Constant.TMP, fileName);
        file.delete(); // 删除原来的, 再写新的
        OutputStream out = new FileOutputStream(file);
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, MimeTypes.MIME_JPEG, 600,
                BufferedImage.TYPE_BYTE_BINARY, false, 0);
        bean.generateBarcode(canvas, fnSku);
        canvas.finish();
        out.close();
        response.setContentTypeIfNotSet(MimeTypes.MIME_JPEG);
        renderBinary(file.toURI().toURL().openStream());
    }

    /**
     * 触发使用计算使用
     *
     * @param date
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void job(@As("yyyy-MM-dd") Date date) throws ExecutionException, InterruptedException {
        //Logger.info(request.remoteAddress);
        //new SellingRecordCaculateJob(new DateTime(date)).now().get();
        renderHtml("<h3>SellingRecordCaculateJob 开始执行</h3>");
    }

}
