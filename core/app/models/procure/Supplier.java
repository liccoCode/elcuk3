package models.procure;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 购买商品的工厂
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:18 PM
 */
@Entity
public class Supplier extends GenericModel {

    @Id
    public String id;

    public String name;

    public String address;

    public String contacter;

}
