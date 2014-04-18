package helper;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.search.builder.SearchSourceBuilder;

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
    public static final String ES_HOST = "http://it.easya.cc:9200";

    public static JSONObject search(String index, String type, SearchSourceBuilder builder) {
        return HTTP.postJson(ES_HOST + "/" + index + "/" + type + "/_search", builder.toString());
    }

    public static JSONObject get(String index, String type, String id) {
        return HTTP.getJson(ES_HOST + "/" + index + "/" + type + "/" + id);
    }

    /**
     * ES中存在特殊字符-,|符号的，作转义处理
     *
     * @param esfield
     * @return
     */
    public static String parseEsString(String esfield) {
        if(esfield == null)
            return null;
        esfield = esfield.replace("-", "").replace(",", "").replace("|", "");
        return esfield;
    }
}
