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
    // TODO: 这里应该改变为环境变量, 而非数据库
    public static final String ELCUK2_ES_HOST = "http://"+models.OperatorConfig.getVal("elcuk2es");
    public static final String ETRACKER_ES_HOST = "http://"+models.OperatorConfig.getVal("etrackeres")+":9200";

    public static JSONObject count(String index, String type, SearchSourceBuilder builder) {
        return HTTP.postJson(ELCUK2_ES_HOST + "/" + index + "/" + type + "/_search", builder.toString());
    }


    public static JSONObject search(String index, String type, SearchSourceBuilder builder) {
        return processSearch(index, type, builder, ELCUK2_ES_HOST);
    }

    public static JSONObject searchOnEtrackerES(String index, String type, SearchSourceBuilder builder) {
        return processSearch(index, type, builder, ETRACKER_ES_HOST);
    }

    public static JSONObject processSearch(String index, String type, SearchSourceBuilder builder, String esHost) {
        return HTTP.postJson(esHost + "/" + index + "/" + type + "/_search", builder.toString());
    }

    public static JSONObject get(String index, String type, String id) {
        return processGet(index, type, id, ELCUK2_ES_HOST);
    }

    public static JSONObject getOnEtrackerES(String index, String type, String id) {
        return processGet(index, type, id, ETRACKER_ES_HOST);
    }

    public static JSONObject processGet(String index, String type, String id, String esHost) {
        return HTTP.getJson(esHost + "/" + index + "/" + type + "/" + id);
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
        esfield = esfield.replace("-", "").replace(",", "").replace("|", "").replace(".", "");
        return esfield;
    }
}
