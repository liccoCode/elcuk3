package models.view.dto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/23/13
 * Time: 9:50 AM
 */
public class DashBoard {

    public static class OrderInfo {
        public Date date;
        public Integer pedings = 0;
        public Integer payments = 0;
        public Integer shippeds = 0;
        public Integer refundeds = 0;
        public Integer returnNews = 0;
        public Integer cancels = 0;

        public Integer total() {
            return this.pedings + this.payments + this.shippeds
                    + this.refundeds + this.returnNews + this.cancels;
        }
    }
}
