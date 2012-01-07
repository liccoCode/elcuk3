package models.market;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 不同的账户, Market Place 可以相同, 但是 Account 不一定相同.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 4:39 PM
 */
@Entity
public class Account extends Model {
    /**
     * 不同的 Market place
     */
    public enum M {
        AMAZON_UK,
        AMAZON_DE,
        AMAZON_IT,
        AMAZON_FR,
        AMAZON_ES,
        AMAZON_US,
        EBAY_UK;

        public String toString() {
            switch(this) {
                case AMAZON_UK:
                    return "amazon.co.uk";
                case AMAZON_DE:
                    return "amazon.de";
                case AMAZON_IT:
                    return "amazon.it";
                case AMAZON_FR:
                    return "amazon.fr";
                case AMAZON_ES:
                    return "amazon.es";
                case AMAZON_US:
                    return "amazon.com";
                case EBAY_UK:
                    return "ebay.co.uk";
                default:
                    return "amazon.co.uk";
            }
        }

        public static M val(String str) {
            String strLowercase = str.toLowerCase();
            if(strLowercase.equals("amazon_uk") || strLowercase.equals("amazon.co.uk")) {
                return AMAZON_UK;
            } else if(strLowercase.equals("amazon_fr") || strLowercase.equals("amazon.fr")) {
                return AMAZON_FR;
            } else if(strLowercase.equals("amazon_es") || strLowercase.equals("amazon.es")) {
                return AMAZON_ES;
            } else if(strLowercase.equals("amazon_de") || strLowercase.equals("amazon.de")) {
                return AMAZON_DE;
            } else if(strLowercase.equals("amazon_it") || strLowercase.equals("amazon.it")) {
                return AMAZON_IT;
            } else if(strLowercase.equals("amazon_us") || strLowercase.equals("amazon.com")) {
                return AMAZON_US;
            } else if(strLowercase.equals("ebay_uk") || strLowercase.equals("ebay.co.uk")) {
                return EBAY_UK;
            } else {
                return null;
            }
        }
    }

    /**
     * 用于限制唯一性的字段; [type]_[username]
     */
    @Column(unique = true, nullable = false)
    public String uniqueName;

    /**
     * 哪一个市场
     */
    @Column(nullable = false)
    public M type;

    /**
     * 市场登陆用户名
     */
    @Column(nullable = false)
    public String username;

    /**
     * 市场登陆的密码
     */
    @Column(nullable = false)
    public String password;

    /**
     * 不同市场所拥有的钥匙
     */
    public String token;

    /**
     * 是否可用
     */
    public boolean closeable;
}
