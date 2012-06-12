package helper;

import com.google.gson.Gson;
import models.procure.PItem;
import org.apache.commons.io.FileUtils;
import play.Logger;

import java.io.File;

/**
 * Procure Helper. 暂时的
 * User: wyattpan
 * Date: 3/11/12
 * Time: 8:32 PM
 */
public class PH {
    public static PItem unMarsh(String id) {
        try {
            String pitemStr = FileUtils.readFileToString(new File(String.format("%s/pitems/%s.json", Constant.HOME, id)));
            return new Gson().fromJson(pitemStr, PItem.class);
        } catch(Exception e) {
            Logger.warn(String.format("%s : %s", e.getClass().getSimpleName(), e.getMessage()));
        }
        return null;
    }

    public static void marsh(PItem pitem) {
        String sid = pitem.selling.sellingId;
        String pid = pitem.product.sku;
        marsh(pitem, String.format("%s_%s", pid, sid));
    }

    public static void marsh(PItem pitem, String id) {
        try {
            PItem tmp = new PItem();
            tmp.in = pitem.in;
            tmp.onWay = pitem.onWay;
            tmp.onWork = pitem.onWork;

            tmp.seaBuy = pitem.seaBuy;
            tmp.seaPatch = pitem.seaPatch;
            tmp.airBuy = pitem.airBuy;
            tmp.airPatch = pitem.airPatch;

            FileUtils.writeStringToFile(
                    new File(String.format("%s/pitems/%s.json", Constant.HOME, id)),
                    new Gson().toJson(tmp));
        } catch(Exception e) {
            Logger.warn(String.format("%s : %s", e.getClass().getSimpleName(), e.getMessage()));
        }
    }
}
