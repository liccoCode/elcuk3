package models.market;

import com.google.gson.annotations.Expose;
import helper.Constant;
import helper.Dates;
import helper.OrderInvoiceFormat;
import helper.PDFs;
import models.ElcukRecord;
import models.finance.SaleFee;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.Logger;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.libs.Files;
import play.modules.pdf.PDF;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * 订单发票
 * User: cary
 * Date: 21/9/14
 * Time: 10:18 AM
 */
@Entity
@DynamicUpdate
public class OrderInvoice extends GenericModel {
    /**
     * 订单的编码
     */
    @Id
    @Expose
    public String orderid;

    /**
     * 发票更新日期
     */
    public Date updateDate;

    /**
     * 发票更新人
     */
    public String updator;


    /**
     * 发票地址
     */
    public String invoiceto;

    /**
     * 用户地址
     */
    public String address;

    /**
     * 未含税金额
     */
    public float notaxamount;

    /**
     * 含税金额
     */
    public float taxamount;

    /**
     * 总金额
     */
    public float totalamount;


    /**
     * 价格
     */
    @Transient
    public List<Float> price = new ArrayList<>();

    /**
     * 修改的价格
     */
    public String editprice;


    /**
     * 是否欧盟税号
     */
    public VAT europevat;

    @Transient
    public int isreturn;


    public enum VAT {
        /**
         * 普通发票
         */
        NORMAL {
            @Override
            public String label() {
                return "普通税号";
            }
        },
        /**
         * 欧盟发票
         */
        EUROPE {
            @Override
            public String label() {
                return "欧盟税号";
            }
        };

        public abstract String label();
    }


    @Transient
    public static float devat = 1.19f;

    @Transient
    public static float buyervat = 1f;

    @Transient
    public static float ukvat = 1.20f;

    @Transient
    public static float frvat = 1.20f;

    @Transient
    public static float itvat = 1.22f;

    @Transient
    public static float esvat = 1.21f;


    /**
     * 价格信息转化给前台显示
     */
    public void setprice() {
        this.price = new ArrayList<>();
        if(editprice != null && StringUtils.isNotBlank(editprice)) {
            String[] prices = editprice.split(",");
            for(String p : prices) {
                this.price.add(new Float(p));
            }
        }
    }


    /**
     * 保存价格信息
     */
    public void saveprice() {
        this.editprice = "";
        this.editprice = StringUtils.join(this.price.toArray(), ",");
    }


    /**
     * 判断invoice是否有效
     */
    public boolean checkInvoice(Orderr ord) {
        int pricecount = ord.items.size();
        for(int i = 0; i < ord.fees.size(); i++) {
            SaleFee fee = ord.fees.get(i);
            if(Arrays.asList("shipping", "shippingcharge", "giftwrap").contains(fee.type.name)) {
                pricecount++;
            }
        }
        if(pricecount > this.price.size()) {
            this.delete();
            return false;
        }
        return true;
    }


    /**
     * 格式化发票的信息
     *
     * @param m
     * @return
     */
    public static OrderInvoiceFormat invoiceformat(M m) {
        if(m == M.AMAZON_DE) {
            return OrderInvoiceFormat.newDe();
        } else if(m == M.AMAZON_UK) {
            return OrderInvoiceFormat.newUk();
        } else if(m == M.AMAZON_IT) {
            return OrderInvoiceFormat.newIt();
        } else if(m == M.AMAZON_FR) {
            return OrderInvoiceFormat.newFr();
        } else if(m == M.AMAZON_ES) {
            return OrderInvoiceFormat.newEs();
        }
        return null;
    }

    /**
     * num 每个市场需要生成多少张发票
     * date 时间
     * market 对应市场,可为空
     * 查询结果为当前date的 月初到月末
     *
     * @param num
     * @param date
     */
    public static void createInvoicePdf(int num, Date date, String market, String regex) {
        Date beginDate = Dates.monthBegin(date);
        Date endDate = Dates.monthEnd(date);
        DateTime dateTime = new DateTime(date);

        /*每月未发送过的单独打包*/
        String path = Constant.INVOICE_PATH + "/" + dateTime.getMonthOfYear() + "unsent";
        File folder = new File(path);
        if(!folder.exists()) folder.mkdir();

        List<M> markets = new ArrayList<>();
        if(StringUtils.isEmpty(market)) {
            markets.add(M.AMAZON_UK);
            markets.add(M.AMAZON_IT);
            markets.add(M.AMAZON_ES);
            markets.add(M.AMAZON_FR);
            markets.add(M.AMAZON_DE);
        } else {
            markets.add(M.val(market));
        }

        num = (num == 0) ? 10000 : num;
        for(M m : markets) {
            List<Orderr> list = Orderr.find("createDate >= ? and createDate <= ? and invoiceState='no' and market = ? "
                    + "and state <> ? and orderId LIKE '" + regex + "%' ", beginDate, endDate, m, Orderr.S.CANCEL)
                    .fetch(1, num);
            if(list != null && list.size() > 0) {
                for(Orderr ord : list) {
                    try {
                        String orderId = ord.orderId;
                        Logger.info(orderId);
                        OrderInvoiceFormat invoiceformat = OrderInvoice.invoiceformat(ord.market);

                        OrderInvoice invoice = OrderInvoice.findById(orderId);
                        if(invoice == null) {
                            invoice = ord.createOrderInvoice();
                        }
                        invoice.setprice();

                        final PDF.Options options = new PDF.Options();
                        options.pageSize = IHtmlToPdfTransformer.A3P;

                        F.T3<Float, Float, Float> amt = ord.amount();
                        Float totalamount = amt._1;
                        Float notaxamount = 0f;
                        if(invoice.europevat == OrderInvoice.VAT.EUROPE) {
                            notaxamount = -1 * totalamount;
                        } else
                            notaxamount = invoice.notaxamount;
                        Float tax = new BigDecimal(-1 * totalamount).subtract(new BigDecimal(notaxamount)).setScale(2, 4)
                                .floatValue();
                        Date returndate = ord.returndate();


                        String pdfName = invoiceformat.filename + orderId + ".pdf";
                        String template = "Orders/generate_invoice_pdf.html";
                        Map<String, Object> args = new HashMap<>();
                        args.put("invoiceformat", invoiceformat);
                        args.put("ord", ord);
                        args.put("invoice", invoice);
                        args.put("totalamount", totalamount);
                        args.put("notaxamount", notaxamount);
                        args.put("tax", tax);
                        args.put("returndate", returndate);
                        PDFs.templateAsPDFWithNoRequest(folder, pdfName, template, options, args);
                    } catch(Exception e) {
                        Logger.info(e.getMessage());
                        new ElcukRecord("自动生成发票", ord.orderId + " 生成时报错; " + e.getMessage(), "licco",
                                ord.orderId, new Date()).save();
                    }
                }
            }
        }
        File zip = new File(Constant.INVOICE_PATH + "/" + dateTime.getMonthOfYear() + "月unsent.zip");
        Files.zip(folder, zip);
    }

    public boolean isEurope() {
        return this.europevat == VAT.EUROPE;
    }
}
