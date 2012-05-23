package controllers;

import ext.LinkExtensions;
import models.Ret;
import models.market.Listing;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
@With({Secure.class, GzipFilter.class})
public class Sellings extends Controller {


    /**
     * 将指定 merchantSKU 的 Selling 与指定的 listingId 进行关联
     *
     * @param msku
     * @param listingId
     */
    public static void assoListing(String msku, String listingId) {
        validation.required(msku);
        validation.required(listingId);

        Selling selling = Selling.find("merchantSKU=?", msku).first();
        Validate.notNull(selling);
        Listing listing = Listing.find("listingId=?", listingId).first();
        Validate.notNull(listing);
        selling.listing = listing;
        selling.save();
    }

    public static void selling(String sid) {
        Selling s = Selling.findById(sid);
        s.aps.arryParamSetUP(-1);
        render(s);
    }

    public static void imageUpload(Selling s, String imgs) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ")" + "不存在!"));
        if(StringUtils.isBlank(s.aps.imageName) && StringUtils.isBlank(imgs)) renderJSON(new Ret("图片信息不能为空!"));
        s.uploadAmazonImg(imgs, false);
        renderJSON(new Ret(true, LinkExtensions.asinLink(s)));
    }
}
