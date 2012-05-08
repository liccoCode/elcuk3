package helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import play.Logger;
import play.Play;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Play 中利用 AsyncHttpClient 的 HTTP 请求
 * User: wyattpan
 * Date: 3/14/12
 * Time: 12:02 PM
 */
public class HTTP {
    private static DefaultHttpClient client;

    public static void init() {
        synchronized(HTTP.class) {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(params, org.apache.http.protocol.HTTP.UTF_8);
            HttpProtocolParams.setUserAgent(params, Play.configuration.getProperty("http.userAgent"));
            HttpClientParams.setRedirecting(params, true);
            HttpConnectionParams.setSoTimeout(params, (int) TimeUnit.SECONDS.toMillis(8));
            HttpConnectionParams.setConnectionTimeout(params, (int) TimeUnit.SECONDS.toMillis(8));

            ThreadSafeClientConnManager multipThread = new ThreadSafeClientConnManager();
            multipThread.setDefaultMaxPerRoute(8); // 每一个站点最多只允许 8 个链接
            multipThread.setMaxTotal(40); // 所有站点最多允许 40 个链接

            client = new DefaultHttpClient(multipThread, params);
            client.setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                    if(response == null) {
                        throw new IllegalArgumentException("HTTP response may not be null");
                    }

                    int statusCode = response.getStatusLine().getStatusCode();
                    String method = request.getRequestLine().getMethod();
                    Header locationHeader = response.getFirstHeader("location");
                    switch(statusCode) {
                        case HttpStatus.SC_MOVED_TEMPORARILY:
                            return (method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                                    || method.equalsIgnoreCase(HttpPost.METHOD_NAME)
                                    || method.equalsIgnoreCase(HttpHead.METHOD_NAME)) && locationHeader != null;
                        case HttpStatus.SC_MOVED_PERMANENTLY:
                        case HttpStatus.SC_TEMPORARY_REDIRECT:
                            return method.equalsIgnoreCase(HttpGet.METHOD_NAME)
                                    || method.equalsIgnoreCase(HttpPost.METHOD_NAME)
                                    || method.equalsIgnoreCase(HttpHead.METHOD_NAME);
                        case HttpStatus.SC_SEE_OTHER:
                            return true;
                        default:
                            return false;
                    } //end of switch
                }
            });
        }
    }

    public static synchronized void stop() {
        HTTP.client = null;
    }

    public static DefaultHttpClient client() {
        if(HTTP.client == null) HTTP.init();
        return HTTP.client;
    }

    /**
     * 清理过期的 Cookie
     */
    public static void clearExpiredCookie() {
        HTTP.client().getCookieStore().clearExpired(new Date());
    }

    public static String get(String url) {
        try {
            return EntityUtils.toString(client().execute(new HttpGet(url)).getEntity());
        } catch(IOException e) {
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    public static String post(String url, List<NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            return EntityUtils.toString(client().execute(post).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    public static JsonElement postJson(String url, List<NameValuePair> params) {
        Logger.debug("HTTP.post Json [%s]", url);
        String json = post(url, params);
        try {
            return new JsonParser().parse(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    public static JsonElement json(String url) {
        Logger.debug("HTTP.get Json [%s]", url);
        String json = get(url);
        try {
            return new JsonParser().parse(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

}
