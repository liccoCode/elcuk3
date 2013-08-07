package factory.procure;

import factory.ModelFactory;
import factory.annotation.Factory;
import models.procure.Cooperator;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/17/13
 * Time: 11:47 AM
 */
public class CooperatorFactory extends ModelFactory<Cooperator> {
    private Cooperator base() {
        Cooperator cooper = new Cooperator();
        cooper.name = "cooperName";
        cooper.address = "cooper address";
        cooper.contacter = "cooper contacter";
        cooper.fax = "cooper fax";
        return cooper;
    }

    @Override
    public Cooperator define() {
        Cooperator cooper = base();
        cooper.type = Cooperator.T.SUPPLIER;
        return cooper;
    }

    @Factory(name = "shipper")
    public Cooperator shipper() {
        Cooperator cooper = base();
        cooper.type = Cooperator.T.SHIPPER;
        return cooper;
    }
}
