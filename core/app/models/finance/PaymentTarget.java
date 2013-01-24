package models.finance;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/24/13
 * Time: 3:13 PM
 */
@Entity
public class PaymentTarget extends Model {
    @OneToMany(mappedBy = "target")
    public List<Payment> payments = new ArrayList<Payment>();

    @Required
    @Column(nullable = false)
    public String name;

    /**
     * 银行账号
     */
    @Required
    @Column(unique = true, nullable = false)
    public String accountNumber;

    public Date createdAt;

    @PrePersist
    public void beforeSave() {
        this.createdAt = new Date();
    }
}
