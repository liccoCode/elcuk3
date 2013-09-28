package norun;

import norun.statemechine.ShipMachine;
import org.junit.Test;
import org.squirrelframework.foundation.fsm.StateMachineWithoutContext;

/**
 * 状态机果然好省事. 这个例子是参考现在业务的中的 Shipment 的状态扭转. 定义好状态机中不同状态之间的转换规则与需要处理的事情,
 * 然后使用 event 去触发事件, 状态机根据自己的状态去进行状态变化.
 * User: wyatt
 * Date: 9/11/13
 * Time: 10:31 AM
 */
public class SquirrelDemo {
    @Test
    public void test() {
        // 起始默认状态
        StateMachineWithoutContext m = ShipMachine.newMachine();
        for(int i = 0; i < 5; i++) {
            m.fire("next");
        }
        m.terminate();

        System.out.println("======================================");
        // 起始指定状态
        m = ShipMachine.newMachine(ShipMachine.ShipState.DONE);
        for(int i = 0; i < 5; i++) {
            m.fire("next");
        }
        m.terminate();
    }
}
