package models.market;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 18/3/2
 * Time: 上午11:33
 */
@Entity
public class SellingRank extends Model {

    @ManyToOne(cascade = CascadeType.PERSIST)
    public Selling selling;

    private Integer rank;

    private String ladderName;

    private String ladderUrl;

    private Date createDate;

}
