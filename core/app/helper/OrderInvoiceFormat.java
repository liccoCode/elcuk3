package helper;

/**
 * 发票的各国语言
 * User: mac
 * Date: 14-9-24
 * Time: 下午3:07
 */
public class OrderInvoiceFormat {
    /**
     * 标题名称
     */
    public String title;
    public String returntitle;
    public String returntitle1;
    public String date;
    public String frominfo1;
    public String frominfo2;
    public String frominfo3;
    public String frominfo4;
    public String frominfo5;
    public String frominfo6;
    public String frominfo7;
    public String rate;
    public String address;
    public String itemname;
    public String qty;
    public String price;
    public String itemamount;
    public String notaxamount;
    public String taxamount;
    public String taxamountper;
    public String totalamount;
    public String country;
    public String filename;

    //欧盟税号发票信息
    public String fromeurinfo1;
    public String fromeurinfo2;
    public String fromeurinfo3;
    public String fromeurinfo4;
    public String fromeurinfo5;
    public String fromeurinfo6;
    public String fromeurinfo7;

    public String shipfee;
    public String giftwrapfee;

    public String from;
    public String to;

    public static OrderInvoiceFormat newDe() {
        OrderInvoiceFormat format = new OrderInvoiceFormat();
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
        return format;
    }

    public static OrderInvoiceFormat newUk() {
        OrderInvoiceFormat format = new OrderInvoiceFormat();
        format.title = "Invoice";
        format.date = "Date";
        format.frominfo1 = "EASYACC TECHNOLOGY CO., LIMITED";
        format.frominfo2 = "UNIT 2508 25/F BANK OF AMERICA";
        format.frominfo3 = "TOWER 12 HARCOURT RD CENTRAL";
        format.frominfo4 = "HONG KONG ";
        format.frominfo5 = "VAT No.: GB239299660";
        format.frominfo6 = "";
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
        return format;
    }

    public static OrderInvoiceFormat newIt() {
        OrderInvoiceFormat format = new OrderInvoiceFormat();
        format.title = "Fattura";
        format.date = "Data";
        format.frominfo1 = "EASYACC TECHNOLOGY CO., LIMITED";
        format.frominfo2 = "UNIT 2508 25/F BANK OF AMERICA";
        format.frominfo3 = "TOWER 12 HARCOURT RD CENTRAL";
        format.frominfo4 = "HONG KONG";
        format.frominfo5 = "P.IVA: IT09384010964";
        format.frominfo6 = "";
        format.frominfo7 = "";
        format.address = "Indirizzo di spedizione";
        format.itemname = "Dettagli prodotto";
        format.qty = "Quantità";
        format.price = "prezzo unitario(€)";
        format.itemamount = "Ammontare(€)";
        format.notaxamount = "Subtotale";
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
        return format;
    }

    public static OrderInvoiceFormat newFr() {
        OrderInvoiceFormat format = new OrderInvoiceFormat();
        format.title = "Facture";
        format.date = "Data";
        format.frominfo1 = "EASYACC TECHNOLOGY CO., LIMITED";
        format.frominfo2 = "UNIT 2508 25/F BANK OF AMERICA ";
        format.frominfo3 = "TOWER 12 HARCOURT RD CENTRAL";
        format.frominfo4 = "HONG KONG";
        format.frominfo5 = "TVA: FR84819696584";
        format.frominfo6 = "";
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
        return format;
    }

    public static OrderInvoiceFormat newEs() {
        OrderInvoiceFormat format = new OrderInvoiceFormat();
        format.title = "Factura";
        format.date = "Fecha";
        format.frominfo1 = "EASYACC TECHNOLOGY CO., LIMITED";
        format.frominfo2 = "UNIT 2508 25/F BANK OF AMERICA";
        format.frominfo3 = "TOWER 12 HARCOURT RD CENTRAL";
        format.frominfo4 = "HONG KONG";
        format.frominfo5 = "VAT No.: IT09384010964";
        format.frominfo6 = "";
        format.frominfo7 = "";
        format.address = "Dirección de envío";
        format.itemname = "Detalles del producto";
        format.qty = "Cantidad";
        format.price = "Precio(€)";
        format.itemamount = "Importe(€)";
        format.notaxamount = "Subtotal";
        format.taxamount = "VAT";
        format.taxamountper = "VAT(21%)";
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
        return format;
    }
}
