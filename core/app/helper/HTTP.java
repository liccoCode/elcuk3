package helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
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
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 对 HttpClient 4 的封装 HTTP 请求
 * User: wyattpan
 * Date: 3/14/12
 * Time: 12:02 PM
 */
public class HTTP {
    private static CloseableHttpClient client;

    /**
     * 默认的 Cookie Store
     */
    public static final CookieStore COOKIE_STORE = new BasicCookieStore();

    public static synchronized void init() {
        client = create();
    }

    public static CloseableHttpClient create() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .setRedirectsEnabled(true) //允许 Redirect
                .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(10); // 每一个站点最多只允许 10 个链接
        connManager.setMaxTotal(100); // 所有站点最多允许 100 个链接

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setCharset(Charset.forName("UTF-8")) //Charset
                .build();

        DefaultRedirectStrategy defaultRedirectStrategy = new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response,
                                        HttpContext context) throws ProtocolException {
                if(response == null) {
                    throw new IllegalArgumentException("HTTP response may not be null");
                }

                int statusCode = response.getStatusLine().getStatusCode();
                String method = request.getRequestLine().getMethod();
                Header locationHeader = response.getFirstHeader("location");
                switch(statusCode) {//设置哪些 response 状态码应该 Redirect
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
                }
            }
        };

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(connManager)
                .setDefaultConnectionConfig(connectionConfig)
                .setUserAgent(Play.configuration.getProperty("http.userAgent")) // UserAgent
                .addInterceptorLast(new RequestAcceptEncoding())
                .addInterceptorLast(new ResponseContentEncoding())
                .setRedirectStrategy(defaultRedirectStrategy)
                .setDefaultCookieStore(HTTP.COOKIE_STORE);
        return httpClientBuilder.build();
    }

    public static synchronized void stop() {
        HTTP.client = null;
    }

    public static CloseableHttpClient client() {
        if(HTTP.client == null) HTTP.init();
        return HTTP.client;
    }

    /**
     * 清理默认的 CookieStore 内的过期的 Cookie
     */
    public static void clearExpiredCookie() {
        HTTP.COOKIE_STORE.clearExpired(new Date());
    }

    /**
     * 设置 CookieStore 到 HttpClientContext
     *
     * @param cookieStore
     * @return
     */
    public static HttpClientContext getContextWithCookieStore(CookieStore cookieStore) {
        //HttpClientContext 参数层级高于 client 的参数层级
        HttpClientContext context = HttpClientContext.create();
        if(cookieStore != null) {
            context.setCookieStore(cookieStore);
        } else {
            //其实这一步没有必要,因为 HttpClientBuilder 的时候已经设置了 DefaultCookieStore 为 HTTP.COOKIE_STORE
            context.setCookieStore(HTTP.COOKIE_STORE);
        }
        return context;
    }

    /**
     * 使用默认 Cookie Store
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        return get(null, url, null);
    }

    public static JSONObject getJson(String url) {
        return getJson(url, null);
    }

    public static JSONObject getJson(String url, Integer timeout) {
        RequestConfig requestConfig = null;
        if(timeout != null) {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .build();
        }
        return JSON.parseObject(get(null, url, requestConfig));
    }

    public static String get(CookieStore cookieStore, String url) {
        return get(cookieStore, url, null);
    }

    /**
     * 传入指定的 CookieStore
     *
     * @param cookieStore
     * @param url
     * @return
     */
    public static String get(CookieStore cookieStore, String url, RequestConfig requestConfig) {
        HttpGet get = new HttpGet(url);
        if(requestConfig != null) get.setConfig(requestConfig);
        try {
            return EntityUtils.toString(
                    client().execute(get, getContextWithCookieStore(cookieStore)).getEntity(),
                    "UTF-8"
            );
        } catch(IOException e) {
            e.printStackTrace();
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    /**
     * 传入指定的 CookieStore, 并返回 HttpClientContext 对象
     * <p>
     * PS: HttpClientContext 可以得到:
     * 1. Request(getRequest())
     * 2. Response(getResponse())
     *
     * @param cookieStore
     * @param url
     * @return
     */
    public static HttpClientContext request(CookieStore cookieStore, String url) {
        try {
            HttpClientContext context = getContextWithCookieStore(cookieStore);
            client().execute(new HttpGet(url), context);
            return context;
        } catch(IOException e) {
            e.printStackTrace();
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.E(e));
            return null;
        }
    }

    public static String toString(HttpResponse reponse) {
        if(reponse != null) {
            try {
                return EntityUtils.toString(reponse.getEntity(), "UTF-8");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return "";
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
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<>(params), Consts.UTF_8));
            return EntityUtils.toString(
                    client().execute(post, getContextWithCookieStore(cookieStore)).getEntity(),
                    Consts.UTF_8
            );
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }

    /**
     * 传入指定的 CookieStore 和 Headers
     *
     * @param cookieStore
     * @param url
     * @param headers
     * @param params
     * @return
     */
    public static String post(CookieStore cookieStore, String url, List<BasicHeader> headers,
                              Collection<? extends NameValuePair> params) {

        RequestBuilder requestBuilder = RequestBuilder.post().setUri(url);
        if(headers != null && !headers.isEmpty()) {
            for(BasicHeader header : headers) requestBuilder.setHeader(header);
        }
        requestBuilder.setEntity(new UrlEncodedFormEntity(new ArrayList<>(params), Consts.UTF_8));
        try {
            CloseableHttpResponse response = client()
                    .execute(requestBuilder.build(), getContextWithCookieStore(cookieStore));
            return EntityUtils.toString(response.getEntity(), Consts.UTF_8);
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
     * 由于下载的时间比较长所以这里单独准备一个 RequestConfig
     *
     * @return
     */
    public static RequestConfig downloadRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(90))
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(90))
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(90))
                .build();
    }

    /**
     * 最简单的下载
     *
     * @param url
     * @return
     */
    public static byte[] getDown(String url) {
        HttpGet get = new HttpGet(url);
        get.setConfig(downloadRequestConfig());
        try {
            return EntityUtils.toByteArray(client().execute(get).getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.getDown[%s] [%s]", url, Webs.E(e));
            return new byte[]{};
        }
    }

    public static F.Option<File> getDownFile(String url, String fileName, CookieStore cookie) {
        HttpGet get = new HttpGet(url);
        get.setConfig(downloadRequestConfig());
        try {
            File file = new File(String.format("%s/%s", Constant.TMP, fileName));
            IO.copy(client().execute(get, getContextWithCookieStore(cookie)).getEntity().getContent(),
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
        post.setConfig(downloadRequestConfig());
        try {
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<NameValuePair>(params), "UTF-8"));
            return EntityUtils.toByteArray(client().execute(post, getContextWithCookieStore(cookieStore)).getEntity());
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
        post.setConfig(downloadRequestConfig());
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
            return EntityUtils.toString(client().execute(post, getContextWithCookieStore(cookieStore)).getEntity());
        } catch(Exception e) {
            e.printStackTrace();
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.E(e));
            return "";
        }
    }


    // -------------------- body string ----------------------

    public static String post(String url, String body) {
        return post(url, body, null);
    }

    public static String post(String url, String body, RequestConfig requestConfig) {
        HttpPost post = new HttpPost(url);
        if(requestConfig != null) post.setConfig(requestConfig);
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
        String json = post(url, body, null);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    /**
     * 支持自定义超时时间的 post 请求
     * 现阶段只有设置超时时间的需求,如果以后还有其他需求再重构成传递一个 RequestConfig
     *
     * @param url
     * @param body
     * @param timeout
     * @return
     */
    public static JSONObject postJson(String url, String body, Integer timeout) {
        Logger.debug("HTTP.post Json [%s]", url);
        RequestConfig requestConfig = null;
        if(timeout != null) {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .build();
        }
        String json = post(url, body, requestConfig);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    public static JSONObject getJson(CookieStore cookieStore, String url) {
        Logger.debug("HTTP.get Json [%s]", url);
        String json = get(cookieStore, url, null);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    public static BasicClientCookie buildCrossDomainCookie(String cookieName, String cookieValue) {
        BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);
        String domain = String.format(".%s", models.OperatorConfig.getVal("domain"));
        cookie.setDomain(domain);
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
        cookie.setAttribute(ClientCookie.PATH_ATTR, "/");
        return cookie;
    }
}
