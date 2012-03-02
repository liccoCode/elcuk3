package models.procure;

import play.db.jpa.GenericModel;

import javax.persistence.Id;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 1:29 PM
 */
public class Inbound extends GenericModel {
    @Id
    public String id;
}
