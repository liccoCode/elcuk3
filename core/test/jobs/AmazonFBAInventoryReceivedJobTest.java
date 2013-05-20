package jobs;

import models.market.Account;
import models.market.JobRequest;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.io.File;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/14/13
 * Time: 5:18 PM
 */
public class AmazonFBAInventoryReceivedJobTest extends UnitTest {

    @Test
    public void testFileToRows() {
        File file = Play.getFile("test/html/fba_inventory_file_test.txt");
        Map<String, AmazonFBAInventoryReceivedJob.Rows> rowsMap = new AmazonFBAInventoryReceivedJob()
                .fileToRows(file);
        AmazonFBAInventoryReceivedJob.Rows rows = rowsMap.get("FBA8F3Y63");
        assertEquals(true, rows.mskus.containsKey("71ACB1-BPU,699054211113"));
        assertEquals(1, rows.mskus.size());
        assertEquals(11, rows.records.size());
        assertEquals(632, rows.qty("71ACB1-BPU,699054211113"));


        rows = rowsMap.get("FBA8K4WY7");
        assertEquals(true, rows.mskus.containsKey("80-QW1A56-BE"));
        assertEquals(1, rows.mskus.size());
        assertEquals(25, rows.records.size());
        assertEquals(540, rows.qty("80-QW1A56-BE"));

        rows = rowsMap.get("FBA8F5XFT");
        assertEquals(2, rows.mskus.size());
        assertEquals(9, rows.records.size());
        assertEquals(true, rows.mskus.containsKey("70EAUB133-BN,654155541866"));
        assertEquals(202, rows.qty("70EAUB133-BN,654155541866"));
        assertEquals(true, rows.mskus.containsKey("73SMN7100-BHSPU,690494285382"));
        assertEquals(300, rows.qty("73SMN7100-BHSPU,690494285382"));

        assertEquals(3, rowsMap.size());
    }

    @Test
    public void testOneJob() {
        AmazonFBAInventoryReceivedJob worker = new AmazonFBAInventoryReceivedJob();
        Account acc = Account.findById(2l);
        JobRequest job = JobRequest.checkJob(acc, worker, acc.marketplaceId());
        if(job != null) job.request();
        JobRequest.updateState(worker.type());

        // 4. 获取 ReportId
        JobRequest.updateReportId(worker.type());

        // 5. 下载 report 文件
        JobRequest.downLoad(worker.type());
    }

    @Test
    public void testParseJob() {
        AmazonFBAInventoryReceivedJob worker = new AmazonFBAInventoryReceivedJob();
        worker.callBack(JobRequest.<JobRequest>findById(26240l));
    }
}
