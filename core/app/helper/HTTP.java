package helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.IO;
import play.libs.MimeTypes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;


/**
 * 对 HttpClient 4 的封装 HTTP 请求
 * User: wyattpan
 * Date: 3/14/12
 * Time: 12:02 PM
 */
public class HTTP {
    private static DefaultHttpClient client;

    private static DefaultHttpClient proxyclient;
    /**
     * 默认的 Cookie Store
     */
    public static final CookieStore COOKIE_STORE = new BasicCookieStore();

    public static void init() {
        synchronized(HTTP.class) {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUserAgent(params, Play.configuration.getProperty("http.userAgent"));
            HttpClientParams.setRedirecting(params, true);
            // Socket 超时不能设置太短, 不然像下载这样的操作会很容易超时
            HttpConnectionParams.setSoTimeout(params, (int) TimeUnit.SECONDS.toMillis(90));
            HttpConnectionParams.setConnectionTimeout(params, (int) TimeUnit.SECONDS.toMillis(90));

            PoolingClientConnectionManager multipThread = new PoolingClientConnectionManager();
            multipThread.setDefaultMaxPerRoute(8); // 每一个站点最多只允许 8 个链接
            multipThread.setMaxTotal(40); // 所有站点最多允许 40 个链接

            client = new DefaultHttpClient(multipThread, params);
            client.addRequestInterceptor(new RequestAcceptEncoding());
            client.addResponseInterceptor(new ResponseContentEncoding());
            client.setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response,
                                            HttpContext context) throws ProtocolException {
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
                                    || method.equalsIgnoreCase(HttpHead.METHOD_NAME)) &&
                                    locationHeader != null;
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


    public static void proxyinit() {
        synchronized(HTTP.class) {
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setContentCharset(params, "UTF-8");
            HttpProtocolParams.setUserAgent(params, Play.configuration.getProperty("http.userAgent"));
            HttpClientParams.setRedirecting(params, true);
            // Socket 超时不能设置太短, 不然像下载这样的操作会很容易超时
            HttpConnectionParams.setSoTimeout(params, (int) TimeUnit.SECONDS.toMillis(90));
            HttpConnectionParams.setConnectionTimeout(params, (int) TimeUnit.SECONDS.toMillis(90));

            PoolingClientConnectionManager multipThread = new PoolingClientConnectionManager();
            multipThread.setDefaultMaxPerRoute(8); // 每一个站点最多只允许 8 个链接
            multipThread.setMaxTotal(40); // 所有站点最多允许 40 个链接

            proxyclient = new DefaultHttpClient(multipThread, params);
            proxyclient.addRequestInterceptor(new RequestAcceptEncoding());
            proxyclient.addResponseInterceptor(new ResponseContentEncoding());
            proxyclient.setRedirectStrategy(new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response,
                                            HttpContext context) throws ProtocolException {
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
                                    || method.equalsIgnoreCase(HttpHead.METHOD_NAME)) &&
                                    locationHeader != null;
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
        HTTP.proxyclient = null;
    }

    public static DefaultHttpClient client() {
        if(HTTP.client == null) HTTP.init();
        return HTTP.client;
    }

    public static DefaultHttpClient proxyclient() {
        if(HTTP.proxyclient == null) HTTP.proxyinit();
        return HTTP.proxyclient;
    }

    /**
     * 可以设置不同的 Cookie 池
     *
     * @param cookieStore
     */
    public static HttpClient cookieStore(CookieStore cookieStore) {
        if(cookieStore == null) client().setCookieStore(HTTP.COOKIE_STORE);
        else client().setCookieStore(cookieStore);
        return client();
    }

    /**
     * 清理过期的 Cookie
     */
    public static void clearExpiredCookie() {
        client().getCookieStore().clearExpired(new Date());
    }

    /**
     * 可以设置不同的 Cookie 池
     *
     * @param cookieStore
     */
    public static HttpClient proxycookieStore(CookieStore cookieStore) {
        if(cookieStore == null) {
            proxyclient().setCookieStore(HTTP.COOKIE_STORE);
            client().setCookieStore(HTTP.COOKIE_STORE);
        } else {
            proxyclient().setCookieStore(cookieStore);
            client().setCookieStore(cookieStore);
        }
        return proxyclient();
    }

    /**
     * 使用默认 Cookie Store
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        return get(null, url);
    }

    public static String getProxy(String proxyhost,String url) {
        try {
            HttpGet get = new HttpGet(url);
            DefaultHttpClient httpClient = (DefaultHttpClient) proxycookieStore(null);

            String[] args = proxyhost.split(",");
            String proxyHost = args[0];
            int proxyPort = Integer.parseInt(args[1]);
            String userName = args[2];
            String password = args[3];

            httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(proxyHost, proxyPort),
                    new UsernamePasswordCredentials(userName, password));
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

            return EntityUtils.toString(
                    httpClient.execute(get).getEntity(),
                    "UTF-8");
        } catch(IOException e) {
            e.printStackTrace();
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    public static JSONObject getJson(String url) {
        return JSON.parseObject(get(null, url));
    }

    /**
     * 传入指定的 CookieStore
     *
     * @param cookieStore
     * @param url
     * @return
     */
    public static String get(CookieStore cookieStore, String url) {
        try {
            return EntityUtils.toString(
                    cookieStore(cookieStore).execute(new HttpGet(url)).getEntity(),
                    "UTF-8");
        } catch(IOException e) {
            e.printStackTrace();
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    /**
     * 使用默认 Cookie Store
     *
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, Collection<? extends NameValuePair> params) {
        return post(null, url, params);
    }


    /**
     * 传入指定的 CookieStore
     *
     * @param cookieStore
     * @param url
     * @param params
     * @return
     */
    public static String post(CookieStore cookieStore, String url,
                              Collection<? extends NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<NameValuePair>(params), "UTF-8"));
            return EntityUtils.toString(cookieStore(cookieStore).execute(post).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    /**
     * 使用代理访问
     *
     * @param cookieStore
     * @param url
     * @param params
     * @return
     */
    public static String postProxy(CookieStore cookieStore, String url,
                                   Collection<? extends NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        try {
            DefaultHttpClient httpClient = (DefaultHttpClient) proxycookieStore(cookieStore);

            HttpHost proxy = new HttpHost("hk2.easya.cc", 8123);
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<NameValuePair>(params), "UTF-8"));
            return EntityUtils.toString(httpClient.execute(post).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }


    public static JSONObject postJson(String url, Collection<? extends NameValuePair> params) {
        Logger.debug("HTTP.post Json [%s]", url);
        String json = post(url, params);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }


    // 修改 JsonElement -> JSONELement 面太广
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

    public static JSONObject json(String url, Collection<? extends NameValuePair> params) {
        Logger.debug("HTTP.post Json [%s]", url);
        String json = post(url, params);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    /**
     * 最简单的下载
     *
     * @param url
     * @return
     */
    public static byte[] getDown(String url) {
        HttpGet get = new HttpGet(url);
        try {
            return EntityUtils.toByteArray(client().execute(get).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.getDown[%s] [%s]", url, Webs.E(e));
            return new byte[]{};
        }
    }

    public static F.Option<File> getDownFile(String url, String fileName, CookieStore cookie) {
        HttpGet get = new HttpGet(url);
        try {
            File file = new File(String.format("%s/%s", Constant.TMP, fileName));
            IO.copy(cookieStore(cookie).execute(get).getEntity().getContent(),
                    new FileOutputStream(file));
            return F.Option.Some(file);
        } catch(IOException e) {
            Logger.warn("HTTP.getDownFile[%s] [%s]", url, Webs.E(e));
            return F.Option.None();
        }
    }


    /**
     * 通过 Post 进行下载
     *
     * @param cookieStore
     * @param url
     * @param params
     * @return
     */
    public static byte[] postDown(CookieStore cookieStore, String url,
                                  Collection<? extends NameValuePair> params) {
        HttpPost post = new HttpPost(url);
        try {
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<NameValuePair>(params), "UTF-8"));
            return EntityUtils.toByteArray(cookieStore(cookieStore).execute(post).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.postDown[%s] [%s]", url, Webs.E(e));
            return new byte[]{};
        }
    }

    /**
     * 上传文件的 AkPI
     *
     * @param cookieStore 使用的 Cookie
     * @param url         访问的地址
     * @param params      需要提交的其他参数
     * @param uploadFiles 上传多个文件的集合(key: fileParamName, Val: File)
     * @return 返回上传的结果
     */
    public static String upload(CookieStore cookieStore, String url,
                                Collection<? extends NameValuePair> params,
                                Map<String, F.T2<String, BufferedInputStream>> uploadFiles) {
        HttpPost post = new HttpPost(url);
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            for(NameValuePair nv : params) {
                multipartEntity.addPart(nv.getName(), new StringBody(nv.getValue()));
            }

            for(String fileParamName : uploadFiles.keySet()) {
                F.T2<String, BufferedInputStream> file = uploadFiles.get(fileParamName);


                multipartEntity.addPart(fileParamName,
                        new InputStreamBody(file._2, MimeTypes.getMimeType(file._1)));
            }
            post.setEntity(multipartEntity);
            return EntityUtils.toString(cookieStore(cookieStore).execute(post).getEntity());
        } catch(Exception e) {
            e.printStackTrace();
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }


    // -------------------- body string ----------------------

    public static String post(String url, String body) {
        HttpPost post = new HttpPost(url);
        try {

            post.setEntity(new StringEntity(body, Charset.forName("UTF-8")));
            return EntityUtils.toString(client().execute(post).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    public static JSONObject postJson(String url, String body) {
        Logger.debug("HTTP.post Json [%s]", url);
        String json = post(url, body);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

}
