package query;

import helper.DBUtils;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/23/13
 * Time: 9:46 AM
 */
public class SellingQuery {

    /**
     * 返回分析页面的 AnalyzeDTO 数据
     *
     * @return
     */
    public List<AnalyzeDTO> analyzePostDTO() {
        SqlSelect sql = new SqlSelect()
                .select("s.sellingId", "s.asin", "s.ps", "s.account_id")
                .from("Selling s");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        List<AnalyzeDTO> dtos = new ArrayList<AnalyzeDTO>();

        for(Map<String, Object> row : rows) {
            AnalyzeDTO dto = new AnalyzeDTO(row.get("sellingId").toString());
            dto.asin = row.get("asin").toString();
            dto.aid = row.get("account_id").toString();
            dto.ps = NumberUtils.toFloat(row.get("ps").toString());
            dtos.add(dto);
        }
        return dtos;
    }
}
