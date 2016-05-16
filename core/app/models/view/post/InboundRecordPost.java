package models.view.post;

import helper.Dates;
import models.whouse.InboundRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/5/16
 * Time: 5:57 PM
 */
public class InboundRecordPost extends Post<InboundRecord> {
    private static final Pattern ID = Pattern.compile("^id:(\\d*)$");
    public Date from;
    public Date to;

    public InboundRecord.O origin;
    public InboundRecord.S state;

    public InboundRecordPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.minusMonths(1).toDate();
        this.to = now.toDate();
        this.perSize = 25;
        this.page = 1;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();

        Long recordId = isSearchForId();
        if(recordId != null) {
            sbd.append(" AND id=?");
            params.add(recordId);
            return new F.T2<>(sbd.toString(), params);
        }
        if(this.origin != null) {
            sbd.append(" AND origin=?");
            params.add(this.origin);
        }
        if(this.state != null) {
            sbd.append(" AND state=?");
            params.add(this.state);
        }
        if(this.from != null) {
            sbd.append(" AND createDate>=?");
            params.add(Dates.morning(this.from));
        }
        if(this.to != null) {
            sbd.append(" AND createDate<=?");
            params.add(Dates.night(this.to));
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(String.format(
                    " AND (checkTask.id='%s' OR stockObjId LIKE ? OR attributes LIKE ? OR attributes LIKE ?)",
                    this.search, this.search)
            );
            params.add(this.word());
            params.add("%\"fba\":\"" + this.search + "\"%");
            params.add("%\"procureunitId\":" + this.search + "%");
        }
        sbd.append(" ORDER BY createDate DESC");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

    @Override
    public List<InboundRecord> query() {
        this.count = this.count();
        F.T2<String, List<Object>> params = params();
        return InboundRecord.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return InboundRecord.count(params._1, params._2.toArray());
    }

    /**
     * 根据正则表达式搜索是否有类似 id:123 这样的搜索如果有则直接进行 id 搜索
     *
     * @return
     */
    private Long isSearchForId() {
        if(org.apache.commons.lang.StringUtils.isNotBlank(this.search)) {
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) return NumberUtils.toLong(matcher.group(1));
        }
        return null;
    }
}
