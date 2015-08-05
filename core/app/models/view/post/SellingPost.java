package models.view.post;

import models.market.Selling;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import play.libs.F;
import java.util.List;

/**
 * Created by licco on 15/8/4.
 */
public class SellingPost extends Post<Selling>{

    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }

    public List<Selling> query(){
        this.count = Selling.find("").fetch().size();
        return  Selling.find(" ORDER BY sellingId DESC").fetch(this.page, this.perSize);
    }

    public Long getTotalCount() {
        return Selling.count();
    }


}
