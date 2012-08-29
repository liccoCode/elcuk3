package controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import helper.Dates;
import models.market.Account;
import models.market.M;
import models.market.OrderItem;
import models.market.Orderr;
import models.support.Ticket;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.joda.time.DateTime;
import play.Play;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.With;

import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Application extends Controller {

    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(9);
        Map<String, Map<String, Long>> ticketTable = Ticket.frontPageTable(DateTime.now().minusDays(1).toDate(), new Date());
        Map<String, Map<String, Long>> ticket7Table = Ticket.frontPageTable(DateTime.now().minusDays(7).toDate(), new Date());
        render(odmaps, ticketTable, ticket7Table);
    }

    @CacheFor(value = "20mn")
    public static void categoryPercent(Date date, long aid) {
        renderJSON(JSON.toJSON(
                OrderItem.itemGroupByCategory(Dates.morning(date),
                        Dates.night(date),
                        Account.<Account>findById(aid)))
        );
    }

    public static void clearCache() {
        Cache.delete("home.page");
        renderJSON(new Ret());
    }

    public static void cc() {
        Cache.clear();
        renderJSON(new Ret());
    }

    /**
     * 清除指定 key 的缓存
     *
     * @param key
     */
    public static void c(String key) {
        Cache.delete(key);
        renderJSON(new Ret(true, String.format("[%s] clear success", key)));
    }

    public static void upload(File file) {

        System.out.println(file.length() / 1024 + " KB");
        renderJSON(new Ret());
    }

    public static void timeline() {
        render();
    }

    /**
     * 测试使用的登陆代码
     *
     * @param id
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void amazonLogin(long id) throws IOException, ClassNotFoundException {
        if(Play.mode.isProd()) forbidden();
        Account acc = Account.findById(id);

        File jsonFile = Play.getFile("/test/" + acc.prettyName() + ".json");
        if(!jsonFile.exists()) {
            acc.loginAmazonSellerCenter();
            FileOutputStream fos = new FileOutputStream(new File(Play.applicationPath + "/test", acc.prettyName() + ".json"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(acc.cookieStore());
            oos.close();
        } else {
            FileInputStream fis = new FileInputStream(jsonFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CookieStore cookieStore = (CookieStore) ois.readObject();
            Account.COOKIE_STORE_MAP.put(Account.cookieKey(acc.id, acc.type), cookieStore);
        }
        Account.COOKIE_STORE_MAP.get(Account.cookieKey(acc.id, acc.type)).clearExpired(new Date());
        renderJSON(Account.COOKIE_STORE_MAP.get(Account.cookieKey(acc.id, acc.type)));
    }

}