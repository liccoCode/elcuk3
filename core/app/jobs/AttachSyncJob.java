package jobs;

import helper.QiniuUtils;
import helper.Webs;
import jobs.driver.BaseJob;
import models.product.Attach;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.jobs.Every;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/21
 * Time: 上午10:25
 */
public class AttachSyncJob extends BaseJob {

    public void doit() {
        List<Attach> attachs = Attach.find("sync=? ORDER BY createDate DESC ", 0).fetch(20);
        attachs.forEach(attach -> {
            Logger.info(String.format("Attach:[%s] 开始执行 七牛云迁移 方法", attach.location));
            try {

                File tempFile = new File(attach.location);
                String fileName = String.format("%s/%s", models.OperatorConfig.getVal("brandname"), tempFile.getName())
                        .toLowerCase();
                String url = QiniuUtils.upload(fileName, models.OperatorConfig.getVal("brandname"), attach.getBytes());
                if(StringUtils.isNotBlank(url)) {
                    attach.qiniuLocation = url;
                    attach.sync = 1;
                } else {
                    attach.sync = 2;
                }
                attach.save();
            } catch(Exception e) {
                attach.sync = 2;
                attach.save();
                Logger.error(Webs.e(e));
            }
        });
    }

}
