package models.product;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Category 类别
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:44
 */
@Entity
public class Category extends Model {

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    public List<Product> products;

    @Column(nullable = false, unique = true)
    public String categoryId;

    @Column(nullable = false, unique = true)
    public String name;

    @Lob
    public String memo;

}
