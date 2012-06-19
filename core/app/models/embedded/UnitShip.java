package models.embedded;

import com.google.gson.annotations.Expose;

import javax.persistence.Embeddable;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:30 PM
 */
@Embeddable
public class UnitShip {
    @Expose
    public Integer inboundQTY;
    /**
     * 外联 ID, 例如 FBA 的运输货号
     */
    @Expose
    public String outerId;
}
