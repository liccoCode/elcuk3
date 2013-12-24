package mws.v2;

import com.amazonaws.mws.MarketplaceWebServiceException;
import factory.FactoryBoy;
import models.market.Account;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/19/13
 * Time: 4:11 PM
 */
public class MWSFeedsTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
    }

    @Ignore
    @Test
    public void testGetFeedResult() throws MarketplaceWebServiceException {
        Account account = FactoryBoy.build(Account.class, "de");
        MWSFeeds mwsFeedRequest = new MWSFeeds(account);
        File file = mwsFeedRequest.getFeedResult("7190370138");
        System.out.println(file.getAbsolutePath());
    }
}
