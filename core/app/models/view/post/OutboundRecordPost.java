package models.view.post;

import models.whouse.OutboundRecord;
import play.libs.F;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 5:57 PM
 */
public class OutboundRecordPost extends Post<OutboundRecord> {
    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }
}
