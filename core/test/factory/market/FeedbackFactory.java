package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.market.Account;
import models.market.Feedback;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/26/13
 * Time: 11:10 AM
 */
public class FeedbackFactory extends ModelFactory<Feedback> {
    @Override
    public Feedback define() {
        Feedback feedback = new Feedback();
        feedback.account = FactoryBoy.lastOrCreate(Account.class, "de");
        feedback.createDate = new Date();
        feedback.isRemove = false;
        return feedback;
    }
}
