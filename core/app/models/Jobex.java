package models;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.Logger;
import play.Play;
import play.db.jpa.Model;
import play.jobs.Job;
import play.libs.Time;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 利用 Play Framework 的 Async Job, 使用一个 JobDriver 来驱动这些 Jobex 以便提供
 * 了能够暂时关闭这个 Jobex 的功能。
 * User: wyattpan
 * Date: 12/29/11
 * Time: 1:05 AM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)//添加缓存, 因为经常加载
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Jobex extends Model {

    @Column(unique = true, nullable = false)
    public String className;

    /**
     * Play 使用的周期, 如果周期为 null 或者解析失败则转而使用 cron 表达式
     */
    public String duration;

    public long lastUpdateTime = 0;

    /**
     * 是否关闭了, 关闭了则不运行;如果关闭了, 无论什么环境都无法运行
     */
    public boolean close = false;

    /**
     * 在测试环境下是否可以运行
     */
    public boolean devRun = false;

    @Column(columnDefinition = "varchar(255) DEFAULT ''")
    public String memo;

    /**
     * 实例化 Job
     */
    @SuppressWarnings("unchecked")
    public Job newInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<Job> jobClz = (Class<Job>) Class.forName(className);
        return jobClz.newInstance();
    }

    /**
     * 立即执行
     *
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Job now() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Job job = newInstance();
        job.now();
        this.lastUpdateTime = System.currentTimeMillis();
        this.save();
        return job;
    }

    public void updateJobAttrs(Jobex njob) {
        this.close = njob.close;
        if(njob.className != null && !njob.className.trim().isEmpty())
            this.className = njob.className;
        if(njob.duration != null && !njob.duration.trim().isEmpty()) {
            if(!Time.CronExpression.isValidExpression(njob.duration))
                Time.parseDuration(njob.duration);
            this.duration = njob.duration;
        }
        this.lastUpdateTime = System.currentTimeMillis();
        this.save();
    }

    /**
     * <pre>
     * 根据任务的各种条件来判断此任务是否可以执行;
     * 1. duration
     * 2. close?
     * 3. devRun?
     * </pre>
     *
     * @return
     */
    public boolean isExcute() {
        if(this.close) return false;
        try {
            /**
             * 1. 判断当前时间与 job 的上次执行时间之间的检查差据如果大于执行周期, 则执行, 否则不执行
             */
            long now = System.currentTimeMillis();
            long durationMills = 0;
            try {
                durationMills = Time.cronInterval(this.duration);
            } catch(Exception e) {
                durationMills = Time.parseDuration(this.duration) * 1000;
            }
            boolean isExecute = Math.abs(now - this.lastUpdateTime) > durationMills;
            if(Play.mode.isDev()) isExecute = isExecute && this.devRun;

            if(isExecute) {
                this.lastUpdateTime = now;
                this.save();
            }
            return isExecute;
        } catch(Exception e) {
            Logger.warn("Jobex %s[%s] run error!", this.className, this.id);
            return false;
        }
    }

    public static Jobex findByClassName(String className) {
        return Jobex.find("className=?", className).first();
    }
}
