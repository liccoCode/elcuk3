package helper;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.model.*;
import com.google.common.collect.Lists;
import models.market.Account;
import models.market.Feed;
import models.market.M;
import models.market.Selling;
import models.view.post.SellingAmzPost;
import mws.product.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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

    public static String submitFeedByXML(Feed feed, T feedType, M.MID marketId, Account account) {
        MarketplaceWebService service = mws.MWSReports.client(account);
        SubmitFeedRequest req = new SubmitFeedRequest().withMerchant(account.merchantId)
                .withFeedType(feedType.toString());

        if(marketId != null) {
            req.withMarketplaceIdList(new IdList(Lists.newArrayList(marketId.name())));
        }
        InputStream content = new ByteArrayInputStream(feed.content.getBytes());
        req.setFeedContent(content);
        Logger.info("#####" + DigestUtils.md5Hex(feed.content));
        req.setContentMD5(DigestUtils.md5Hex(feed.content));
        req.setContentType(ContentType.XML);
        try {
            SubmitFeedResponse resp = service.submitFeed(req);
            return resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String computeContentMD5Header(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        DigestInputStream s;
        try {
            s = new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            byte[] buffer = new byte[8192];
            while(s.read(buffer) > 0) ;
            s.getMessageDigest().update(buffer);
            return new String(org.apache.commons.codec.binary.Base64.encodeBase64(s.getMessageDigest().digest()));
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 使用方法：
    // 获取32位md5加密密文:getMd5Value("sub");
    // 获取16位md5加密密文:getMd5Value("sub").substring(8, 24);
    public static String getMD5ofStr(String xml) {
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(xml.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();
            for(int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if(i < 0)
                    i += 256;
                if(i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString().toLowerCase();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Base64加密
     */
    public static String encodeToBase64(String srcString) {
        return (new sun.misc.BASE64Encoder()).encode(srcString.getBytes());
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
