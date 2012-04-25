package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Id;
import java.util.List;

/**
 * 产品分层中第二级别的 Brand 品牌
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
public class Brand extends GenericModel {

    /**
     * 品牌名称
     */
    @Id
    public String name;

    public List<Family> families;
}
