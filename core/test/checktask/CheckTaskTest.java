package checktask;


import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;
import models.qc.CheckTask;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/29/13
 * Time: 5:02 PM
 */
public class CheckTaskTest extends UnitTest {

    @Test
    public void testTask(){
        CheckTask.generateTask();
    }
}