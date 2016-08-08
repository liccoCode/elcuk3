package models.whouse;

import com.google.gson.annotations.Expose;
import helper.GTs;
import models.procure.ReceiveRecord;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.*;

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
        //已收货的出货单
        List<ReceiveRecord> records = ReceiveRecord.find("state=? and procureUnit.attrs.planShipDate is null and " +
                "procureUnit.product.sku = ? ", ReceiveRecord.S.Received, name).fetch();
        int no_country = 0;
        int total_num = 0;

        for(ReceiveRecord record : records) {
            if(record.qty > 0) {
                if(record.procureUnit.whouse == null) {
                    no_country += record.qty;
                } else {
                    // 仓库名都为FBA_DE,FBA_US
                    String country_name = record.procureUnit.whouse.name.split("_")[1];
                    if(map.containsKey(country_name)) {
                        map.put(country_name, map.get(country_name) + record.qty);
                    } else {
                        map.put(country_name, record.qty);
                    }
                }
                total_num += record.qty;
            }
        }

        List<WhouseItem> items = WhouseItem.find("stockObj.stockObjId=? and stockObj.stockObjType=? and " +
                "whouse.name like ? ", name, StockObj.SOT.valueOf(type), "半成品%").fetch();
        for(WhouseItem item : items) {
            if(item.qty > 0) {
                String country_name = item.whouse.country;
                if(country_name != null && country_name.equals("no_country")) {
                    no_country += item.qty;
                } else {
                    if(map.containsKey(country_name)) {
                        map.put(country_name, map.get(country_name) + item.qty);
                    } else {
                        map.put(country_name, item.qty);
                    }
                }
                total_num += item.qty;
            }
        }

        int td_num = map.keySet().size();
        if(no_country > 0) {
            td_num++;
            map.put("无条码无FBA", no_country);
        }
        map.put("total_num", total_num);
        map.put("td_num", td_num);
        return map;
    }

}
