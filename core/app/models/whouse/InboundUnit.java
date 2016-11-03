package models.whouse;

import models.procure.ProcureUnit;
import play.db.jpa.Model;

/**
 * 入库单元
 * Created by licco on 2016/11/2.
 */
public class InboundUnit extends Model {

    public Inbound inbound;

    public ProcureUnit unit;



}
