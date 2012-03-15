package helper;

import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import play.Play;

import java.io.IOException;

/**
 * Play 中利用 AsyncHttpClient 的 HTTP 请求
 * User: wyattpan
 * Date: 3/14/12
 * Time: 12:02 PM
 */
public class HTTP {
    private static DefaultHttpClient client;

    public static synchronized void init() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, org.apache.http.protocol.HTTP.UTF_8);
        HttpProtocolParams.setUserAgent(params, Play.configuration.getProperty("http.userAgent"));
        HttpClientParams.setRedirecting(params, true);
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(), params);
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

    public static synchronized void stop() {
        client = null;
    }

    public static DefaultHttpClient client() {
        if(client == null) throw new IllegalAccessError("Application is not startup have some error!");
        return client;
    }

    public static String get(HttpUriRequest get) throws IOException {
        return EntityUtils.toString(client().execute(get).getEntity());
    }

}
