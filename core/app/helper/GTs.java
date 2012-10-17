package helper;

import play.modules.gtengineplugin.TemplateLoader;
import play.templates.Template;
import play.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用系统内的 GTtemplate 作为模板
 * User: wyattpan
 * Date: 5/9/12
 * Time: 11:46 AM
 */
public class GTs {
    public static final String BASE_PATH = "/app/views/gt_templates/%s.html";

    /**
     * 从默认的路径(views/gt_templates)下寻找文件进行模板内容输出; 模板都是以 html 结尾;
     * 例:
     * GTs.render('templateName', GTs.newMap('key', val).build());
     *
     * @param name , 区分大小写
     * @param args
     * @return
     */
    public static String render(String name, Map<String, Object> args) {
        Template t = TemplateLoader.load(VirtualFile.fromRelativePath(String.format(BASE_PATH, name)));
        return t.render(args);
    }

    /**
     * 通过 key 与模板源文件进行模板内容输出
     *
     * @param key
     * @param source
     * @param args
     * @return
     */
    public static String render(String key, String source, Map<String, Object> args) {
        Template t = TemplateLoader.load(key, source);
        return t.render(args);
    }

    /**
     * 通过 key 与模板源文件进行模板内容输出, 同时清理此 key 缓存的模板
     *
     * @param key
     * @param source
     * @param args
     * @param reload
     * @return
     */
    public static String render(String key, String source, Map<String, Object> args, boolean reload) {

        Template t = TemplateLoader.load(key, source, reload);
        return t.render(args);
    }

    public static MapBuilder<String, Object> newMap(String key, Object obj) {
        return new MapBuilder<String, Object>().put(key, obj);
    }

    /**
     * 为了方便使用 Map 参数向 Template 中设置值的 MapBuilder
     *
     * @param <K>
     * @param <V>
     */
    public static class MapBuilder<K, V> {
        private HashMap<K, V> innerMap = new HashMap<K, V>();

        private MapBuilder() {
        }

        public static <K, V> MapBuilder<K, V> map(K k, V v) {
            return new MapBuilder<K, V>().put(k, v);
        }

        public MapBuilder<K, V> put(K k, V v) {
            innerMap.put(k, v);
            return this;
        }

        public MapBuilder<K, V> remove(K k) {
            innerMap.remove(k);
            return this;
        }

        public MapBuilder<K, V> putAll(Map<K, V> maps) {
            innerMap.putAll(maps);
            return this;
        }

        public Map<K, V> build() {
            return innerMap;
        }
    }
}
