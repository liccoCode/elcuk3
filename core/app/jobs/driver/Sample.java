package jobs.driver;

import play.Logger;

/**
 * 例子
 * User: wyatt
 * Date: 12/13/13
 * Time: 10:43 PM
 */
public class Sample extends BaseJob {
    @Override
    public void doit() {
        Logger.info("Context Map: %s", getContext());
        Logger.info("Execute and delete!");
    }
}
