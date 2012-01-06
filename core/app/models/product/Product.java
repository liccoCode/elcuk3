package models.product;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
public class Product extends Model {
    /**
     * 唯一的标示
     */
    @Column(nullable = false, unique = true)
    public String sku;
}
