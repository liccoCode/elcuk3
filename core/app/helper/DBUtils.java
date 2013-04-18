package helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.DB;
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
        Map<String, Object> row = new HashMap<String, Object>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; ) ps.setObject(++i, params[(i > 0 ? i - 1 : 0)]);

            loger.debug(String.format("%s -> %s", sql, Arrays.toString(params)));

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData mete = ps.getMetaData();

            int rowSize = 0;
            while(rs.next()) {
                ++rowSize;
                if(rowSize >= 2) throw new FastRuntimeException("Only Deal one Row!");

                for(int i = 1; i <= mete.getColumnCount(); i++) {
                    Object value = rs.getObject(i);
                    if(value != null && value.getClass() == Timestamp.class) {
                        Timestamp dateValue = (Timestamp) value;
                        row.put(mete.getColumnLabel(i), new Date(dateValue.getTime()));
                    } else {
                        row.put(mete.getColumnLabel(i), value);
                    }
                }
            }
            rs.close();
            ps.close();
        } catch(Exception e) {
            throw new FastRuntimeException(e);
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
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; ) ps.setObject(++i, params[(i > 0 ? i - 1 : 0)]);

            loger.debug(String.format("%s -> %s", sql, Arrays.toString(params)));

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData mete = ps.getMetaData();

            while(rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>();

                for(int i = 1; i <= mete.getColumnCount(); i++) {
                    row.put(mete.getColumnLabel(i), rs.getObject(i));
                }

                rows.add(row);
            }

        } catch(Exception e) {
            throw new FastRuntimeException(e);
        }
        return rows;
    }
}
