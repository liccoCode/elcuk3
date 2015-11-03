package models.view.dto;

import helper.Caches;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.highchart.AbstractSeries;
import models.view.highchart.HighChart;
import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;
import query.ShipmentReportESQuery;
import services.MetricShipmentService;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by licco on 15/10/29.
 */
public class CostReportDTO implements Serializable {

    private static final long serialVersionUID = 4407545647400799337L;

    public String market;

    public float seaFreight;

    public float seaWeight;

    public Float seaUnit;

    public Float seaVatPrice;

    public Float seaVatUnit;

    public float airFreight;

    public float airWeight;

    public Float airVatPrice;

    public Float airUnit;

    public Float airVatUnit;

    public float expressFreight;

    public float expressWeight;

    public Float expressUnit;

    public Float expressVatPrice = 0f;

    public Float expressVatUnit;

    public static List<CostReportDTO> setReportData(Date from, Date to) {
        String key = Caches.Q.cacheKey(from, to, "reportDate");
        List<CostReportDTO> dtos = Cache.get(key, List.class);
        if(dtos != null && dtos.size() > 0) {
            return dtos;
        } else {
            dtos = new ArrayList<CostReportDTO>();
        }
        from = Dates.morning(from);
        to = Dates.night(to);
        MetricShipmentService mes = new MetricShipmentService(from, to, null, null);
        Map<String, Float> vat = mes.countVAT();
        for(M m : M.values()) {
            if(!m.name().equals("EBAY_UK")) {
                CostReportDTO dto = new CostReportDTO();
                dto.market = m.label();
                for(Shipment.T type : Shipment.T.values()) {
                    mes = new MetricShipmentService(from, to, type, m);
                    Float weight = mes.countShipWeight();
                    Float freight = mes.countShipFee();
                    Float vatPrice = vat.get(m.sortName().toUpperCase() + "_" + type.name()) == null ? 0f :
                            vat.get(m.sortName().toUpperCase() + "_" + type.name());
                    if(type.name().equals("SEA")) {
                        dto.seaWeight = weight;
                        dto.seaFreight = freight;
                        dto.seaVatPrice = vatPrice;
                    } else if(type.name().equals("AIR")) {
                        dto.airWeight = weight;
                        dto.airFreight = freight;
                        dto.airVatPrice = vatPrice;
                    } else if(type.name().equals("EXPRESS")) {
                        dto.expressWeight = weight;
                        dto.expressFreight = freight;
                        dto.expressVatPrice = vatPrice;
                    }
                }
                if(dto.seaWeight > 0 && dto.seaFreight > 0) {
                    dto.seaUnit = dto.seaFreight / dto.seaWeight;
                    dto.seaUnit = new BigDecimal(dto.seaUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.seaUnit = 0.0f;
                }
                if(dto.airWeight > 0 && dto.airFreight > 0) {
                    dto.airUnit = dto.airFreight / dto.airWeight;
                    dto.airUnit = new BigDecimal(dto.airUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.airUnit = 0.0f;
                }
                if(dto.expressWeight > 0 && dto.expressFreight > 0) {
                    dto.expressUnit = dto.expressFreight / dto.expressWeight;
                    dto.expressUnit = new BigDecimal(dto.expressUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.expressUnit = 0.0f;
                }

                /**计算VAT+关税  与 重量 **/
                if(dto.seaWeight > 0 && dto.seaVatPrice > 0) {
                    dto.seaVatUnit = dto.seaVatPrice / dto.seaWeight;
                    dto.seaVatUnit = new BigDecimal(dto.seaVatUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.seaVatUnit = 0.0f;
                }
                if(dto.airWeight > 0 && dto.airVatPrice > 0) {
                    dto.airVatUnit = dto.airVatPrice / dto.airWeight;
                    dto.airVatUnit = new BigDecimal(dto.airVatUnit.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.airVatUnit = 0.0f;
                }
                if(dto.expressWeight > 0 && dto.expressVatPrice > 0) {
                    dto.expressVatUnit = dto.expressVatPrice / dto.expressWeight;
                    dto.expressVatUnit = new BigDecimal(dto.expressVatUnit.doubleValue())
                            .setScale(2, BigDecimal.ROUND_HALF_UP)
                            .floatValue();
                } else {
                    dto.expressVatUnit = 0.0f;
                }
                dto.seaWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaWeight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dto.seaFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaFreight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dto.airWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airWeight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dto.airFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airFreight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dto.expressWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressWeight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dto.expressFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressFreight)))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                dtos.add(dto);
            }
        }
        Cache.delete(key);
        Cache.add(key, dtos, "4h");
        return dtos;
    }

}
