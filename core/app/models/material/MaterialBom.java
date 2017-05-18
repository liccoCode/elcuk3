package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/17
 * Time: 下午2:27
 */
@Entity
@DynamicUpdate
public class MaterialBom extends Model {

    public String number;

    public String name;

    public enum S {

        InProofing {
            @Override
            public String label() {
                return "打样中";
            }
        },
        OldPacking {
            @Override
            public String label() {
                return "旧包装";
            }
        },
        InDesign {
            @Override
            public String label() {
                return "设计中";
            }
        },
        InProduction {
            @Override
            public String label() {
                return "生产中";
            }
        },
        Stop {
            @Override
            public String label() {
                return "暂停";
            }
        };

        public abstract String label();
    }

    @ManyToMany(cascade = CascadeType.PERSIST)
    public List<Material> materials = new ArrayList<>();

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S status;

    public User creator;

    public Date createDate;

    public Date updateDate;


}
