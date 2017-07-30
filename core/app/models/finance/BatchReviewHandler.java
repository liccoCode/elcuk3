package models.finance;

import models.User;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/7/29
 * Time: 下午4:53
 */
@Entity
public class BatchReviewHandler extends Model {

    private static final long serialVersionUID = 397301259876867994L;

    @ManyToOne
    public BatchReviewApply apply;
    
    @OneToOne
    public User handler;

    public Date createDate;

    public enum R {
        Agree {
            @Override
            public String label() {
                return "同意";
            }
        },
        Disagree {
            @Override
            public String label() {
                return "不同意";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    public R result;

    public String memo;

}
