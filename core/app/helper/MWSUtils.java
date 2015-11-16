package helper;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.google.common.collect.Lists;
import models.market.Account;
import models.market.Feed;
import models.market.M;
import models.market.Selling;
import models.view.post.SellingAmzPost;
import mws.product.*;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;

/**
 * Created by licco on 15/11/16.
 */
public class MWSUtils {

    public enum T {
        UPLOAD_PRODUCT {
            @Override
            public String toString() {
                return "_POST_FLAT_FILE_LISTINGS_DATA_";
            }
        }
    }

    private Account account;

    public String submintFeedByXML(Feed feed, T feedType, M.MID marketId, Selling selling) {
        MarketplaceWebService service = mws.MWSReports.client(account);
        SubmitFeedRequest req = new SubmitFeedRequest().withMerchant(account.merchantId)
                .withFeedType(feedType.toString());
        if(marketId != null) {
            req.withMarketplaceIdList(new IdList(Lists.newArrayList(marketId.name())));
        }

        try {
            SubmitFeedResponse resp = service.submitFeed(req);

            return resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String buildXMLBySelling(Selling selling, SellingAmzPost p) {
        Product product = new Product();
        product.setSKU(selling.merchantSKU);
        StandardProductID standardProductID = new StandardProductID();
        standardProductID.setType("ASIN");
        standardProductID.setValue(selling.asin);
        product.setStandardProductID(standardProductID);
        Product.DescriptionData data = new Product.DescriptionData();
        data.setTitle(selling.aps.title);
        if(p.productdesc) {
            data.setDescription(selling.aps.productDesc);
        }
        if(p.keyfeturess) {
            data.getBulletPoint().clear();
            for(String text : selling.aps.keyFeturess) {
                if(StringUtils.isNotBlank(text)) {
                    data.getBulletPoint().add(text);
                }
            }
        }
        if(p.productvolume || p.productWeight) {
            if(p.productvolume) {
                LengthDimension lengthDimension = new LengthDimension();
                lengthDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                lengthDimension.setValue(new BigDecimal(p.productLengths));
                data.getItemDimensions().setLength(lengthDimension);

            }


        }

        product.setDescriptionData(data);


        return "";
    }


}
