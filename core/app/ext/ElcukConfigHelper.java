package ext;

import models.ElcukConfig;
import models.market.M;
import org.apache.commons.lang.math.NumberUtils;
import play.templates.JavaExtensions;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-22
 * Time: PM12:04
 */
public class ElcukConfigHelper extends JavaExtensions {
    public static long sumShipDay(String str) {
        String market = str.split("_")[0];
        String shipType = str.split("_")[1];
        long sum = 0;
        List<ElcukConfig> configs = ElcukConfig
                .find("name like ?", M.val(market).sortName() + "_" + shipType.toLowerCase() + "_%").fetch();

        for(ElcukConfig config : configs) {
            sum += NumberUtils.toLong(config.val);
        }
        return sum;
    }
}
