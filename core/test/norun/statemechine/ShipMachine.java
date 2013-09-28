package norun.statemechine;

import org.squirrelframework.foundation.fsm.*;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachineWithoutContext;

import java.util.Map;

/**
 * 状态机实验
 * User: wyatt
 * Date: 9/11/13
 * Time: 10:33 AM
 */
public class ShipMachine extends AbstractStateMachineWithoutContext<ShipMachine, ShipMachine.ShipState, String> {
    protected ShipMachine(
            ImmutableState<ShipMachine, ShipState, String, Void> initialState,
            Map<ShipState, ImmutableState<ShipMachine, ShipState, String, Void>> states) {
        super(initialState, states);
    }

    public enum ShipState {
        PLAN, SHIPPING, CLEARANCE, DONE
    }

    private static final StateMachineBuilder<ShipMachine, ShipState, String, Void> builder;

    static {
        builder = StateMachineBuilderFactory.create(ShipMachine.class, ShipState.class, String.class);
        builder.externalTransition().from(ShipState.PLAN).to(ShipState.SHIPPING).on("next").when(new Condition<Void>() {
            @Override
            public boolean isSatisfied(Void context) {
                return true;
            }
        }).perform(new StdOut());
        builder.externalTransition().from(ShipState.SHIPPING).to(ShipState.CLEARANCE).on("next")
                .when(Conditions.<Void>always()).perform(new StdOut());
        builder.externalTransition().from(ShipState.CLEARANCE).to(ShipState.DONE).on("next")
                .when(Conditions.<Void>always());
    }


    static class StdOut implements Action<ShipMachine, ShipState, String, Void> {
        @Override
        public void execute(ShipState from, ShipState to, String event, Void context, ShipMachine stateMachine) {
            System.out.println(String.format("From %s to %s on event %s", from, to, event));
        }
    }

    public static <T extends StateMachineWithoutContext> T newMachine() {
        return (T) newMachine(ShipState.PLAN);
    }

    public static ShipMachine newMachine(ShipState state) {
        return builder.newStateMachine(state);
    }
}
