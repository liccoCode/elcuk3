package models.whouse;

import com.google.gson.annotations.Expose;
import models.procure.ReceiveRecord;
import play.db.jpa.Model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 库存项
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 10:52 AM
 */
@Entity
public class WhouseItem extends Model {

    @ManyToOne
    public Whouse whouse;

    /**
     * 库存对象(到底存的是什么东西, SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    @Expose
    public Integer qty = 0;

    /**
     * 待处理库存数量
     */
    @Expose
    public Integer pendingQty = 0;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    public WhouseItem() {
    }

    public WhouseItem(StockObj stockObj, Whouse whouse) {
        this.stockObj = stockObj.dump();
        this.whouse = whouse;
    }

    public static WhouseItem findItem(StockObj stockObj, Whouse whouse) {
        WhouseItem whouseItem = WhouseItem.find("stockObjId=? AND stockObjType=? AND whouse_id=?",
                stockObj.stockObjId, stockObj.stockObjType.name(), whouse.id).first();
        if(whouseItem == null) {
            return new WhouseItem(stockObj, whouse).save();
        }
        return whouseItem;
    }

    public static HashMap<String, Integer> caluStockInProcureUnit(String name, String type) {
        HashMap<String, Integer> map = new HashMap<>();
        List<ReceiveRecord> records = ReceiveRecord.find("state=? and procureUnit.attrs.planShipDate is null",
                ReceiveRecord.S.Received).fetch();
        int no_country = 0;
        int total_num = 0;

        for(ReceiveRecord record : records) {
            if(record.procureUnit.whouse == null) {
                no_country += record.qty;
            } else {
                if(map.containsKey(record.procureUnit.whouse.name)) {
                    map.put(record.procureUnit.whouse.name, map.get(record.procureUnit.whouse.name) + record.qty);
                } else {
                    map.put(record.procureUnit.whouse.name, record.qty);
                }
            }
            total_num += record.qty;
        }


        List<WhouseItem> items = WhouseItem.find("stockObj.stockObjId=? and stockObj.stockObjType=?",
                name, StockObj.SOT.valueOf(type)).fetch();
        for(WhouseItem item : items) {
            item.stockObj.unmarshalAtts();

            if(item.stockObj.attrs.get("fba") == null) {


            }
        }
        int td_num = map.keySet().size();
        if(no_country > 0) {
            td_num++;
        }

        map.put("无条码无FBA", no_country);
        map.put("total_num", total_num);
        map.put("td_num", td_num);
        return map;
    }

}
