package jobs.driver;

import helper.DBUtils;
import helper.J;
import org.apache.commons.lang3.math.NumberUtils;
import play.Logger;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.jobs.Job;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 驱动系统内其他 Job 执行的 Drive Job
 * User: wyatt
 * Date: 12/13/13
 * Time: 11:05 PM
 */
//@Every("2s") // 因为负载不会很高, 时效要求也不会很严, 所以控制每 2 s 一次
public class DriverJob extends Job {

    @Override
    public void doJob() {
        try {
            SqlSelect sql = new SqlSelect()
                    .select("id", "className", "jsonArgs", "scheduleAt", "state")
                    .from("GJob")
                    .where("state=?").param(GJob.S.DB.name())
                    .where("scheduleAt<=?").param(new Date())
                    .limit(10);
            List<Map<String, Object>> rows = DBUtils
                    .rows(sql.toString(), sql.getParams().toArray());
            for(Map<String, Object> job : rows) {
                long jobId = NumberUtils.toLong(job.get("id").toString());
                try {
                    updateGjobStateToMEM(jobId);
                    Class clazz = Class.forName(job.get("className").toString());
                    BaseJob bjob = (BaseJob) clazz.newInstance();
                    Map context = J.from(job.get("jsonArgs").toString(), Map.class);
                    context.put("gjobId", jobId);
                    bjob.setContext(context);
                    bjob.now();
                } catch(Exception e) {
                    updateGjobStateToEND(jobId);
                    Logger.warn("Gjob %s with Exception %s, turn it off.", jobId, e.getMessage());
                }
            }
        } finally {
            // 这个频率参数可变, 可配置
            new DriverJob().in(2);
        }
    }

    public static void updateGjobStateToMEM(long gjobId) {
        updateGjobState(gjobId, GJob.S.MEM);
    }

    public static void updateGjobStateToEND(long gjobId) {
        updateGjobState(gjobId, GJob.S.END);
    }

    public static void updateGjobState(long gjobId, GJob.S state) {
        DB.execute("update GJob set state='" + state.name() + "' where id=" + gjobId);
    }
}
