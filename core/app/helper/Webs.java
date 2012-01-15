package helper;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-11
 * Time: 上午3:55
 */
public class Webs {

    public static final String SPLIT = "|-|";

    /**
     * <pre>
     * 用来对 Pager 与 pageSize 的值进行修正;
     * page: 1~N
     * size: 1~100 (-> 20)
     * </pre>
     *
     * @param p
     * @param s
     */
    public static void fixPage(Integer p, Integer s) {
        if(p == null || p < 0) p = 1; // 判断在页码
        if(s == null || s < 1 || s > 100) s = 20; // 判断显示的条数控制 
    }

}
