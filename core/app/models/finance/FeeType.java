package models.finance;

import play.db.jpa.GenericModel;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * 所有费用的类型
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:21 AM
 */
@Entity
public class FeeType extends GenericModel {

    @Id
    public String name;

    @ManyToOne(cascade = CascadeType.ALL)
    public FeeType parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<FeeType> children;

    @Lob
    public String memo;

    @PrePersist
    public void prePersist() {
        this.name = this.name.toLowerCase();
    }
}
