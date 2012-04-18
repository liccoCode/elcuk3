package models;

/**
 * Market 枚举
 * User: wyattpan
 * Date: 4/18/12
 * Time: 1:46 PM
 */
public enum MT {
    AUK,
    ADE,
    AFR,
    AUS,
    AES,
    AIT,
    EUK;

    public static MT val(String market) {
        String s = market.toLowerCase();
        if("uk".equals(s) || s.equals("auk") || s.equals("amazon_uk") || s.equals("amazon.co.uk") || s.equals("www.amazon.co.uk")) {
            return AUK;
        } else if("fr".equals(s) || s.equals("afr") || s.equals("amazon_fr") || s.equals("amazon.fr") || s.equals("www.amazon.fr")) {
            return AFR;
        } else if("es".equals(s) || s.equals("aes") || s.equals("amazon_es") || s.equals("amazon.es") || s.equals("www.amazon.es")) {
            return AES;
        } else if("de".equals(s) || s.equals("ade") || s.equals("amazon_de") || s.equals("amazon.de") || s.equals("www.amazon.de")) {
            return ADE;
        } else if("it".equals(s) || s.equals("ait") || s.equals("amazon_it") || s.equals("amazon.it") || s.equals("www.amazon.it")) {
            return AIT;
        } else if("us".equals(s) || s.equals("aus") || s.equals("amazon_us") || s.equals("amazon.com") || s.equals("www.amazon.com")) {
            return AUS;
        } else if(s.equals("euk") || s.equals("ebay_uk") || s.equals("ebay.co.uk") || s.equals("www.ebay.co.uk")) {
            return EUK;
        } else {
            return null;
        }
    }
}
