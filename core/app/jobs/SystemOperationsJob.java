package jobs;

import helper.HTTP;
import jobs.driver.BaseJob;
import models.Server;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录系统的操作日志
 * User: cary
 * Date: 14-10-17
 * Time: 上午9:25
 */
public class SystemOperationsJob extends BaseJob {
    private String actionMethod;
    private String url;
    private String controller;

    private String rollurl = "http://rollapi.easya.cc/messages";

    public SystemOperationsJob(String actionMethod, String url, String controller) {
        this.actionMethod = actionMethod;
        this.url = url;
        this.controller = controller;
    }

    @Override
    public void doit() {
        String level = "info";
        String request_time = helper.Dates.date2DateTime();
        String hostname = "elcuk2";
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("req_action", actionMethod));
        param.add(new BasicNameValuePair("req_hostname", hostname));
        param.add(new BasicNameValuePair("url", url));
        param.add(new BasicNameValuePair("level", level));
        param.add(new BasicNameValuePair("request_time", request_time));
        param.add(new BasicNameValuePair("req_controller", this.controller));
        String json = HTTP.post(this.rollurl, param);
    }
}
