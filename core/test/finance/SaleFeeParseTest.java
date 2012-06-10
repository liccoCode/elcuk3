package finance;

import jobs.FinanceCheckJob;
import jobs.KeepSessionJob;
import models.finance.SaleFee;
import models.market.Account;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 1:51 PM
 */
public class SaleFeeParseTest extends UnitTest {
    //    @Test
    public void testParse() {
        Account acc = Account.findById(1l);
        List<SaleFee> fees = SaleFee.flagFinanceParse(new File("/Users/wyattpan/elcuk2-data/finance/amazon.co.uk/2012.05/easyacc.eu@gmail.com_2012.05.03_21h.txt"), acc, Account.M.AMAZON_DE);
        for(SaleFee f : fees) {
            System.out.println(String.format("OrderId: %s, Cost: %s %s, USD_Cost: %s USD", f.orderId, f.cost, f.currency.name(), f.usdCost));
        }
    }

    //    @Test
    public void testParseThrough() {
        new KeepSessionJob().doJob();
        new FinanceCheckJob().doJob();
    }

    //    @Test
    public void testParseFlatV2() {
        ///Volumes/wyatt/Downloads/9299174904.txt
        Account acc = Account.findById(1l);
        List<File> files = new ArrayList<File>(FileUtils.listFiles(new File("/Users/wyattpan/elcuk2-data/finance/auto"), new String[]{"txt"}, false));

        for(File f1 : files) {
            String name = f1.getName();
            if(StringUtils.isBlank(name)) continue;
            if(NumberUtils.toInt(name.split("\\.")[0].split("f")[1]) < 5) continue;

            System.out.println("----------------" + f1.getAbsolutePath() + "/" + name + "-------------------");
            List<SaleFee> fees = SaleFee.flat2FinanceParse(f1, acc, Account.M.AMAZON_UK);
            for(SaleFee f : fees) {
                f.save();
            }
        }
    }

    @Before
    public void login() {
        Account acc = Account.findById(1l);
        acc.loginWebSite();
    }

    @Test
    public void testAccountBriefFlatFinance() {
        Account acc = Account.findById(1l);
        acc.briefFlatFinance(Account.M.AMAZON_UK);
    }
}
