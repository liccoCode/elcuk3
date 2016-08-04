package helper;

import com.amazonaws.mws.MarketplaceWebService;
import com.amazonaws.mws.model.IdList;
import com.amazonaws.mws.model.SubmitFeedRequest;
import com.amazonaws.mws.model.SubmitFeedResponse;
import com.elcuk.jaxb.*;
import com.google.common.collect.Lists;
import models.market.Account;
import models.market.Feed;
import models.market.M;
import models.market.Selling;
import models.product.Attach;
import models.view.post.SellingAmzPost;
import org.apache.commons.lang.StringUtils;
import play.utils.FastRuntimeException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.Override;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.GregorianCalendar;


/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/11/16
 * Time: 4:12 PM
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
        PRODUCT_FEED {
            @Override
            public String toString() {
                return "_POST_PRODUCT_DATA_";
            }
        },
        PRICING_FEED {
            @Override
            public String toString() {
                return "_POST_PRODUCT_PRICING_DATA_";
            }
        },
        PRODUCT_IMAGES_FEED {
            @Override
            public String toString() {
                return "_POST_PRODUCT_IMAGE_DATA_";
            }
        },
        PRODUCT_INVENTORY_FEED {
            @Override
            public String toString() {
                return "_POST_INVENTORY_AVAILABILITY_DATA_";
            }
        }
    }

    public static String submitFeedByXML(Feed feed, T feedType, M.MID marketId, Account account) {
        MarketplaceWebService service = mws.MWSReports.client(account);
        SubmitFeedRequest req = new SubmitFeedRequest();
        req.setMerchant(account.merchantId);
        req.setFeedType(feedType.toString());
        if(marketId != null) {
            req.withMarketplaceIdList(new IdList(Lists.newArrayList(marketId.name())));
        }
        try {
            File file = new File("conf/res/content.txt");
            if(!file.exists())
                file.createNewFile();
            FileOutputStream out = new FileOutputStream(file, false);
            out.write(feed.content.getBytes());
            req.setFeedContent(new FileInputStream(file));
            SubmitFeedResponse resp = service.submitFeedFromFile(req);
            String id = resp.getSubmitFeedResult().getFeedSubmissionInfo().getFeedSubmissionId();
            feed.feedId = id;
            feed.save();
            out.close();
            return id;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String buildProductXMLBySelling(Selling selling, SellingAmzPost p) {
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
        if(selling.market.name().equals("AMAZON_CA")) {
            ConditionInfo conditionInfo = new ConditionInfo();
            conditionInfo.setConditionType("New");
            product.setCondition(conditionInfo);
        }
        Product.DescriptionData data = new Product.DescriptionData();
        data.setTitle(selling.aps.title);
        if(p.productdesc) {
            data.setDescription(selling.aps.productDesc.trim());
        }
        if(p.keyfeturess) {
            data.getBulletPoint().clear();
            for(String text : selling.aps.keyFeturess) {
                if(StringUtils.isNotBlank(text)) {
                    data.getBulletPoint().add(text.trim());
                }
            }
        }
        if(p.productvolume || p.productWeight) {
            Dimensions dimensions = new Dimensions();
            data.setItemDimensions(dimensions);
            if(p.productvolume) {
                LengthDimension lengthDimension = new LengthDimension();
                lengthDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                lengthDimension.setValue(new BigDecimal(p.productLengths).setScale(2, BigDecimal.ROUND_HALF_UP));
                data.getItemDimensions().setLength(lengthDimension);
                LengthDimension widthDimension = new LengthDimension();
                widthDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                widthDimension.setValue(new BigDecimal(p.productWidth).setScale(2, BigDecimal.ROUND_HALF_UP));
                data.getItemDimensions().setWidth(widthDimension);
                LengthDimension heightDimension = new LengthDimension();
                heightDimension.setUnitOfMeasure(LengthUnitOfMeasure.fromValue(p.volumeunit));
                heightDimension.setValue(new BigDecimal(p.productHeigh).setScale(2, BigDecimal.ROUND_HALF_UP));
                data.getItemDimensions().setHeight(heightDimension);
            }
            if(p.weight) {
                WeightDimension weightDimension = new WeightDimension();
                weightDimension.setUnitOfMeasure(WeightUnitOfMeasure.fromValue(p.weightUnit));
                weightDimension.setValue(new BigDecimal(p.proWeight).setScale(2, BigDecimal.ROUND_HALF_UP));
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
                    data.getSearchTerms().add(word.trim());
                }
            }
        }
        product.setDescriptionData(data);
        message.setProduct(product);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    public static String buildPriceXMLBySelling(Selling selling, SellingAmzPost p) {
        AmazonEnvelope envelope = new AmazonEnvelope();
        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());
        envelope.setHeader(header);
        envelope.setMessageType("Price");
        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));

        Price price = new Price();
        price.setSKU(selling.merchantSKU);
        OverrideCurrencyAmount amount = new OverrideCurrencyAmount();
        amount.setValue(new BigDecimal(selling.aps.standerPrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));
        amount.setCurrency(BaseCurrencyCodeWithDefault.fromValue(Currency.M(selling.market).toString()));
        price.setStandardPrice(amount);
        if(p.saleprice) {
            Price.Sale sale = new Price.Sale();
            DatatypeFactory dataTypeFactory;
            try {
                dataTypeFactory = DatatypeFactory.newInstance();
            } catch(DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis(selling.aps.startDate.getTime());
            sale.setStartDate(dataTypeFactory.newXMLGregorianCalendar(gc));
            gc = new GregorianCalendar();
            gc.setTimeInMillis(selling.aps.endDate.getTime());
            sale.setEndDate(dataTypeFactory.newXMLGregorianCalendar(gc));
            OverrideCurrencyAmount salePrice = new OverrideCurrencyAmount();
            salePrice.setValue(new BigDecimal(selling.aps.salePrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));
            salePrice.setCurrency(BaseCurrencyCodeWithDefault.fromValue(Currency.M(selling.market).toString()));
            sale.setSalePrice(salePrice);
            price.setSale(sale);
        }
        message.setPrice(price);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    public static String buildProductImageBySelling(Selling selling, String[] images) {
        AmazonEnvelope envelope = new AmazonEnvelope();
        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());
        envelope.setHeader(header);
        envelope.setMessageType("ProductImage");

        for(int i = 0; i < images.length; i++) {
            String location = Attach.attachImageSend(selling.sellingId.split(",")[0], images[i]);
            if(StringUtils.isBlank(location))
                throw new FastRuntimeException("填写的图片名称(" + images[i] + ")不存在! 请重新上传.");
            AmazonEnvelope.Message message = new AmazonEnvelope.Message();
            message.setMessageID(BigInteger.valueOf(i + 1));
            message.setOperationType("Update");
            ProductImage productImage = new ProductImage();
            productImage.setSKU(selling.merchantSKU);
            String imageType;
            if(i == 0) imageType = "Main";
            else imageType = "PT" + i;
            productImage.setImageType(imageType);
            productImage.setImageLocation(location);

            message.setProductImage(productImage);
            envelope.getMessage().add(message);
        }
        for(int i = images.length; i < 9; i++) {
            AmazonEnvelope.Message message = new AmazonEnvelope.Message();
            message.setMessageID(BigInteger.valueOf(i + 1));
            message.setOperationType("Delete");
            ProductImage productImage = new ProductImage();
            productImage.setSKU(selling.merchantSKU);
            String imageType;
            if(i == 0) imageType = "Main";
            else imageType = "PT" + i;
            productImage.setImageType(imageType);
            message.setProductImage(productImage);
            envelope.getMessage().add(message);
        }
        return JaxbUtil.convertToXml(envelope);
    }


    /**
     * 生成向 Amazon 上架时提交的 XML
     *
     * @return
     */
    public static String toSaleAmazonXml(Selling selling) {
        AmazonEnvelope envelope = new AmazonEnvelope();

        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());
        envelope.setHeader(header);

        envelope.setMessageType("Product");
        envelope.setPurgeAndReplace(false);

        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));
        message.setOperationType("Update");

        com.elcuk.jaxb.Product product = new com.elcuk.jaxb.Product();
        //Merchant SKU
        product.setSKU(selling.merchantSKU);
        StandardProductID standardProductID = new StandardProductID();
        standardProductID.setType("UPC");
        //UPC
        standardProductID.setValue(selling.aps.upc);

        product.setStandardProductID(standardProductID);

        ConditionInfo conditionInfo = new ConditionInfo();
        conditionInfo.setConditionType("New");
        product.setCondition(conditionInfo);

        com.elcuk.jaxb.Product.DescriptionData descriptionData = new com.elcuk.jaxb.Product.DescriptionData();
        //Title
        descriptionData.setTitle(selling.aps.title);
        //Description
        descriptionData.setDescription(selling.aps.productDesc);
        //Part Number
        descriptionData.setMfrPartNumber(selling.aps.manufacturerPartNumber);
        //GiftWrap
        descriptionData.setIsGiftMessageAvailable(selling.aps.isGiftWrap);
        //Brand
        descriptionData.setBrand("EasyAcc");
        //Manufacturer
        descriptionData.setManufacturer(selling.aps.manufacturer);
        //RBN
        if(selling.market == M.AMAZON_US) {
            descriptionData.getUsedFor().add(selling.aps.rbns.get(0));
        } else {
            descriptionData.getRecommendedBrowseNode().add(BigInteger.valueOf(Long.valueOf(selling.aps.rbns.get(0))));
        }
        // BulletPoints
        for(String keyFeturess : selling.aps.keyFeturess) {
            if(StringUtils.isNotBlank(keyFeturess)) descriptionData.getBulletPoint().add(keyFeturess);
        }
        //SearchTerms
        for(String searchTerm : selling.aps.searchTermss) {
            if(StringUtils.isNotBlank(searchTerm)) descriptionData.getSearchTerms().add(searchTerm);
        }
        product.setDescriptionData(descriptionData);

        if(!skipProductData(selling.aps.templateType, selling.aps.feedProductType)) {
            Product.ProductData productData = new Product.ProductData();
            new ProductTypeSetter(productData, selling.aps.templateType, selling.aps.feedProductType).doSet();
            product.setProductData(productData);
        }
        message.setProduct(product);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    public static String assignPriceXml(Selling selling) {
        AmazonEnvelope envelope = new AmazonEnvelope();

        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());

        envelope.setHeader(header);
        envelope.setMessageType("Price");

        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));

        Price price = new Price();
        price.setSKU(selling.merchantSKU);
        OverrideCurrencyAmount amount = new OverrideCurrencyAmount();
        amount.setValue(new BigDecimal(selling.aps.standerPrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));
        amount.setCurrency(BaseCurrencyCodeWithDefault.fromValue(Currency.M(selling.market).toString()));
        price.setStandardPrice(amount);


        Price.Sale sale = new Price.Sale();
        DatatypeFactory dataTypeFactory;
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch(DatatypeConfigurationException e) {
            throw new FastRuntimeException(e);
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(selling.aps.startDate.getTime());

        sale.setStartDate(dataTypeFactory.newXMLGregorianCalendar(gc));
        gc = new GregorianCalendar();
        gc.setTimeInMillis(selling.aps.endDate.getTime());
        sale.setEndDate(dataTypeFactory.newXMLGregorianCalendar(gc));
        OverrideCurrencyAmount salePrice = new OverrideCurrencyAmount();
        salePrice.setValue(new BigDecimal(selling.aps.salePrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));
        salePrice.setCurrency(BaseCurrencyCodeWithDefault.fromValue(Currency.M(selling.market).toString()));
        sale.setSalePrice(salePrice);
        price.setSale(sale);

        message.setPrice(price);
        envelope.getMessage().add(message);

        return JaxbUtil.convertToXml(envelope);
    }

    public static String fulfillmentByAmazonXml(Selling selling) {
        AmazonEnvelope envelope = new AmazonEnvelope();

        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.market.toMerchantIdentifier());

        envelope.setHeader(header);
        envelope.setMessageType("Inventory");

        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));
        message.setOperationType("Update");

        Inventory inventory = new Inventory();
        inventory.setSKU(selling.merchantSKU);
        inventory.setFulfillmentCenterID(selling.market.fulfillmentCenterID());
        inventory.setLookup("FulfillmentNetwork");
        inventory.setSwitchFulfillmentTo("AFN");

        message.setInventory(inventory);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    public static boolean skipProductData(String templateType, String feedProductType) {
        return "Computers".equalsIgnoreCase(templateType) && "NotebookComputer".equalsIgnoreCase(feedProductType);
    }

    private static class ProductTypeSetter {
        Product.ProductData productData;
        String templateType;
        public String feedProductType;

        ProductTypeSetter(Product.ProductData productData, String templateType, String feedProductType) {
            this.productData = productData;
            this.templateType = templateType;
            this.feedProductType = feedProductType;
        }

        void doSet() {
            if("Computers".equalsIgnoreCase(templateType)) {
                setComputers();
            } else if("ConsumerElectronics".equalsIgnoreCase(templateType)) {
                setCE();
            } else if("Wireless".equalsIgnoreCase(templateType)) {
                setWireless();
            } else if("HomeImprovement".equalsIgnoreCase(templateType)) {
                setHomeImprovement();
            } else if("Home".equalsIgnoreCase(templateType)) {
                setHome();
            } else if("Games".equalsIgnoreCase(templateType)) {
                setGames();
            } else if("Sports".equalsIgnoreCase(templateType)) {
                setSports();
            } else if("Lighting".equalsIgnoreCase(templateType)) {
                setLighting();
            } else {
                setCE();
            }
        }

        Object getInstanceByFeedProductType() {
            try {
                Class clazz = Class.forName(String.format("com.elcuk.jaxb.%s", this.feedProductType));
                return clazz.newInstance();
            } catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new FastRuntimeException(
                        String.format("您所选择的 Feed Product Type 字段[%s]可能是不被支持的, 请更换该字段后再重试一次.", this.feedProductType));
            }
        }

        void setType(Object setter, Object param) {
            try {
                Method method = setter.getClass().getDeclaredMethod(String.format("set%s", this.feedProductType),
                        param.getClass());
                method.invoke(setter, param);
            } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new FastRuntimeException(
                        String.format("您所选择的 Feed Product Type 字段[%s]可能是不被支持的, 请更换该字段后再重试一次.", this.feedProductType));
            }
        }

        void setComputers() {
            Computers computers = new Computers();
            Computers.ProductType productType = new Computers.ProductType();
            setType(productType, getInstanceByFeedProductType());
            computers.setProductType(productType);
            productData.setComputers(computers);
        }

        void setCE() {
            CE ce = new CE();
            CE.ProductType productType = new CE.ProductType();
            setType(productType, getInstanceByFeedProductType());
            ce.setProductType(productType);
            productData.setCE(ce);
        }

        void setWireless() {
            Wireless wireless = new Wireless();
            Wireless.ProductType productType = new Wireless.ProductType();
            setType(productType, getInstanceByFeedProductType());
            wireless.setProductType(productType);
            productData.setWireless(wireless);
        }

        void setHomeImprovement() {
            HomeImprovement homeImprovement = new HomeImprovement();
            HomeImprovement.ProductType productType = new HomeImprovement.ProductType();
            setType(productType, getInstanceByFeedProductType());
            homeImprovement.setProductType(productType);
            productData.setHomeImprovement(homeImprovement);
        }

        void setHome() {
            Home home = new Home();
            Home.ProductType productType = new Home.ProductType();
            setType(productType, getInstanceByFeedProductType());
            home.setProductType(productType);
            productData.setHome(home);
        }

        void setGames() {
            SoftwareVideoGames videoGames = new SoftwareVideoGames();
            SoftwareVideoGames.ProductType productType = new SoftwareVideoGames.ProductType();
            setType(productType, getInstanceByFeedProductType());
            videoGames.setProductType(productType);
            productData.setSoftwareVideoGames(videoGames);
        }

        void setSports() {
            Sports sports = new Sports();
            sports.setProductType(this.feedProductType);
            productData.setSports(sports);
        }

        void setLighting() {
            Lighting lighting = new Lighting();
            Lighting.ProductType productType = new Lighting.ProductType();
            setType(productType, getInstanceByFeedProductType());
            lighting.setProductType(productType);
            productData.setLighting(lighting);
        }
    }
}