package models.whouse;

import models.procure.ProcureUnit;
import play.db.jpa.Model;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * 入库单元
 * Created by licco on 2016/11/2.
 */
public class InboundUnit extends Model {

    public Inbound inbound;


    public ProcureUnit unit;

    /**
     * 实际交货数量
     */
    public int qty;

    /**
     * 交货不足处理方式
     */
    @Enumerated(EnumType.STRING)
    public H handType;

    public enum H {
        A {
            @Override
            public String label() {
                return "按实际到货处理";
            }
        },

        B {
            @Override
            public String label() {
                return "收货且创建尾货单";
            }
        };

        public abstract String label();
    }




}
