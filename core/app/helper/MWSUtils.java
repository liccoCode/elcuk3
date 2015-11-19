package helper;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.model.*;
import com.elcuk.jaxb.*;
import com.google.common.collect.Lists;
import models.market.Account;
import models.market.Feed;
import models.market.M;
import models.market.Selling;
import models.view.post.SellingAmzPost;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Override;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by licco on 15/11/16.
 */
public class MWSUtils {

    private static final String CHARACTER_ENCODING = "UTF-8";
    final static String ALGORITHM = "HmacSHA256";


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
        try {
            req.setContentMD5(MWSUtils.computeContentMD5Header(content));
            req.setContentType(ContentType.XML);
            SubmitFeedResponse resp = service.submitFeed(req);
            return resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String calculateStringToSignV2(
            Map<String, String> parameters, String serviceUrl) throws SignatureException, URISyntaxException {
        // Sort the parameters alphabetically by storing
        // in TreeMap structure
        Map<String, String> sorted = new TreeMap<String, String>();
        sorted.putAll(parameters);

        // Set endpoint value
        URI endpoint = new URI(serviceUrl.toLowerCase());

        // Create flattened (String) representation
        StringBuilder data = new StringBuilder();
        data.append("POST\n");
        data.append(endpoint.getHost());
        data.append("\n/");
        data.append("\n");

        Iterator<Map.Entry<String, String>> pairs = sorted.entrySet().iterator();
        while(pairs.hasNext()) {
            Map.Entry<String, String> pair = pairs.next();
            if(pair.getValue() != null) {
                data.append(pair.getKey() + "=" + pair.getValue());
            } else {
                data.append(pair.getKey() + "=");
            }
            if(pairs.hasNext()) {
                data.append("&");
            }
        }
        return data.toString();
    }

    /*
     * Sign the text with the given secret key and convert to base64
     */
    private static String sign(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException,
            IllegalStateException, UnsupportedEncodingException {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secretKey.getBytes(CHARACTER_ENCODING),
                ALGORITHM));
        byte[] signature = mac.doFinal(data.getBytes(CHARACTER_ENCODING));
        String signatureBase64 = new String(Base64.encodeBase64(signature),
                CHARACTER_ENCODING);
        return new String(signatureBase64);
    }

    private static String urlEncode(String rawValue) {
        String value = (rawValue == null) ? "" : rawValue;
        String encoded = null;

        try {
            encoded = URLEncoder.encode(value, CHARACTER_ENCODING)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch(UnsupportedEncodingException e) {
            System.err.println("Unknown encoding: " + CHARACTER_ENCODING);
            e.printStackTrace();
        }
        return encoded;
    }

    public static String computeContentMD5Header(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        DigestInputStream s;
        try {
            DigestInputStream dis = new DigestInputStream(inputStream, MessageDigest.getInstance("MD5"));
            byte[] buffer = new byte[8192];
            while(dis.read(buffer) > 0) ;

            String md5Content = new String(org.apache.commons.codec.binary.Base64.encodeBase64(
                    dis.getMessageDigest().digest()));

            // Effectively resets the stream to be beginning of the file
            // via a FileChannel.

            return md5Content;
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
        AmazonEnvelope envelope = new AmazonEnvelope();
        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());
        envelope.setHeader(header);
        envelope.setMessageType("Product");
        envelope.setPurgeAndReplace(false);

        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));
        message.setOperationType("PartialUpdate");

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
        message.setProduct(product);
        envelope.getMessage().add(message);
        String xml = JaxbUtil.convertToXml(envelope);
        return xml;
    }


}
