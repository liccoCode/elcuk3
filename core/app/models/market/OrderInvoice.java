package models.market;

import com.google.gson.annotations.Expose;
import models.finance.SaleFee;
import play.db.jpa.GenericModel;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import helper.OrderInvoiceFormat;

/**
 * 订单发票
 * User: cary
 * Date: 21/9/14
 * Time: 10:18 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
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
    public List<Float> price;

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
    public static float esvat = 1.20f;


    /**
     * 价格信息转化给前台显示
     */
    public void setprice() {
        if(editprice != null) {
            String[] prices = editprice.split(",");
            this.price = new ArrayList<Float>();
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
        for(float p : this.price) {
            this.editprice = this.editprice + p + ",";
        }
    }


    /**
     * 判断invoice是否有效
     */
    public boolean checkInvoice(Orderr ord) {
        int pricecount = ord.items.size();
        for(int i = 0; i < ord.fees.size(); i++) {
            SaleFee fee = ord.fees.get(i);
            if(fee.type.name.equals("shipping") || fee.type.name.equals("giftwrap")) {
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
        OrderInvoiceFormat format = new OrderInvoiceFormat();

        if(m == M.AMAZON_DE) {
            format.title = "Rechnung";
            format.date = "Datum";
            format.frominfo1 = "TUGGLE ELECTRONIC COMMERCE CO.,LTD";
            format.frominfo2 = "Unit A5, 9/F Silvercorp Int'l";
            format.frominfo3 = "Tower 707-713 Nathan Rd";
            format.frominfo4 = "Mongkok, KL";
            format.frominfo5 = "999077 Hongkong";
            format.frominfo6 = "Steuernummer: 1667318915";
            format.frominfo7 = "USt-ID-Nr.: DE 292695920";
            format.address = "Lieferadresse";
            format.itemname = "Beschreibung";
            format.qty = "Menge";
            format.price = "Stückpreis(€)";
            format.itemamount = "Betrag(€)";
            format.notaxamount = "Zwischensumme";
            format.taxamount = "MwST";
            format.taxamountper = "MwST(19%)";
            format.totalamount = "Summe";
            format.country = "Deutschland";
            format.filename = "Rechnung de";
            format.returntitle = "Gutschrift";
            format.returntitle1 = "Originalrechnung";
            format.shipfee = "Versandkosten";
            format.giftwrapfee = "Geschenkpapier";
            format.rate = "Wechselkurs: 1 EUR = 0.8358 GBP";

            format.from = "From";
            format.to = "To";
        } else if(m == M.AMAZON_UK) {
            format.title = "Invoice";
            format.date = "Date";
            format.frominfo1 = "EDEER NETWORK TECHNOLOGY CO., LTD";
            format.frominfo2 = "Unit E6, 3 FLOOR WING TAT";
            format.frominfo3 = "COMMERCIAL BUILDING";
            format.frominfo4 = "97 BONHAM STRAND, SHEUNG WAN ";
            format.frominfo5 = "999077 HONGKONG";
            format.frominfo6 = "VAT No.: GB 117317336";
            format.frominfo7 = "";
            format.address = "Delivery Address";
            format.itemname = "Description";
            format.qty = "Quantity";
            format.price = "Unit Price(£)";
            format.itemamount = "Amount(£)";
            format.notaxamount = "Subtotal";
            format.taxamount = "VAT";
            format.taxamountper = "VAT(20%)";
            format.totalamount = "Total";
            format.country = "United Kingdom";
            format.filename = "Invoice uk";

            format.returntitle = "Credit Note";
            format.returntitle1 = "Original Invoice";


            format.shipfee = "Shipping cost";
            format.giftwrapfee = "giftwrap";

            format.from = "From";
            format.to = "To";

        } else if(m == M.AMAZON_IT) {
            format.title = "Fattura";
            format.date = "Data";
            format.frominfo1 = "TUGGLE ELECTRONIC COMMERCE CO.,LTD";
            format.frominfo2 = "Unit A5, 9/F Silvercorp Int'l";
            format.frominfo3 = "Tower 707-713 Nathan Rd";
            format.frominfo4 = "Mongkok, KL";
            format.frominfo5 = "999077 Hongkong";
            format.frominfo6 = "P.IVA: 08677060967";
            format.frominfo7 = "";
            format.address = "Indirizzo di spedizione";
            format.itemname = "Dettagli prodotto";
            format.qty = "Quantità";
            format.price = "prezzo unitario(€)";
            format.itemamount = "Ammontare(€)";
            format.notaxamount = "Sottotale";
            format.taxamount = "IVA";
            format.totalamount = "Totale";
            format.taxamountper = "IVA(22%)";
            format.rate = "Tasso di cambio: 1 EUR = 0,8358 GBP";
            format.country = "Italia";
            format.filename = "Fattura it";
            format.returntitle = "Nota di Credito";
            format.returntitle1 = "Fattura Originale";
            format.fromeurinfo1 = "EDEER NETWORK TECHNOLOGY CO., LTD";
            format.fromeurinfo2 = "Unit E6, 3 FLOOR WING TAT";
            format.fromeurinfo3 = "COMMERCIAL BUILDING";
            format.fromeurinfo4 = "97 BONHAM STRAND, SHEUNG WAN ";
            format.fromeurinfo5 = "999077 HONGKONG";
            format.fromeurinfo6 = "VAT No.: GB 117317336";
            format.fromeurinfo7 = "";

            format.shipfee = "Spese di Spedizione";
            format.giftwrapfee = "carta da regalo";

            format.from = "From";
            format.to = "To";

        } else if(m == M.AMAZON_FR) {
            format.title = "Facture";
            format.date = "Data";
            format.frominfo1 = "TUGGLE ELECTRONIC COMMERCE CO.,LTD";
            format.frominfo2 = "Unit A5, 9/F Silvercorp Int'l ";
            format.frominfo3 = "Tower 707-713 Nathan Rd";
            format.frominfo4 = "Mongkok, KL ";
            format.frominfo5 = "999077 HONGKONG";
            format.frominfo6 = "TVA： FR63328387212";
            format.frominfo7 = "";
            format.address = "Adresse d'expédition";
            format.itemname = "Détails du produit";
            format.qty = "Quantité";
            format.price = "Prix ​​unitaire(€)";
            format.itemamount = "Montant(€)";
            format.notaxamount = "Sous-total";
            format.taxamount = "TVA";
            format.taxamountper = "TVA(20%)";
            format.totalamount = "Total";
            format.rate = "Taux de change: 1 EUR = 0,8358 GBP";
            format.country = "France";
            format.filename = "France";
            format.returntitle = "Note de Crédit";
            format.returntitle1 = "Facture d'origine";
            format.shipfee = "Livraison";
            format.giftwrapfee = "emballage cadeau";

            format.from = "From";
            format.to = "To";

        } else if(m == M.AMAZON_ES) {
            format.title = "Factura";
            format.date = "Fecha";
            format.frominfo1 = "EDEER NETWORK TECHNOLOGY CO., LTD";
            format.frominfo2 = "Unit E6, 3 FLOOR WING TAT";
            format.frominfo3 = "COMMERCIAL BUILDING";
            format.frominfo4 = "97 BONHAM STRAND, SHEUNG WAN ";
            format.frominfo5 = "999077 HONGKONG";
            format.frominfo6 = "VAT No.: GB 117317336";
            format.frominfo7 = "";
            format.address = "Dirección de envío";
            format.itemname = "Detalles del producto";
            format.qty = "Cantidad";
            format.price = "Precio(€)";
            format.itemamount = "Importe(€)";
            format.notaxamount = "Subtotal";
            format.taxamount = "VAT";
            format.taxamountper = "VAT(20%)";
            format.totalamount = "Total";
            format.rate = "Tipo de cambio: 1 EUR = 0,8358 GBP";
            format.country = "España";
            format.filename = "España";
            format.returntitle = "Nota de Crédito";
            format.returntitle1 = "Factura original";
            format.shipfee = "Spese di Spedizione";
            format.giftwrapfee = "";

            format.from = "Desde";
            format.to = "Factura a";
        }
        return format;
    }

}