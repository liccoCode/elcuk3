package models.view.dto;

import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.highchart.AbstractSeries;
import models.view.highchart.HighChart;
import org.apache.commons.lang3.StringUtils;
import query.ShipmentReportESQuery;
import services.MetricShipmentService;

import java.math.BigDecimal;
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

    public Float seaUnit;

    public float airFreight;

    public float airWeight;

    public Float airUnit;

    public float expressFreight;

    public float expressWeight;

    public Float expressUnit;

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
            if(dto.seaWeight > 0 && dto.seaFreight > 0) {
                dto.seaUnit = dto.seaFreight / dto.seaWeight;
                dto.seaUnit = new BigDecimal(dto.seaUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                        .floatValue();
            }
            if(dto.airWeight > 0 && dto.airFreight > 0) {
                dto.airUnit = dto.airFreight / dto.airWeight;
                dto.airUnit = new BigDecimal(dto.airUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                        .floatValue();
            }
            if(dto.expressWeight > 0 && dto.expressFreight > 0) {
                dto.expressUnit = dto.expressFreight / dto.expressWeight;
                dto.expressUnit = new BigDecimal(dto.expressUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                        .floatValue();
            }
            dto.seaWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaWeight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            dto.seaFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaFreight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            dto.airWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airFreight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            dto.airFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airFreight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            dto.expressWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressWeight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            dto.expressFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressFreight)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

            dtos.add(dto);
        }
        return dtos;
    }

}
