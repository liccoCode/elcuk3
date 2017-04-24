package models;

import helper.Caches;
import play.cache.Cache;
import play.data.validation.IPv4Address;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.List;

/**
 * 项目说使用的不同的服务器
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:57
 * @deprecated 无需使用
 */
@Entity
public class Server extends Model {
    public Server() {
    }

    public Server(String url) {
        this.url = url;
    }

    /**
     * Server 的类型
     */
    public enum T {
        CRAWLER,
        /**
         * 普通服务器
         */
        SERVER
    }


    @Column(nullable = false)
    @Required
    public String name;

    public String username;

    public String password;

    @Column(nullable = false, unique = true)
    @Required
    public String url;

    @IPv4Address
    @Required
    public String ipAddress;

    public T type = T.CRAWLER;

    public Float ratio = 1.0f;

    @SuppressWarnings("unchecked")
    public static Server server(T type) {
        //Crawler 将负载均衡交给 nginx 去处理
        if(type == T.CRAWLER) return new Server("http://crawl.easya.cc");
        List<Server> servers = (List<Server>) Cache.get(String.format(Caches.SERVERS, type.toString()));
        if(servers == null || servers.size() == 0) { // 每次缓存 5 分钟
            synchronized(Server.class) {
                if(servers == null || servers.size() == 0) {
                    servers = Server.find("type=?", type).fetch();
                    Cache.add(String.format(Caches.SERVERS, type.toString()), servers, "5mn");
                }
            }
        }
        return servers.get(0);
    }
}
