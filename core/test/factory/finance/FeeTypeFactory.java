package factory.finance;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.finance.FeeType;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 11:25 AM
 */
public class FeeTypeFactory extends ModelFactory<FeeType> {
    @Override
    public FeeType define() {
        FeeType type = new FeeType();
        type.name = "amazon";
        return type;
    }

    @Factory(name = "commission")
    public FeeType commission() {
        FeeType type = new FeeType();
        type.name = "commission";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "crossborderfulfilmentfee")
    public FeeType crossborderfulfilmentfee() {
        FeeType type = new FeeType();
        type.name = "crossborderfulfilmentfee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "disposalcomplete")
    public FeeType disposalcomplete() {
        FeeType type = new FeeType();
        type.name = "disposalcomplete";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbaperorderfulfillmentfee")
    public FeeType fbaperorderfulfillmentfee() {
        FeeType type = new FeeType();
        type.name = "fbaperorderfulfillmentfee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbaperunitfulfillmentfee")
    public FeeType fbaperunitfulfillmentfee() {
        FeeType type = new FeeType();
        type.name = "fbaperunitfulfillmentfee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbapickpackfeeperunit")
    public FeeType fbapickpackfeeperunit() {
        FeeType type = new FeeType();
        type.name = "fbapickpackfeeperunit";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbastoragefee")
    public FeeType fbastoragefee() {
        FeeType type = new FeeType();
        type.name = "fbastoragefee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbaweightbasedfee")
    public FeeType fbaweightbasedfee() {
        FeeType type = new FeeType();
        type.name = "fbaweightbasedfee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "fbaweighthandlingfee")
    public FeeType fbaweighthandlingfee() {
        FeeType type = new FeeType();
        type.name = "fbaweighthandlingfee";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "giftwrap")
    public FeeType giftwrap() {
        FeeType type = new FeeType();
        type.name = "giftwrap";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "giftwrapchargeback")
    public FeeType giftwrapchargeback() {
        FeeType type = new FeeType();
        type.name = "giftwrapchargeback";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "productcharges")
    public FeeType productcharges() {
        FeeType type = new FeeType();
        type.name = "productcharges";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "shippingchargeback")
    public FeeType shippingchargeback() {
        FeeType type = new FeeType();
        type.name = "shippingchargeback";
        type.parent = FeeType.amazon();
        return type;
    }

    @Factory(name = "shipping")
    public FeeType shipping() {
        FeeType type = new FeeType();
        type.name = "shipping";
        type.parent = FeeType.amazon();
        return type;
    }


    /**
     * FeeType 的初始化
     */
    public static void feeTypeInit() {
        FactoryBoy.create(FeeType.class);
        for(String fee : Arrays.asList("commission", "crossborderfulfilmentfee", "disposalcomplete",
                "productcharges", "fbaperorderfulfillmentfee", "fbaperunitfulfillmentfee",
                "fbapickpackfeeperunit",
                "fbastoragefee", "fbaweightbasedfee", "fbaweighthandlingfee", "giftwrap", "giftwrapchargeback",
                "shipping", "shippingchargeback")) {
            FactoryBoy.create(FeeType.class, fee);
        }
    }
}
