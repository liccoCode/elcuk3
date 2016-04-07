package models.view.post;

import models.whouse.StockRecord;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 6:00 PM
 */
public class StockRecordPost extends Post<StockRecord> {
    public StockRecordPost() {
        this.page = 1;
        this.perSize = 20;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND stockObjId LIKE ?");
            params.add(this.word());
        }

        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<StockRecord> query() {
        F.T2<String, List<Object>> params = this.params();
        this.count = this.count(params);
        return StockRecord.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return StockRecord.count(params._1, params._2.toArray());
    }
}
