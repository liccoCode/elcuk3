package models.view.post;

import controllers.Secure;
import jobs.PmDashboard.AbnormalFetchJob;
import models.User;
import models.product.Category;
import models.view.dto.AbnormalDTO;
import play.cache.Cache;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-24
 * Time: PM5:03
 */
public class AbnormalPost {
    public enum T {
        /**
         * review 信息异常
         */
        REVIEW,

        /**
         * 昨天销售额与同期对比
         */
        DAY1,

        /**
         * 上周销售额与上上周对比
         */
        BEFOREAMOUNT,

        /**
         *
         */
        BEFOREPROFIT
    }

    /**
     * 异常信息的类型
     */
    public T abnormalType;

    public AbnormalPost() {
    }

    public AbnormalPost(T abnormalType) {
        this.abnormalType = abnormalType;
    }

    public List<AbnormalDTO> abnormal(User user) {
        Map<String, List<AbnormalDTO>> dtoMap = Cache.get(AbnormalFetchJob.AbnormalDTO_CACHE, Map.class);
        if(dtoMap == null || dtoMap.size() == 0) {
            new AbnormalFetchJob().now();
            throw new FastRuntimeException("正在后台计算中, 请 10 mn 后再尝试");
        }
        List<String> categoryIds = new ArrayList<String>();
        for(Category category : User.getTeamCategorys(user)) {
            categoryIds.add(category.categoryId);
        }
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);

        List<AbnormalDTO> dtos = dtoMap.get(this.abnormalType.toString());
        List<AbnormalDTO> filterResult = abnormalFilter(skus, dtos);
        return filterResult;
    }

    /**
     * 对异常对象进行过滤筛选
     *
     * @return
     */
    public List<AbnormalDTO> abnormalFilter(List<String> skus, List<AbnormalDTO> dtos) {
        List<AbnormalDTO> filterResult = new ArrayList<AbnormalDTO>();
        for(AbnormalDTO dto : dtos) {
            if(skus.contains(dto.sku)) {
                filterResult.add(dto);
            }
        }
        return filterResult;
    }
}
