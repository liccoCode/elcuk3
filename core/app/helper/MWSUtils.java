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

import javax.xml.bind.JAXB;
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
        },
        UPDATE_PRODUCT {
            @Override
            public String toString() {
                return "_POST_PRODUCT_DATA_";
            }
        }
    }

    public static String submintFeedByXML(Feed feed, T feedType, M.MID marketId, Account account) {
        MarketplaceWebService service = mws.MWSReports.client(account);
        SubmitFeedRequest req = new SubmitFeedRequest().withMerchant(account.merchantId).withFeedType(feedType.toString());
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

    public static String buildXMLBySelling(Selling selling, SellingAmzPost p) {
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
            Dimensions dimensions = new Dimensions();
            data.setItemDimensions(dimensions);
            if(p.productvolume) {
                LengthDimension lengthDimension = new LengthDimension();
                lengthDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                lengthDimension.setValue(new BigDecimal(p.productLengths));
                data.getItemDimensions().setLength(lengthDimension);
                LengthDimension widthDimension = new LengthDimension();
                widthDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                widthDimension.setValue(new BigDecimal(p.productWidth));
                data.getItemDimensions().setWidth(widthDimension);
                LengthDimension heightDimension = new LengthDimension();
                heightDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                heightDimension.setValue(new BigDecimal(p.productHeigh));
                data.getItemDimensions().setHeight(heightDimension);
            }
            if(p.weight) {
                WeightDimension weightDimension = new WeightDimension();
                weightDimension.setUnitOfMeasure(WeightUnitOfMeasure.fromValue(p.weightUnit));
                weightDimension.setValue(new BigDecimal(p.proWeight));
                data.getItemDimensions().setWeight(weightDimension);
            }
        }
        if(StringUtils.isNotBlank(selling.listing.product.partNumber)) {
            if(selling.market.toString().equals("AMAZON_JP") &&
                    StringUtils.isNotBlank(selling.listing.product.partNumberJP)) {
                data.setMfrPartNumber(selling.listing.product.partNumberJP);
            } else {
                data.setMfrPartNumber(selling.listing.product.partNumber);
            }
        }
        if(p.searchtermss) {
            data.getSearchTerms().clear();
            for(String word : selling.aps.searchTermss) {
                if(StringUtils.isNotBlank(word)) {
                    data.getSearchTerms().add(word);
                }
            }
        }
        product.setDescriptionData(data);
        String xml = JaxbUtil.convertToXml(product);
        return xml;
    }


}
