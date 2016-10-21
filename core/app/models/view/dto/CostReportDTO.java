package models.view.dto;

import helper.Caches;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import play.cache.Cache;
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

    public float seaUnit;

    public float seaVatPrice;

    public float seaVatUnit;

    public float airFreight;

    public float airWeight;

    public float airVatPrice;

    public float airUnit;

    public float airVatUnit;

    public float expressFreight;

    public float expressWeight;

    public float expressUnit;

    public float expressVatPrice = 0f;

    public float expressVatUnit;

    public static List<CostReportDTO> setReportData(Date from, Date to) {
        String key = Caches.Q.cacheKey(from, to, "reportDate");
        List<CostReportDTO> dtos = Cache.get(key, List.class);
        if(dtos != null && dtos.size() > 0) {
            return dtos;
        } else {
            dtos = new ArrayList<>();
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
                caluUnit(dto);
                dtos.add(dto);
            }
        }
        CostReportDTO totalDTO = new CostReportDTO();
        totalDTO.market = "合计";
        for(CostReportDTO dto : dtos) {
            totalDTO.seaWeight += dto.seaWeight;
            totalDTO.seaFreight += dto.seaFreight;
            totalDTO.seaVatPrice += dto.seaVatPrice;
            totalDTO.airWeight += dto.airWeight;
            totalDTO.airFreight += dto.airFreight;
            totalDTO.airVatPrice += dto.airVatPrice;
            totalDTO.expressWeight += dto.expressWeight;
            totalDTO.expressFreight += dto.expressFreight;
            totalDTO.expressVatPrice += dto.expressVatPrice;
        }
        caluUnit(totalDTO);
        dtos.add(totalDTO);
        Cache.delete(key);
        Cache.add(key, dtos, "4h");
        return dtos;
    }

    public static void caluUnit(CostReportDTO dto) {
        if(dto.seaWeight > 0 && dto.seaFreight > 0) {
            dto.seaUnit = dto.seaFreight / dto.seaWeight;
            dto.seaUnit = new BigDecimal(dto.seaUnit).setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            dto.seaUnit = 0.0f;
        }
        if(dto.airWeight > 0 && dto.airFreight > 0) {
            dto.airUnit = dto.airFreight / dto.airWeight;
            dto.airUnit = new BigDecimal(dto.airUnit).setScale(3, BigDecimal.ROUND_HALF_UP)
                    .floatValue();
        } else {
            dto.airUnit = 0.0f;
        }
        if(dto.expressWeight > 0 && dto.expressFreight > 0) {
            dto.expressUnit = dto.expressFreight / dto.expressWeight;
            dto.expressUnit = new BigDecimal(dto.expressUnit).setScale(3, BigDecimal.ROUND_HALF_UP)
                    .floatValue();
        } else {
            dto.expressUnit = 0.0f;
        }

        /**计算VAT+关税  与 重量 **/
        if(dto.seaWeight > 0 && dto.seaVatPrice > 0) {
            dto.seaVatUnit = dto.seaVatPrice / dto.seaWeight;
            dto.seaVatUnit = new BigDecimal(dto.seaVatUnit).setScale(3, BigDecimal.ROUND_HALF_UP)
                    .floatValue();
        } else {
            dto.seaVatUnit = 0.0f;
        }
        if(dto.airWeight > 0 && dto.airVatPrice > 0) {
            dto.airVatUnit = dto.airVatPrice / dto.airWeight;
            dto.airVatUnit = new BigDecimal(dto.airVatUnit).setScale(3, BigDecimal.ROUND_HALF_UP)
                    .floatValue();
        } else {
            dto.airVatUnit = 0.0f;
        }
        if(dto.expressWeight > 0 && dto.expressVatPrice > 0) {
            dto.expressVatUnit = dto.expressVatPrice / dto.expressWeight;
            dto.expressVatUnit = new BigDecimal(dto.expressVatUnit)
                    .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        } else {
            dto.expressVatUnit = 0.0f;
        }
        dto.seaWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaWeight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        dto.seaFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.seaFreight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        dto.airWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airWeight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        dto.airFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.airFreight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        dto.expressWeight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressWeight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        dto.expressFreight = new BigDecimal(Double.parseDouble(String.valueOf(dto.expressFreight)))
                .setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
    }

}
