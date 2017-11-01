package models.view.post;

import models.product.Family;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/1
 * Time: 上午11:38
 */
public class FamilyPost extends Post<Family> {

    private static final long serialVersionUID = 8793345282133659864L;

    public List<String> categories = new ArrayList<>();

    public FamilyPost() {
        this.perSize = 25;
    }


    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("SELECT DISTINCT p FROM Family p WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if(categories.size() > 0) {
            sbd.append(" AND p.category.categoryId IN ").append(SqlSelect.inlineParam(categories));
        }
        if(StringUtils.isNotBlank(this.search)) {
            String word = this.word();
            sbd.append("AND (p.family LIKE ? OR p.brand.name LIKE ? OR p.category.name LIKE ? )");
            for(int i = 0; i < 3; i++) params.add(word);
        }
        return new F.T2<>(sbd.toString(), params);
    }

    public List<Family> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);
        return Family.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return (long) Family.find(params._1, params._2.toArray()).fetch().size();
    }

    @Override
    public Long getTotalCount() {
        return this.count();
    }

}
