package models.view.dto;

import models.OperatorConfig;
import models.procure.Shipment;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/12
 * Time: 下午4:52
 */
public class TransportChannelDto {

    public Shipment.T type;

    public List<String> channels;

    public OperatorConfig config;

    public TransportChannelDto(Shipment.T type, List<String> channels, OperatorConfig config) {
        this.type = type;
        this.channels = channels;
        this.config = config;
    }

}
