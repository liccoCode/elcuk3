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

    public String review(String asin, int page) {
        //http://www.amazon.it/product-reviews/{ASIN}?pageNumber={PAGE}&sortBy=bySubmissionDateDescending
        switch(this) {
            case AUK:
                return String.format("http://www.amazon.co.uk/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
            case AUS:
                return String.format("http://www.amazon.com/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
            case ADE:
            case AES:
            case AIT:
            case AFR:
                return String.format("http://www.amazon.%s/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", this.name().toLowerCase(), asin, page);
            default:
                return "";
        }
    }

    public String listing(String asin) {
        //http://www.amazon.co.uk/dp/B007TR9VRU
        switch(this) {
            case AUK:
                return String.format("http://www.amazon.co.uk/dp/%s", asin);
            case AUS:
                return String.format("http://www.amazon.com/dp/%s", asin);
            case ADE:
            case AES:
            case AIT:
            case AFR:
                return String.format("http://www.amazon.%s/dp/%s", this.name().toLowerCase(), asin);
            default:
                return "";
        }
    }

    public String offers(String asin) {
        //http://www.amazon.co.uk/gp/offer-listing/B007TR9VRU
        switch(this) {
            case AUK:
                return String.format("http://www.amazon.co.uk/gp/offer-listing/%s", asin);
            case AUS:
                return String.format("http://www.amazon.com/gp/offer-listing/%s", asin);
            case ADE:
            case AES:
            case AIT:
            case AFR:
                return String.format("http://www.amazon.%s/gp/offer-listing/%s", this.name().toLowerCase(), asin);
            default:
                return "";
        }

    }
}
