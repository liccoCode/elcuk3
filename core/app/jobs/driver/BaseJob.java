package jobs.driver;

import helper.LogUtils;
import helper.Webs;
import play.Logger;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.jobs.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * 扩展 Job, 让 Job 能够拥有一个可由外界传递信息的 Context (Map)
 * User: wyatt
 * Date: 12/13/13
 * Time: 10:40 PM
 */
public abstract class BaseJob extends Job {
    private Map context = new HashMap();

    public Map getContext() {
        return context;
    }

    public void setContext(Map context) {
        this.context = context;
        Logger.debug("Driver Job(%s) Context: %s", this.hashCode(), getContext());
    }

    public void end(String msg) {
        Object jobId = getContext().get("gjobId");
        if(jobId != null) {
            DB.execute("UPDATE GJob set msg=" + SqlSelect.quote(msg == null ? "" : msg)
                    + ", state='" + GJob.S.END.name() + "' " + "where id=" + jobId.toString());
        }
    }

    public void del() {
        Object jobId = getContext().get("gjobId");
        if(jobId != null) {
            DB.execute("DELETE FROM GJob where id=" + jobId.toString());
        }
    }

    @Override
    public void doJob() {
        try {
            doit();
            del();
        } catch(Exception e) {
            LogUtils.JOBLOG.info(Webs.s(e));
            end(e.getMessage());
        }
    }


    public void doit() {
    }

}
