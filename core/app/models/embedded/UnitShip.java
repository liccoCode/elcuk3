package models.embedded;

import com.google.gson.annotations.Expose;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:30 PM
 */
@Embeddable
public class UnitShip implements Serializable {

    private static final long serialVersionUID = 7216438149653150961L;
    @Expose
    public Integer inboundQTY;
    /**
     * 外联 ID, 例如 FBA 的运输货号
     */
    @Expose
    public String outerId;
}
