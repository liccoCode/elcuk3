package query;

import helper.DBUtils;
import play.i18n.Messages;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 3:58 PM
 */
public class ElcukRecordQuery {
    /**
     * 返回一行数据
     *
     * @param from
     * @param to
     * @param lineType
     * @return
     */
    public Map<String, List<Integer>> emails(Date from, Date to, String lineType) {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT action, count(*) as c, " +
                "date_format(createAt,'%Y-%m-%d') as date FROM ElcukRecord WHERE" +
                " createAt>=? AND createAt<=? AND action=? AND fid=?" +
                " GROUP BY date_format(createAt, '%Y-%m-%d')",
                from, to, Messages.get("email.record"), lineType);

        Map<String, List<Integer>> line = new HashMap<String, List<Integer>>();
        List<Integer> points = new ArrayList<Integer>();
        for(Map<String, Object> row : rows) {
            points.add(((Long) row.get("c")).intValue());
        }
        line.put(lineType, points);
        return line;
    }
}
