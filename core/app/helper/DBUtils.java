package helper;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 直接使用 SQL 的工具方法
 * User: wyattpan
 * Date: 8/22/12
 * Time: 4:43 PM
 */
public class DBUtils {

    private DBUtils() {
    }

    private static Logger loger = LoggerFactory.getLogger(DBUtils.class);

    /**
     * 获取一行的数据, 如果数据大于一行, 会抛出 FastRuntimeException
     *
     * @param sql
     * @return
     */
    public static Map<String, Object> row(String sql, Object... params) {
        return row(DB.getConnection(), sql, params);
    }

    /**
     * 获取一行的数据, 如果数据大于一行, 会抛出 FastRuntimeException
     *
     * @param sql
     * @param conn
     * @return
     */
    public static Map<String, Object> row(Connection conn, String sql, Object... params) {
        Map<String, Object> row = new HashMap<>();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; ) ps.setObject(++i, params[(i > 0 ? i - 1 : 0)]);

            loger.debug(String.format("%s -> %s", sql, Arrays.toString(params)));

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData mete = ps.getMetaData();

            int rowSize = 0;
            while(rs.next()) {
                ++rowSize;
                if(rowSize >= 2)
                    throw new FastRuntimeException(
                            String.format("Only Deal one Row! %s -> %s", sql, Arrays.toString(params)));
                row = mapOneRow(mete, rs);
            }
            rs.close();
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Exception e) {
                play.Logger.error(Webs.S(e));
            }
        }
        return row;
    }

    /**
     * 映射 DB 一行数据进行 Map<String, Object>
     *
     * @param mete
     * @param rs
     * @return
     * @throws SQLException
     */
    private static Map<String, Object> mapOneRow(ResultSetMetaData mete, ResultSet rs) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        for(int i = 1; i <= mete.getColumnCount(); i++) {
            Object value = rs.getObject(i);
            if(value != null && value.getClass() == Timestamp.class) {
                Timestamp dateValue = (Timestamp) value;
                row.put(mete.getColumnLabel(i), new Date(dateValue.getTime()));
            } else {
                row.put(mete.getColumnLabel(i), value);
            }
        }
        return row;
    }

    /**
     * 获取多行数据
     *
     * @param sql
     * @return
     */
    public static List<Map<String, Object>> rows(String sql, Object... params) {
        return rows(DB.getConnection(), sql, params);
    }

    /**
     * 获取多行数据
     *
     * @param sql
     * @param conn
     * @return
     */
    public static List<Map<String, Object>> rows(Connection conn, String sql, Object... params) {
        List<Map<String, Object>> rows = new ArrayList<>();
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; ) ps.setObject(++i, params[(i > 0 ? i - 1 : 0)]);

            loger.debug(String.format("%s -> %s", sql, Arrays.toString(params)));

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData mete = ps.getMetaData();

            while(rs.next()) {
                rows.add(mapOneRow(mete, rs));
            }
            rs.close();
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        } finally {
            try {
                if(ps != null) ps.close();
            } catch(Exception e) {
                play.Logger.error(Webs.S(e));
            }
        }
        return rows;
    }

    public static boolean execute(String sql) {
        return DB.execute(sql);
    }

    public static String whereOr(String column, Object param) {
        String value = orlineParam(column, param);
        if(value.length() == 0) return value;
        return value;
    }

    public static long count(String sql, Object... params) {
        Map<String, Object> row = row(sql, params);
        if(row == null || !row.containsKey("count")) {
            throw new FastRuntimeException("未找到 count 结果, 请检查查询语句是否正确!");
        }
        return NumberUtils.toLong(row.get("count").toString());
    }

    public static String orlineParam(String column, Object param) {
        if(param == null) return "NULL";
        String str;
        if(param instanceof String) str = column + "=" + SqlSelect.quote(param.toString());
        else if(param instanceof Iterable<?>) {
            SqlSelect.Concat list = new SqlSelect.Concat("(", " or ", ")");
            for(Object p : (Iterable<?>) param) list.append(orlineParam(column, p));
            str = list.toString();
        } else if(param instanceof Object[]) {
            SqlSelect.Concat list = new SqlSelect.Concat("(", " or ", ")");
            for(Object p : (Object[]) param) list.append(orlineParam(column, p));
            str = list.toString();
        } else if(param instanceof Enum<?>) {
            str = column + "=" + SqlSelect.quote(param.toString());
        } else str = column + "=" + param.toString();
        return str;
    }
}
