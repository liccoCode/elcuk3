package models.view.post;

import helper.Dates;
import models.procure.Deliveryment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/4/12
 * Time: 5:09 PM
 */
public class DeliveryPost extends Post {
    /**
     * 解析 Id 的正则表达式
     */
    private static final Pattern ID = Pattern.compile("^(\\w{2}\\|\\d{6}\\|\\d{2})$");

    /**
     * 解析 +N 这样的数字, 解析出含有 N 个 ProcureUnit 的 Deliveryment
     */
    private static final Pattern SIZE = Pattern.compile("^\\+(\\w*)$");

    public Deliveryment.S state;

    @Override
    public F.T2<String, List<Object>> params() {
        F.T3<Boolean, String, List<Object>> specialSearch = deliverymentId();

        // 针对 Id 的唯一搜索
        if(specialSearch._1) return new F.T2<String, List<Object>>(specialSearch._2, specialSearch._3);

        // +n 处理需要额外的搜索
        specialSearch = multiProcureUnit();

        StringBuilder sbd = new StringBuilder("SELECT DISTINCT d FROM Deliveryment d LEFT JOIN d.units u WHERE");
        List<Object> params = new ArrayList<Object>();
        sbd.append(" d.createDate>=? AND d.createDate<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(specialSearch._1) {
            sbd.append(" AND ").append(specialSearch._2).append(" ");
            params.addAll(specialSearch._3);
        }

        if(this.state != null) {
            sbd.append(" AND d.state=?");
            params.add(this.state);
        }

        if(StringUtils.isNotBlank(this.search) && !specialSearch._1) {
            String word = this.word();
            sbd.append(" AND (")
                    .append(" u.sid LIKE ?")
                    .append(" OR d.name LIKE ?")
                    .append(")");
            for(int i = 0; i < 3; i++) params.add(word);
        }

        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public List<Deliveryment> query() {
        F.T2<String, List<Object>> params = params();
        return Deliveryment.find(params._1 + " ORDER BY d.createDate DESC", params._2.toArray()).fetch();
    }

    public F.T3<Boolean, String, List<Object>> multiProcureUnit() {
        if(StringUtils.isNotBlank(this.search)) {
            this.search = this.search.trim();
            Matcher matcher = SIZE.matcher(this.search);
            if(matcher.find()) {
                int size = NumberUtils.toInt(matcher.group(1));
                return new F.T3<Boolean, String, List<Object>>(true, "SIZE(d.units)>=?", new ArrayList<Object>(Arrays.asList(size)));
            }
        }
        return new F.T3<Boolean, String, List<Object>>(false, null, null);
    }

    /**
     * 通过 Id 搜索 Deliveryment
     *
     * @return
     */
    private F.T3<Boolean, String, List<Object>> deliverymentId() {
        if(StringUtils.isNotBlank(this.search)) {
            this.search = this.search.trim();
            Matcher matcher = ID.matcher(this.search);
            if(matcher.find()) {
                String deliverymentId = matcher.group(1);
                return new F.T3<Boolean, String, List<Object>>(true, "SELECT d FROM Deliveryment d WHERE d.id=?", new ArrayList<Object>(Arrays.asList(deliverymentId)));
            }
        }
        return new F.T3<Boolean, String, List<Object>>(false, null, null);
    }

}
