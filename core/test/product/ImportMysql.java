package product;

import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/18/12
 * Time: 2:25 PM
 */
public class ImportMysql extends UnitTest {
    @Test
    public void importMysql() {
        try {
//            Logger.info("mysql -f -uroot -pcrater10lake elcuk2_t < /Users/wyattpan/backup-db/elcuk2_t.sql");
//            Process pro = Runtime.getRuntime().exec("mysql -f -uroot -pcrater10lake elcuk2_t < /Users/wyattpan/backup-db/elcuk2_t.sql");
            Process pro = Runtime.getRuntime().exec("sh " + System.getProperty("user.home") + "/backup-db/im.sh");
            String rtStr = IO.readContentAsString(pro.getInputStream());
            String errStr = IO.readContentAsString(pro.getErrorStream());
            System.out.println("------------------ Output ------------------");
            System.out.println(rtStr);
            System.out.println("------------------ Error ------------------");
            System.out.println(errStr);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
