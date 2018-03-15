package helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
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

    private HTTP() {
    }

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
                .setSocketTimeout((int) TimeUnit.SECONDS.toMillis(5)) // 请求获取数据的超时时间
                .setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5)) // 连接超时时间
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(1)) // 从 pool 获取 connection超时时间
                .setRedirectsEnabled(true) //允许 Redirect
                .setCircularRedirectsAllowed(true)
                .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(12); // 每一个站点最多只允许 12 个链接(request pool 的两倍)
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
                                || method.equalsIgnoreCase(HttpHead.METHOD_NAME))
                                && locationHeader != null;
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

        // 注册 Cookie 策略(http://hc.apache.org/httpcomponents-client-ga/tutorial/html/statemgmt.html#d5e499)
        PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        Registry<CookieSpecProvider> cookieSpecProviderRegistry = RegistryBuilder.<CookieSpecProvider>create()
                .register(CookieSpecs.DEFAULT,
                        new DefaultCookieSpecProvider(publicSuffixMatcher))
                .register("amazon", new AmazonCookieSpecProvider())
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(connManager)
                .setDefaultConnectionConfig(connectionConfig)
                .setUserAgent(Play.configuration.getProperty("http.userAgent")) // UserAgent
                .addInterceptorLast(new RequestAcceptEncoding())
                .addInterceptorLast(new ResponseContentEncoding())
                .setRedirectStrategy(defaultRedirectStrategy)
                .setDefaultCookieStore(HTTP.COOKIE_STORE)
                .setDefaultCookieSpecRegistry(cookieSpecProviderRegistry)
                .build();
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
     * 返回包含了传入的 CookieStore 的 HttpClientContext
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

    public static RequestConfig requestConfigWithTimeout(int timeout) {
        if(timeout <= 0) return null;
        return RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout((int) TimeUnit.SECONDS.toMillis(1))
                .build();
    }

    public static BasicClientCookie buildCrossDomainCookie(String cookieName, String cookieValue) {
        BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);
        String domain = String.format(".%s", models.OperatorConfig.getVal("domain"));
        cookie.setDomain(domain);
        cookie.setAttribute(ClientCookie.DOMAIN_ATTR, domain);
        cookie.setAttribute(ClientCookie.PATH_ATTR, "/");
        return cookie;
    }

    public static CloseableHttpResponse doRequest(HttpRequestBase request) throws IOException {
        return doRequest(request, null, null);
    }

    public static CloseableHttpResponse doRequest(HttpRequestBase request, HttpClientContext context)
            throws IOException {
        return doRequest(request, context, null);
    }

    public static CloseableHttpResponse doRequest(HttpRequestBase request, RequestConfig config) throws IOException {
        return doRequest(request, null, config);
    }

    public static CloseableHttpResponse doRequest(HttpRequestBase request,
                                                  HttpClientContext context,
                                                  RequestConfig config) throws IOException {
        if(config != null) request.setConfig(config);
        return client().execute(request, context);
    }

    public static void closeResponse(CloseableHttpResponse response) {
        try {
            if(response != null) response.close();
        } catch(IOException e) {
            Logger.warn("关闭 Response 时出现错误!", Webs.e(e));
        }
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

    public static JSONObject getJson(String url, RequestConfig requestConfig) {
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
        CloseableHttpResponse response = null;
        try {
            response = doRequest(get, getContextWithCookieStore(cookieStore), requestConfig);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch(IOException e) {
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.s(e));
            return "";
        } finally {
            closeResponse(response);
        }
    }

    public static String get(HttpGet get) {
        CloseableHttpResponse response = null;
        try {
            response = HTTP.client().execute(get);
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch(IOException e) {
            Logger.warn("HTTP.get[%s] [%s]", get.getURI().toString(), Webs.s(e));
            return "";
        } finally {
            closeResponse(response);
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
    public static HttpClientContext request(CookieStore cookieStore, String url, RequestConfig requestConfig) {
        CloseableHttpResponse response = null;
        try {
            HttpClientContext context = getContextWithCookieStore(cookieStore);
            response = doRequest(new HttpGet(url), context, requestConfig);
            return context;
        } catch(IOException e) {
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.s(e));
            return null;
        } finally {
            closeResponse(response);
        }
    }

    public static HttpClientContext request(CookieStore cookieStore, String url) {
        CloseableHttpResponse response = null;
        try {
            HttpClientContext context = getContextWithCookieStore(cookieStore);
            response = doRequest(new HttpGet(url), context);
            return context;
        } catch(IOException e) {
            Logger.warn("HTTP.get[%s] [%s]", url, Webs.s(e));
            return null;
        } finally {
            closeResponse(response);
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
        return post(cookieStore, url, null, params);
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
                              Collection<? extends NameValuePair> params, RequestConfig requestConfig) {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(new ArrayList<>(params), Consts.UTF_8));
        if(headers != null && !headers.isEmpty()) {
            for(BasicHeader header : headers) post.setHeader(header);
        }

        CloseableHttpResponse response = null;
        try {
            response = doRequest(post, getContextWithCookieStore(cookieStore), requestConfig);
            return EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.e(e));
            return "";
        } finally {
            closeResponse(response);
        }
    }

    public static String post(CookieStore cookieStore, String url, List<BasicHeader> headers,
                              Collection<? extends NameValuePair> params) {
        return post(cookieStore, url, headers, params, null);
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
        CloseableHttpResponse response = null;
        try {
            response = doRequest(get, downloadRequestConfig());
            return EntityUtils.toByteArray(response.getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.getDown[%s] [%s]", url, Webs.e(e));
            return new byte[]{};
        } finally {
            closeResponse(response);
        }
    }

    public static F.Option<File> getDownFile(String url, String fileName, CookieStore cookie) {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = doRequest(get, getContextWithCookieStore(cookie), downloadRequestConfig());
            File file = new File(String.format("%s/%s", Constant.TMP, fileName));
            IO.copy(response.getEntity().getContent(),
                    new FileOutputStream(file));
            return F.Option.Some(file);
        } catch(IOException e) {
            Logger.warn("HTTP.getDownFile[%s] [%s]", url, Webs.e(e));
            return F.Option.None();
        } finally {
            closeResponse(response);
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
        CloseableHttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(new ArrayList<>(params), "UTF-8"));
            response = doRequest(post, getContextWithCookieStore(cookieStore), downloadRequestConfig());
            return EntityUtils.toByteArray(response.getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.postDown[%s] [%s]", url, Webs.e(e));
            return new byte[]{};
        } finally {
            closeResponse(response);
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
        CloseableHttpResponse response = null;
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
            response = doRequest(post, getContextWithCookieStore(cookieStore), downloadRequestConfig());
            return EntityUtils.toString(response.getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.s(e));
            return "";
        } finally {
            closeResponse(response);
        }
    }


    // -------------------- body string ----------------------

    public static String post(String url, String body) {
        return post(url, body, null);
    }

    public static String post(String url, String body, RequestConfig requestConfig) {
        HttpPost post = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            post.setEntity(new StringEntity(body, Charset.forName("UTF-8")));
            response = doRequest(post, requestConfig);
            return EntityUtils.toString(response.getEntity());
        } catch(Exception e) {
            Logger.warn("HTTP.post[%s] [%s]", url, Webs.e(e));
            return "";
        } finally {
            closeResponse(response);
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
     * @param requestConfig
     * @return
     */
    public static JSONObject postJson(String url, String body, RequestConfig requestConfig) {
        Logger.debug("HTTP.post Json [%s]", url);
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

    public static JSONObject getJson(CookieStore cookieStore, String url, Header[] headers) {
        Logger.debug("HTTP.get Json [%s]", url);
        String json = get(cookieStore, url, null);
        try {
            return JSON.parseObject(json);
        } catch(Exception e) {
            Logger.error("Bad JSON: \n%s", json);
            throw new RuntimeException("Cannot parse JSON (check logs)", e);
        }
    }

    private static class AmazonCookieSpec implements CookieSpec {
        public final DefaultCookieSpec defaultCookieSpec;

        public AmazonCookieSpec() {
            this.defaultCookieSpec = new DefaultCookieSpec();
        }

        @Override
        public int getVersion() {
            return this.defaultCookieSpec.getVersion();
        }

        @Override
        public List<Cookie> parse(Header header,
                                  CookieOrigin origin) throws MalformedCookieException {
            Args.notNull(header, "Header");
            Args.notNull(origin, "Cookie origin");
            HeaderElement[] helems = header.getElements();
            for(final HeaderElement helem : helems) {
                //删掉 Set-Cookie 中的 version 字段
                if(helem.getParameterByName("version") != null
                        && helem.getParameterByName("expires") == null
                        && StringUtils.contains(origin.getHost(), "amazon")) {
                    header = new BasicHeader(header.getName(), StringUtils.remove(header.getValue(), "Version"));
                }
            }
            return this.defaultCookieSpec.parse(header, origin);
        }

        @Override
        public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
            this.defaultCookieSpec.validate(cookie, origin);
        }

        @Override
        public boolean match(Cookie cookie, CookieOrigin origin) {
            return this.defaultCookieSpec.match(cookie, origin);
        }

        @Override
        public List<Header> formatCookies(List<Cookie> cookies) {
            return this.defaultCookieSpec.formatCookies(cookies);
        }

        @Override
        public Header getVersionHeader() {
            return null;
        }

        @Override
        public String toString() {
            return "custom";
        }
    }

    private static class AmazonCookieSpecProvider implements CookieSpecProvider {
        public AmazonCookieSpecProvider() {
        }

        @Override
        public CookieSpec create(HttpContext context) {
            return new AmazonCookieSpec();
        }
    }
}
