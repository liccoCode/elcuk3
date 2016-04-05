package models.view.post;

import models.whouse.StockRecord;
import play.libs.F;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 6:00 PM
 */
public class StockRecordPost extends Post<StockRecord> {
    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }
}
