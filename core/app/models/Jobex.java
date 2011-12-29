package models;

import play.db.jpa.Model;
import play.jobs.Job;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 1:05 AM
 */
@Entity
public class Jobex extends Model {

    @Column(unique = true, nullable = false)
    public String className;

    /**
     * Play 使用的周期, 如果周期为 null 或者解析失败则转而使用 cron 表达式
     */
    public String duration;

    public long lastUpdateTime = 0;

    /**
     * 是否关闭了, 关闭了则不运行
     */
    public boolean close = false;

    /**
     * 实例化 Job
     */
    @SuppressWarnings("unchecked")
    public Job newInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<Job> jobClz = (Class<Job>) Class.forName(className);
        return jobClz.newInstance();
    }
}
