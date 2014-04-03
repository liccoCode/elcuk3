package controllers;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.categoryInfo.CategoryInfoFetchJob;
import models.User;
import models.product.Category;
import models.view.dto.CategoryInfoDTO;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-27
 * Time: AM10:19
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class CategoryInfos extends Controller {

    @Check("categoryinfos.show")
    public static void show(String id) {
        User user = User.findByUserName(Secure.Security.connected());
        List<Category> cates = User.getTeamCategorys(user);
        if(StringUtils.isBlank(id) && cates.size() > 0) id = cates.get(0).categoryId;
        List<CategoryInfoDTO> dtos = CategoryInfoDTO.query(id);
        render(cates, dtos);
    }
}
