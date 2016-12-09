package models.whouse;

import models.procure.ProcureUnit;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Created by licco on 2016/11/25.
 */
@Entity
@DynamicUpdate
public class RefundUnit extends Model {

    @ManyToOne
    public Refund refund;

    @OneToOne
    public ProcureUnit unit;

    /**
     * 计划退货数量
     */
    public int planQty;

    /**
     * 实际退货数量
     */
    public int qty;


    /**
     * 主箱信息
     */
    @Lob
    public String mainBoxInfo;

    /**
     * 主箱信息
     */
    @Lob
    public String lastBoxInfo;




}
