package models.market;

import models.product.Category;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;

import javax.persistence.*;

/**
 * Listing 产生的问题的原因
 * <p/>
 * User: wyattpan
 * Date: 7/26/12
 * Time: 4:29 PM
 */
@Entity
public class ListingReason extends GenericModel {
    @Id
    @GeneratedValue
    public Long id;

    @Column(length = 55)
    @Required
    @MaxSize(55)
    @MinSize(3)
    @Unique
    public String reason;

    @ManyToOne
    @Required
    public Category category;

    @Lob
    @Required
    @MinSize(3)
    public String memo = " ";

    public ListingReason checkAndSave() {
        this.check();
        return this.save();
    }

    public ListingReason checkAndUpdate() {
        this.check();
        return this.save();
    }

    private void check() {
        this.reason = this.reason.trim();
        this.memo = this.memo.trim();
    }
}
