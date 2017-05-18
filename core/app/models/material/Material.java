package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import models.procure.Cooperator;
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
 * Date: 2017/5/16
 * Time: 下午3:30
 */
@Entity
@DynamicUpdate
public class Material extends Model {

    private static final long serialVersionUID = 4894533191306168541L;
    /**
     * 物料编码
     */
    public String code;

    /**
     * 物料名称
     */
    public String name;

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {

        Parts {
            @Override
            public String label() {
                return "配件";
            }
        },

        Package {
            @Override
            public String label() {
                return "包材";
            }
        },

        RawMaterial {
            @Override
            public String label() {
                return "原材料";
            }
        };

        public abstract String label();
    }

    /**
     * 规格
     */
    public String specification;

    /**
     * 材质
     */
    public String texture;

    /**
     * 工艺
     */
    public String technology;

    /**
     * 图号/版本
     */
    public String version;

    /**
     * 供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    @ManyToMany(mappedBy = "materials", cascade = CascadeType.PERSIST)
    public List<MaterialBom> boms = new ArrayList<>();

    @Transient
    public Long cooperatorId;

    /**
     * 创建人
     */
    @OneToOne
    public User creator;

    public Date createDate;

    /**
     * 最近更新时间
     */
    public Date updateDate;


}
