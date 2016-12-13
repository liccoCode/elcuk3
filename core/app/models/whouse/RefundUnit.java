package models.whouse;

import com.alibaba.fastjson.JSON;
import helper.J;
import models.procure.ProcureUnit;
import models.qc.CheckTaskDTO;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Transient
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();


    public static Map<Integer, List<RefundUnit>> pageNumForTen(List<Refund> list) {
        Map<Integer, List<RefundUnit>> ten = new HashMap<>();
        int k = 0;
        for(Refund refund : list) {
            List<RefundUnit> ru = refund.unitList;
            int max = ru.size();
            for(int i = 0; i < ru.size(); i += 10) {
                int num = max - i;
                ten.put(k, ru.subList(i, num > 10 ? i + 10 : i + num));
                k++;
            }
        }
        return ten;
    }

    @PostLoad
    public void postPersist() {
        this.mainBox = JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }


    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

}
