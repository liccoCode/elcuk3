package mws;

import helper.Webs;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 13-12-18
 * Time: 上午10:20
 */
public class SellingTest extends UnitTest {

    @Test
    public void generateFeedTemplateFileTest(){
        try {
            List<Selling> sellings = new ArrayList<Selling>();
            Selling s = Selling.findById("10HTCEVO3D-1900S|A_DE|1");
            s.aps.feedProductType = "ComputerDriveOrStorage";//ConsumerElectronics
            s.aps.standerPrice = (float)999.99;
            String [] keyFetures = StringUtils.splitByWholeSeparator(s.aps.keyFetures, Webs.SPLIT);
            for(String str : keyFetures) {
                s.aps.keyFeturess.add(str);
            }
            String [] rbns = StringUtils.splitByWholeSeparator(s.aps.RBN, ",");
            for(String str : rbns) {
                s.aps.rbns.add(str);
            }
            sellings.add(s);
            File file = new File("/Users/mac/computers.txt");
            BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "ISO8859-1"));
            String temp = Selling.generateFeedTemplateFile(sellings);
            bwriter.write(temp);
            bwriter.flush();
            bwriter.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
