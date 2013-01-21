package helper;

import models.market.M;
import org.junit.Test;
import play.test.UnitTest;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/18/13
 * Time: 11:52 AM
 */
public class DatesTest extends UnitTest {
    //PS: JVM Default TimeZone is Asia/Shanghai

    @Test
    public void UTCTimeZone() {
        String utc = "2013-01-14T07:56:16+00:00";
        // CST = UTC + 8:00
        assertEquals("2013-01-14 15:56:16", Dates.date2DateTime(Dates.parseXMLGregorianDate(utc)));
    }

    @Test
    public void GMLTTimeZone() throws DatatypeConfigurationException {
        String utc = "2013-01-14T07:56:16+08:00";
        // 解析的就是 +8 就应该是北京时间
        assertEquals("2013-01-14 07:56:16", Dates.date2DateTime(Dates.parseXMLGregorianDate(utc)));
        // Asia/Shanghai 和 GMT+8 不是一个实例, 但 Offset 是一样
        assertEquals(Dates.CN.toTimeZone().getRawOffset(),
                DatatypeFactory.newInstance().newXMLGregorianCalendar(utc).toGregorianCalendar()
                        .getTimeZone().getRawOffset());
    }

    @Test
    public void usTimeZone() {
        // 2012-11-30 00:00:00
        String us = "November 30, 2012";
        // +16
        assertEquals("2012-11-30 16:00:00",
                Dates.date2DateTime(Dates.transactionDate(M.AMAZON_US, us)));

    }

    /*
        US All Orders
       <PurchaseDate>2013-01-18T05:29:01+00:00</PurchaseDate>
       <LastUpdatedDate>2013-01-18T05:35:24+00:00</LastUpdatedDate>
       <OrderStatus>Pending</OrderStatus>
       <SalesChannel>Amazon.com</SalesChannel>
     */

    /*
        UK All Orders
      <MerchantOrderID>026-5432251-3657111</MerchantOrderID>
      <PurchaseDate>2013-01-18T05:06:37+00:00</PurchaseDate>
      <LastUpdatedDate>2013-01-18T05:06:43+00:00</LastUpdatedDate>
      <OrderStatus>Pending</OrderStatus>
      <SalesChannel>Amazon.co.uk</SalesChannel>
     */
    /*
        DE All Orders
      <MerchantOrderID>302-1065815-5932311</MerchantOrderID>
      <PurchaseDate>2013-01-18T05:08:38+00:00</PurchaseDate>
      <LastUpdatedDate>2013-01-18T05:10:56+00:00</LastUpdatedDate>
      <OrderStatus>Pending</OrderStatus>
      <SalesChannel>Amazon.de</SalesChannel>
     */

    /*
    110-2162328-5179422		DmlbKXMtN	Dprp1L4JR	07664457044546		2013-01-15T12:24:57+00:00	2013-01-17T05:51:50+00:00	2013-01-17T05:06:24+00:00	2013-01-17T16:10:00+00:00	blnpdwtsw5npzjr@marketplace.amazon.com	Diana Gil		80DBK12000-AB	EasyAcc 12000mAh 4 x USB Portable External Battery Pack Charger Power Bank for Tablets: iPad 3, iPad mini; Kindle Fire HD, Google Nexus 7, Nexus 10; S	1	USD	40.99	0.00	3.85	0.00	0.00	0.00	Standard	Diana Gil	163 Lloyd Street	2ND Floor		New Haven	CT	06513	US									0.00	-3.85	SMARTPOST	9102901001301798807420	2013-01-24T04:00:00+00:00	LEX1	AFN	Amazon.com
     */
}
