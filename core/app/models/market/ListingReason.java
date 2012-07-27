package models.market;

import com.google.gson.annotations.Expose;
import models.product.Category;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
    @Expose
    public Long id;

    @Column(length = 55)
    @Required
    @MaxSize(55)
    @MinSize(3)
    @Unique
    @Expose
    public String reason;

    @ManyToOne
    @Required
    public Category category;

    /**
     * 这个 Tag 所含有的 Review
     */
    @ManyToMany(mappedBy = "reasons")
    public List<AmazonListingReview> reviews = new ArrayList<AmazonListingReview>();

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

    public static ListingReason findByReason(String reason) {
        return ListingReason.find("reason=?", reason).first();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        ListingReason that = (ListingReason) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;
        if(reason != null ? !reason.equals(that.reason) : that.reason != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }
}
