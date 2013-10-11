package factory.finance;


import factory.ModelFactory;
import models.finance.TransportApply;



public class TransportApplyFactory extends ModelFactory<TransportApply> {
    @Override
    public TransportApply define() {
        TransportApply transportApply = new TransportApply();
        transportApply.serialNumber = "SQK-周伟-000-0";

        return transportApply;
    }

}
