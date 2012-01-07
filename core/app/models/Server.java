package models;

import play.cache.Cache;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

/**
 * 项目说使用的不同的服务器
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:57
 */
@Entity
public class Server extends Model {
    /**
     * Server 的类型
     */
    public enum T {
        CRAWLER
    }


    @Column(nullable = false)
    public String name;

    public String username;

    public String password;

    @Column(nullable = false, unique = true)
    public String url;

    public T type = T.CRAWLER;

    public Float ratio = 1.0f;

    @SuppressWarnings("unchecked")
    public static Server server(T type) {
        List<Server> servers = (List<Server>) Cache.get("Server_" + type.toString());
        if(servers == null || servers.size() == 0) { // 每次缓存 5 分钟
            servers = Server.find("type=?", type).fetch();
            Cache.add("Server_" + type.toString(), servers, "5mn");
        }
        //TODO 根据 ratio 计算获取哪一个
        return servers.get(0);
    }
}
