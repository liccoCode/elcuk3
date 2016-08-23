package helper;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 简单的集成 ES 的搜索功能. 不是用 ElasticSearch 提供的 API 是因为
 * -> http://blog.florian-hopf.de/2013/05/getting-started-with-elasticsearch-part.html)
 * <p/>
 * 其无需进入到 ES 的集群中, 只需要使用简单的 Restful 接口调用即可
 * User: wyatt
 * Date: 10/24/13
 * Time: 4:19 PM
 */
public class ES {
    public static JSONObject count(String index, String type, SearchSourceBuilder builder) {
        return HTTP.postJson(System.getenv(Constant.ES_HOST) + "/" + index + "/" + type + "/_search",
                builder.toString());
    }

    public static JSONObject search(String index, String type, SearchSourceBuilder builder) {
        return processSearch(index, type, builder, System.getenv(Constant.ES_HOST));
    }

    /**
     * @param index
     * @param type
     * @param builder
     * @return
     * @deprecated
     */
    public static JSONObject searchOnEtrackerES(String index, String type, SearchSourceBuilder builder) {
        return processSearch(index, type, builder, System.getenv(Constant.ETRACKER_ES_HOST));
    }

    public static JSONObject processSearch(String index, String type, SearchSourceBuilder builder, String esHost) {
        return HTTP.postJson(esHost + "/" + index + "/" + type + "/_search",
                builder.toString(),
                (int) TimeUnit.SECONDS.toMillis(1));
    }

    public static JSONObject get(String index, String type, String id) {
        return processGet(index, type, id, System.getenv(Constant.ES_HOST));
    }

    public static JSONObject getOnEtrackerES(String index, String type, String id) {
        return processGet(index, type, id, System.getenv(Constant.ETRACKER_ES_HOST));
    }

    public static JSONObject processGet(String index, String type, String id, String esHost) {
        return HTTP.getJson(esHost + "/" + index + "/" + type + "/" + id, (int) TimeUnit.SECONDS.toMillis(2));
    }

    /**
     * ES中存在特殊字符-,|符号的，作转义处理
     *
     * @param esfield
     * @return
     */
    public static String parseEsString(String esfield) {
        if(StringUtils.isBlank(esfield)) {
            return "";
        }
        return StringUtils.replaceEach(esfield, new String[]{"-", ",", "|", "."}, new String[]{"", "", "", ""});
    }
}
