package models.view.post;

import models.finance.Apply;
import play.libs.F;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/2/13
 * Time: 3:25 PM
 */
public class ApplyPost extends Post<Apply> {

    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }

    public List<Apply> query() {
        throw new UnsupportedOperationException("请自行实现");
    }
}
