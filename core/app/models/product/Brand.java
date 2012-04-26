package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * 产品分层中第二级别的 Brand 品牌
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
@Entity
public class Brand extends GenericModel {

    /**
     * 品牌名称
     */
    @Id
    public String name;

    /**
     * Brand 可以附属与很多类别
     */
    @ManyToMany(mappedBy = "brands")
    public List<Category> categories;
}
