package templates;

import com.google.common.collect.Lists;
import models.market.Selling;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-12-25
 * Time: 下午4:10
 */
public class TestFeedTrmplate extends UnitTest {

    @Test
    public void feedTrmplateTest() throws IOException {
        Selling s = Selling.findById("10HTCEVO3D-1900S|A_UK|1");
        s.aps.upc = "601359203373";
        s.aps.standerPrice = (float) 999.8;
        s.aps.feedProductType = "ComputerComponent";
        s.aps.templateType = "Computers";
        FileUtils.write(new File("/Users/mac/Desktop/Computers.txt"), Selling.generateFeedTemplateFile(Lists.newArrayList(s), s.aps.templateType, s.market.toString()));
    }
}
