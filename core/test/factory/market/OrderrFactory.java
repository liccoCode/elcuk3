package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.annotation.Factory;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import util.DateHelper;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/29/13
 * Time: 6:22 PM
 */
public class OrderrFactory extends ModelFactory<Orderr> {
    @Override
    public Orderr define() {
        Orderr orderr = new Orderr();
        orderr.orderId = "403-3852686-2505963";
        orderr.email = "81798950@qq.com";
        orderr.market = M.AMAZON_FR;
        orderr.paymentDate = new Date();
        orderr.address = "Kdfdsj";
        orderr.state = Orderr.S.SHIPPED;
        return orderr;
    }

    @Factory(name = "de")
    public Orderr de() {
        Orderr ord = base();
        ord.market = M.AMAZON_DE;
        ord.account = FactoryBoy.lastOrCreate(Account.class, "de");
        return ord;
    }

    public Orderr base() {
        //002-0001874-5693836	NULL		2013-08-21 11:00:00	JANICE A LAIRD	Dover	US	2013-08-17 06:42:22	8lzp4zbmkp3n8xf@marketplace.amazon.com	AMAZON_US	NULL	2013-08-17 06:42:22	717-292-6218	17315	PA	NULL	2013-08-18 04:36:13	SecondDay	NULL	SMARTPOST	SHIPPED	NULL	9261293150292952104697	131	A16K6LAS9K8BCU	NULL	1	0
        Orderr order = new Orderr();
        order.orderId = "302-9323131-4361931";
        order.address = "Am Moorgraben 31a";
        order.address1 = "Roswitha Neugebauer\n" +
                "Am Moorgraben 31a\n" +
                "Apen\n" +
                "Niedersachsen\n" +
                "26689\n" +
                "Germany";
        order.arriveDate = DateHelper.t("2011-10-13 19:00:00");
        order.buyer = "Roswitha Neugebauer";
        order.city = "Apen";
        order.country = "DE";
        order.createDate = DateHelper.t("2011-10-07 10:02:19");
        order.email = "xj8y6t06kvryrts@marketplace.amazon.de";
        order.market = M.AMAZON_DE;
        order.paymentDate = DateHelper.t("2011-10-07 10:18:00");
        order.phone = "0176-24054244";
        order.postalCode = "26689";
        order.province = "Niedersachsen";
        order.reciver = "Roswitha Neugebauer";
        order.shipDate = DateHelper.t("2011-10-10 18:25:18");
        order.shipLevel = "Standard";
        order.shippingAmount = 0f;
        order.shippingService = "DP";
        order.state = Orderr.S.SHIPPED;
        order.totalAmount = 46.371f;
        order.trackNo = "164200407975";
        order.account = FactoryBoy.lastOrCreate(Account.class);
        order.userid = "A3BU1NBH6D77ID";
        return order;
    }
}
