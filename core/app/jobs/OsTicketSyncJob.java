package jobs;

import com.google.gson.JsonElement;
import helper.HTTP;
import org.apache.http.message.BasicNameValuePair;
import play.jobs.Job;

import java.util.Arrays;

/**
 * 向 OsTicket 系统进行 Ticket 的数据同步
 * User: wyattpan
 * Date: 7/30/12
 * Time: 5:05 PM
 */
public class OsTicketSyncJob extends Job {

    @Override
    public void doJob() {
        JsonElement rs = HTTP.json("http://t.easyacceu.com/api/tickt_sync?", Arrays.asList(new BasicNameValuePair("", "")));
    }
}
