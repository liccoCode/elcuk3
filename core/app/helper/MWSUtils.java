package helper;

import com.elcuk.jaxb.*;
import models.market.M;
import models.market.Selling;
import models.procure.FBAShipment;
import models.product.Attach;
import models.view.post.SellingAmzPost;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.utils.FastRuntimeException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.lang.Override;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;


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
        },
        FBA_INBOUND_CARTON_CONTENTS {
            @Override
            public String toString() {
                return "_POST_FBA_INBOUND_CARTON_CONTENTS_";
            }
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
        product.setDescriptionData(new DescriptionDataBuilder(selling, p).build());
        message.setProduct(product);
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

        product.setDescriptionData(new DescriptionDataBuilder(selling).build());

        if(!skipProductData(selling.aps.templateType, selling.aps.feedProductType)) {
            Product.ProductData productData = new ProductDataBuilder(selling.aps.templateType,
                    selling.aps.feedProductType).build();
            product.setProductData(productData);
        }
        message.setProduct(product);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    /**
     * 生成设置价格的 XML
     *
     * @param selling
     * @return
     */
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

    /**
     * 生成设置 Fulfillment By Amazon 的 XML
     *
     * @param selling
     * @return
     */
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

    /**
     * 生成提交包装信息给 FBA 的XML
     *
     * @param fbaShipment
     * @return
     */
    public static String fbaInboundCartonContentsXml(FBAShipment fbaShipment) {
        Selling selling = fbaShipment.selling();
        if(selling == null) return null;

        AmazonEnvelope envelope = new AmazonEnvelope();

        Header header = new Header();
        header.setDocumentVersion("1.01");
        header.setMerchantIdentifier(selling.account.merchantId);

        envelope.setHeader(header);
        envelope.setMessageType("CartonContentsRequest");

        AmazonEnvelope.Message message = new AmazonEnvelope.Message();
        message.setMessageID(BigInteger.valueOf(1));

        //如果填写了 lastCartonNum 则多加一个 Carton 尾箱
        int numCartons = fbaShipment.dto.boxNum;
        if(fbaShipment.dto.haveLastCartonNum()) {
            ++numCartons;
        }

        CartonContentsRequest request = new CartonContentsRequest();
        request.setShipmentId(fbaShipment.shipmentId);
        request.setNumCartons(BigInteger.valueOf(numCartons));//箱数

        for(int i = 0; i < numCartons; i++) {
            CartonContentsRequest.Carton.Item item = new CartonContentsRequest.Carton.Item();
            item.setSKU(selling.merchantSKU);
            if(i == numCartons - 1 && fbaShipment.dto.haveLastCartonNum()) {
                item.setQuantityShipped(BigInteger.valueOf(fbaShipment.dto.lastCartonNum));
                item.setQuantityInCase(BigInteger.valueOf(fbaShipment.dto.lastCartonNum));
            } else {
                item.setQuantityShipped(BigInteger.valueOf(fbaShipment.dto.num));
                item.setQuantityInCase(BigInteger.valueOf(fbaShipment.dto.num));
            }

            CartonContentsRequest.Carton carton = new CartonContentsRequest.Carton();
            //CartonId 有用处 参考: http://docs.developer.amazonservices.com/en_US/fba_guide/FBAGuide_SubmitCartonContentsFeed.html
            carton.setCartonId(String.format("%s%s", fbaShipment.shipmentId, i));
            carton.getItem().add(item);
            request.getCarton().add(carton);
        }

        message.setCartonContentsRequest(request);
        envelope.getMessage().add(message);
        return JaxbUtil.convertToXml(envelope);
    }

    private static boolean skipProductData(String templateType, String feedProductType) {
        return "Computers".equalsIgnoreCase(templateType) && "NotebookComputer".equalsIgnoreCase(feedProductType);
    }

    private static class DescriptionDataBuilder {
        private Product.DescriptionData descriptionData;
        M market;
        String title;
        String description;
        String partNumber;
        boolean giftWrap;
        String brand;
        String manufacturer;
        String rbn;
        List<String> bulletPoints;
        List<String> searchTerms;

        String unitOfVolume; // 长度单位
        Float length;
        Float width;
        Float heigh;
        Float weight;
        String unitOfWeight; // 重量单位
        models.product.Product p;

        DescriptionDataBuilder() {
            this.descriptionData = new Product.DescriptionData();
        }

        DescriptionDataBuilder(Selling selling) {
            this();
            this.p = models.product.Product.findByMerchantSKU(selling.merchantSKU);
            this.market = selling.market;
            this.title = selling.aps.title;
            this.description = selling.aps.title;
            this.partNumber = selling.aps.manufacturerPartNumber;
            this.giftWrap = selling.aps.isGiftWrap;
            this.brand = selling.aps.brand;
            this.manufacturer = selling.aps.manufacturer;
            this.rbn = selling.aps.rbns.get(0);
            this.bulletPoints = selling.aps.keyFeturess;
            this.searchTerms = selling.aps.searchTermss;

        }

        DescriptionDataBuilder(Selling selling, SellingAmzPost post) {
            this(selling);
            if(post.weight) {
                this.weight = post.packWeight;
                this.unitOfWeight = post.weightUnit;
            }
            if(post.productvolume) {
                this.unitOfVolume = post.volumeunit;
                this.length = post.productLengths;
                this.width = post.productWidth;
                this.heigh = post.productHeigh;
                this.weight = post.packWeight;
            }
            //删除掉未选中的属性
            if(!post.productdesc) this.description = null;
            if(!post.keyfeturess) this.bulletPoints = null;
            if(!post.searchtermss) this.searchTerms = null;
        }

        Product.DescriptionData build() {
            //Title
            this.descriptionData.setTitle(StringUtils.trim(this.title));
            //Description
            this.descriptionData.setDescription(StringUtils.trim(this.description));
            //Part Number
            this.descriptionData.setMfrPartNumber(this.partNumber);
            //GiftWrap
            this.descriptionData.setIsGiftMessageAvailable(this.giftWrap);
            //Brand
            this.descriptionData.setBrand(this.brand);
            //Manufacturer
            this.descriptionData.setManufacturer(this.manufacturer);
            //RBN
            if(this.market == M.AMAZON_US) {
                this.descriptionData.getUsedFor().add(this.rbn);
            } else {
                this.descriptionData.getRecommendedBrowseNode().add(BigInteger.valueOf(Long.valueOf(this.rbn)));
            }
            // BulletPoints
            if(this.bulletPoints != null && !this.bulletPoints.isEmpty()) {
                this.bulletPoints.stream()
                        .filter(StringUtils::isNotBlank)
                        .forEach(keyFeturess -> this.descriptionData.getBulletPoint().add(
                                StringUtils.trim(keyFeturess))
                        );
            }

            //SearchTerms
            if(this.searchTerms != null && !this.searchTerms.isEmpty()) {
                this.searchTerms.stream()
                        .filter(StringUtils::isNotBlank)
                        .forEach(searchTerm -> this.descriptionData.getSearchTerms().add(
                                StringUtils.trim(searchTerm))
                        );
            }
            //Dimensions
            setDimensions();
            setPackageDimensions();
            return this.descriptionData;
        }

        void setPackageDimensions() {
            Dimensions dimensions = new Dimensions();

            LengthUnitOfMeasure measure = LengthUnitOfMeasure.fromValue("MM");
            LengthDimension lengthDimension = new LengthDimension();
            /**长（包材）**/
            lengthDimension.setUnitOfMeasure(measure);
            lengthDimension.setValue(new BigDecimal(this.p.lengths).setScale(2, BigDecimal.ROUND_HALF_UP));
            dimensions.setLength(lengthDimension);
            /**宽（包材）**/
            LengthDimension widthDimension = new LengthDimension();
            widthDimension.setUnitOfMeasure(measure);
            widthDimension.setValue(new BigDecimal(this.p.width).setScale(2, BigDecimal.ROUND_HALF_UP));
            dimensions.setWidth(widthDimension);
            /**高（包材）**/
            LengthDimension heightDimension = new LengthDimension();
            heightDimension.setUnitOfMeasure(measure);
            heightDimension.setValue(new BigDecimal(this.p.heigh).setScale(2, BigDecimal.ROUND_HALF_UP));
            dimensions.setHeight(heightDimension);
            /**重量（包材）**/
            WeightUnitOfMeasure wm = WeightUnitOfMeasure.fromValue("KG");
            this.descriptionData.setPackageDimensions(dimensions);
            PositiveWeightDimension weightDimension = new PositiveWeightDimension();
            weightDimension.setUnitOfMeasure(wm);
            weightDimension.setValue(new BigDecimal(this.p.weight).setScale(2, BigDecimal.ROUND_HALF_UP));
            this.descriptionData.setPackageWeight(weightDimension);
        }

        void setDimensions() {
            Dimensions dimensions = new Dimensions();

            //Volume
            if(StringUtils.isNotBlank(this.unitOfVolume)) {
                LengthUnitOfMeasure measure = LengthUnitOfMeasure.fromValue(this.unitOfVolume);
                //Length
                if(this.p.productLengths != null) {
                    LengthDimension lengthDimension = new LengthDimension();
                    lengthDimension.setUnitOfMeasure(measure);
                    lengthDimension
                            .setValue(new BigDecimal(this.p.productLengths).setScale(2, BigDecimal.ROUND_HALF_UP));
                    dimensions.setLength(lengthDimension);
                }
                //width
                if(this.p.productWidth != null) {
                    LengthDimension widthDimension = new LengthDimension();
                    widthDimension.setUnitOfMeasure(measure);
                    widthDimension.setValue(new BigDecimal(this.p.productWidth).setScale(2, BigDecimal.ROUND_HALF_UP));
                    dimensions.setWidth(widthDimension);
                }
                //height
                if(this.p.productHeigh != null) {
                    LengthDimension heightDimension = new LengthDimension();
                    heightDimension.setUnitOfMeasure(measure);
                    heightDimension.setValue(new BigDecimal(this.p.productHeigh).setScale(2, BigDecimal.ROUND_HALF_UP));
                    dimensions.setHeight(heightDimension);
                }
            }
            //weight
            if(StringUtils.isNotBlank(this.unitOfWeight) && this.p.productWeight != null) {
                WeightUnitOfMeasure measure = WeightUnitOfMeasure.fromValue(this.unitOfWeight);
                WeightDimension weightDimension = new WeightDimension();
                weightDimension.setUnitOfMeasure(measure);
                weightDimension.setValue(new BigDecimal(this.p.productWeight).setScale(2, BigDecimal.ROUND_HALF_UP));
                dimensions.setWeight(weightDimension);
            }
            if(isShouldSetDimensions(dimensions)) this.descriptionData.setItemDimensions(dimensions);
        }

        boolean isShouldSetDimensions(Dimensions dimensions) {
            return dimensions.getLength() != null || dimensions.getWidth() != null
                    || dimensions.getWeight() != null || dimensions.getWeight() != null;
        }

        boolean isShouldBeSetVolumeDimensions() {
            return StringUtils.isNotBlank(this.unitOfVolume)
                    && (this.length != null || this.width != null || this.heigh != null);
        }
    }


    private static class ProductDataBuilder {
        String templateType;
        public String feedProductType;
        private Product.ProductData productData;

        ProductDataBuilder(String templateType, String feedProductType) {
            this.templateType = templateType;
            this.feedProductType = feedProductType;
            this.productData = new Product.ProductData();
        }

        Product.ProductData build() {
            try {
                switch(this.templateType) {
                    case "Computers":
                        setComputers();
                        break;
                    case "ConsumerElectronics":
                        setCE();
                        break;
                    case "Wireless":
                        setWireless();
                        break;
                    case "HomeImprovement":
                        setHomeImprovement();
                        break;
                    case "Home":
                        setHome();
                        break;
                    case "Games":
                        setGames();
                        break;
                    case "Sports":
                        setSports();
                        break;
                    case "Lighting":
                        setLighting();
                        break;
                    default:
                        setCE();
                        break;
                }
                return this.productData;
            } catch(FastRuntimeException e) {
                Logger.error(Webs.S(e));
                throw new FastRuntimeException(String.format(
                        "您所选择的 Feed Product Type 字段[%s]可能是不被支持的, 请更换该字段后再重试一次. ERROR:[%s]",
                        this.feedProductType, e.getMessage()));
            }
        }

        Object getInstanceByFeedProductType() {
            try {
                Class clazz = Class.forName(String.format("com.elcuk.jaxb.%s", this.feedProductType));
                return clazz.newInstance();
            } catch(IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                throw new FastRuntimeException(e);
            }
        }

        void setType(Object setter, Object param) {
            try {
                Method method = setter.getClass().getDeclaredMethod(String.format("set%s", this.feedProductType),
                        param.getClass());
                method.invoke(setter, param);
            } catch(NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new FastRuntimeException(e);
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
