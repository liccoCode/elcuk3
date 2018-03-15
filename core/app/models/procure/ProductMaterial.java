package models.procure;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/10/18
 * Time: 下午5:49
 */
@Entity
public class ProductMaterial extends Model {

    private static final long serialVersionUID = -5562390764464115254L;

    @ManyToOne
    public CooperItem product;

    @ManyToOne
    public CooperItem material;

}
