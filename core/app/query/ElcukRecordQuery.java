package query;

import helper.DBUtils;
import play.db.helper.SqlSelect;
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
        SqlSelect sql = new SqlSelect()
                .select("count(*) as c", "date_format(createAt, '%Y-%m-%d') as date")
                .from("ElcukRecord")
                .where("createAt>=?").param(from)
                .where("createAt<=?").param(to)
                .where("action=?").param(Messages.get("email.record"))
                .where("fid=?").param(lineType)
                .groupBy("date_format(createAt, '%Y-%m-%d')");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, List<Integer>> line = new HashMap<>();
        List<Integer> points = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            points.add(((Long) row.get("c")).intValue());
        }
        line.put(lineType, points);
        return line;
    }
}
