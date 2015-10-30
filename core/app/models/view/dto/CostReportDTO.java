package models.view.dto;

import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.highchart.AbstractSeries;
import models.view.highchart.HighChart;
import org.apache.commons.lang3.StringUtils;
import query.ShipmentReportESQuery;
import services.MetricShipmentService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by licco on 15/10/29.
 */
public class CostReportDTO {

    public String market;

    public float seaFreight;

    public float seaWeight;

    public float seaUnit;

    public float airFreight;

    public float airWeight;

    public float airUnit;

    public float expressFreight;

    public float expressWeight;

    public float expressUnit;

    public static List<CostReportDTO> setReportData(int year, int month) {
        List<CostReportDTO> dtos = new ArrayList<CostReportDTO>();
        Date from = Dates.getMonthFirst(year, month);
        Date to = Dates.getMonthLast(year, month);
        for(M m : M.values()) {
            CostReportDTO dto = new CostReportDTO();
            dto.market = m.label();
            for(Shipment.T type : Shipment.T.values()) {
                MetricShipmentService mes = new MetricShipmentService(from, to, type, m);
                Float weight = mes.countShipWeight();
                Float freight = mes.countShipFee();
                if(weight.floatValue() > 0 || freight.floatValue() > 0) {
                    if(type.name().equals("SEA")) {
                        dto.seaWeight = weight;
                        dto.seaFreight = freight;
                    } else if(type.name().equals("AIR")) {
                        dto.airWeight = weight;
                        dto.airFreight = freight;
                    } else if(type.name().equals("EXPRESS")) {
                        dto.expressWeight = weight;
                        dto.expressFreight = freight;
                    }
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

}
