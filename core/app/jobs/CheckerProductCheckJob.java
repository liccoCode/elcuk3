package jobs;

import helper.LogUtils;
import models.Jobex;
import models.product.Product;
import models.view.dto.AnalyzeDTO;
import models.view.post.AnalyzePost;
import notifiers.SystemMails;
import play.jobs.Job;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * Product 的规范性检查.
 *  * 周期:
 * - 轮询周期: 1d
 * - Duration: 0 10 0 (* /3) * ?
 * </pre>
 * User: wyattpan
 * Date: 8/24/12
 * Time: 12:22 PM
 * @deprecated
 */
public class CheckerProductCheckJob extends Job {
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(CheckerProductCheckJob.class.getName()).isExcute()) return;
        /**
         * 1. 现在仅检查 Product 的图片问题
         */
        AnalyzePost post = new AnalyzePost();
        post.type = "sku";
        List<AnalyzeDTO> skuSales = post.analyzes();
        List<F.T2<Product, AnalyzeDTO>> thisTimeWarnningProduct = new ArrayList<F.T2<Product, AnalyzeDTO>>();
        for(AnalyzeDTO skuSale : skuSales) {
            if(thisTimeWarnningProduct.size() >= 10) break;
            Product prod = Product.findByMerchantSKU(skuSale.fid);
            if(prod.pictureCount() >= 4) continue;
            thisTimeWarnningProduct.add(new F.T2<Product, AnalyzeDTO>(prod, skuSale));
        }

        SystemMails.productPicCheckermail(thisTimeWarnningProduct);
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"CheckerProductCheckJob")) {
            LogUtils.JOBLOG.info(String
                    .format("CheckerProductCheckJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }
}
