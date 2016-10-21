package models.view.post;

import controllers.Login;
import helper.Dates;
import models.Notification;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;


/**
 * 通知 搜索功能
 * <p/>
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 10/15/12
 * Time: 5:56 PM
 */
public class NotificationPost extends Post<Notification> {

    public Notification.S state;

    public enum DateType {
        /**
         * 创建时间
         */
        CREATE {
            @Override
            public String label() {
                return "创建时间";
            }
        },
        /**
         * 通知时间
         */
        NOTIFICATION {
            @Override
            public String label() {
                return "通知时间";
            }
        };

        public abstract String label();
    }


    public NotificationPost() {
        this.state = Notification.S.UNCHECKED;
        this.perSize = 25;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return Notification.count(params._1, params._2.toArray());
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder jpql = new StringBuilder("user=?");
        List<Object> params = new ArrayList<>();
        params.add(Login.current());

        jpql.append("AND createAt>=? AND createAt<=?");
        params.add(Dates.morning(this.from));
        params.add(Dates.night(this.to));

        if(this.state != null) {
            jpql.append("AND state=?");
            params.add(this.state);
        }

        jpql.append(" ORDER BY createAt DESC");
        return new F.T2<>(jpql.toString(), params);
    }

    @Override
    public List<Notification> query() {
        F.T2<String, List<Object>> params = params();
        this.count = this.count(params);

        return Notification.find(params._1, params._2.toArray()).fetch(this.page, this.perSize);
    }

    @Override
    public Long getTotalCount() {
        F.T2<String, List<Object>> params = params();
        return Notification.count(params._1, params._2.toArray());
    }
}
