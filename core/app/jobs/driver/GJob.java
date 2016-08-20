package jobs.driver;

import helper.J;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/13/13
 * Time: 10:54 PM
 */
@Entity
@DynamicUpdate
public class GJob extends Model {

    public enum S {
        /**
         * 在 DB 保存着, 可以加载出来
         */
        DB,
        /**
         * 在 内存中计算着, 无需加载出来
         */
        MEM,
        /**
         * 任务完成, 可以删除.
         */
        END
    }

    @Required
    public String className;

    public String jsonArgs = "{}";
    @Transient
    public Map<String, Object> args;

    public Date scheduleAt = new Date();

    @Enumerated(EnumType.STRING)
    public S state = S.DB;

    public String msg = "";

    public static GJob perform(String className, Map<String, Object> args, Date scheduleAt) {
        GJob job = new GJob();
        job.className = className;
        job.jsonArgs = J.json(args);
        job.state = S.DB;
        job.scheduleAt = scheduleAt;
        return job.save();
    }

    public static GJob perform(String className, Map<String, Object> args) {
        return perform(className, args, new Date());
    }

    public static GJob perform(Class clazz, Map<String, Object> args) {
        return perform(clazz.getName(), args, new Date());
    }
}
