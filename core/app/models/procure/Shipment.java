package models.procure;

import play.db.jpa.GenericModel;

import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:53 PM
 */
public class Shipment extends GenericModel {

    @OneToOne(mappedBy = "shipment")
    public List<Procure> procures;

    @Id
    public String id;
}
