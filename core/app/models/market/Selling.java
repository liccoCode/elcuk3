package models.market;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
public class Selling extends Model {

    @ManyToOne
    public Listing listing;
}
