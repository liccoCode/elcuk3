package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.OperatorConfig;
import models.material.Material;
import models.material.MaterialBom;
import models.material.MaterialOutbound;
import models.material.MaterialOutboundUnit;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.MaterialOutboundPost;
import models.view.post.MaterialPost;
import models.whouse.Outbound;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物料出库单Controller
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: PM3:04
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialOutbounds extends Controller {

    @Before(only = {"index", "blank", "edit" ,"indexMaterial"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        List<MaterialBom> boms = MaterialBom.findAll();
        renderArgs.put("boms", boms);
        String id = request.params.get("id");
        if(id != null)
            renderArgs.put("records", ElcukRecord.records(id));
    }

    /**
     * 查询物料库存信息
     *
     * @param p
     */
    public static void indexMaterial(MaterialPost p) {
        if(p == null) p = new MaterialPost();
        List<Material> materials = p.outBoundQuery();
        int size = materials.size();
        render(p, materials, size);
    }

    public static void index(MaterialOutboundPost p) {
        if(p == null) p = new MaterialOutboundPost();
        List<MaterialOutbound> materialOutbounds = p.query();
        render(p, materialOutbounds);
    }

    /**
     * 根据出库单ID查询出库计划集合
     *
     * @param id
     */
    public static void showMaterialOutboundUnitList(String id) {
        MaterialOutbound outbound = MaterialOutbound.findById(id);
        List<MaterialOutboundUnit> units = outbound.units;
        render("/MaterialOutbounds/_units.html", units);
    }

    public static void blank(List<Long> pids) {
        List<Material> units = Material.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        MaterialOutbound outbound = new MaterialOutbound();
        render(units, outbound);
    }

    public static void create(MaterialOutbound outbound, List<Material> dtos) {
        Validation.required("物料出库单名称", outbound.name);
        Validation.required("物料出库单出库类型", outbound.type);
        Validation.required("物料出库单出库日期", outbound.name);
        outbound.id = MaterialOutbound.id();
        outbound.handler = Login.current();
        outbound.save();
        for(Material dto : dtos) {
            if(dto != null) {
                Material material = Material.findById(dto.id);
                MaterialOutboundUnit unit = new MaterialOutboundUnit();
                unit.materialOutbound = outbound;
                unit.material = material;
                unit.outQty = dto.outQty;
                unit.save();
            }
        }
        flash.success("物料出库单【" + outbound.id + "】创建成功!");
        index(new MaterialOutboundPost());
    }

    public static void edit(String id) {
        MaterialOutbound outbound = MaterialOutbound.findById(id);
        String brandName = OperatorConfig.getVal("brandname");
        boolean qtyEdit = false;
        if(outbound.status == Outbound.S.Create) {
            qtyEdit = true;
        }
        render(outbound, brandName ,qtyEdit);
    }


    public static void update(MaterialOutbound outbound, String rid) {
        MaterialOutbound old = MaterialOutbound.findById(rid);
        old.saveAndLog(outbound);
        flash.success("物料出库单【" + rid + "】更新成功!");
        edit(rid);
    }

    /**
     * 出库单修改页面ajax修改出库明细的实际出库数
     *
     * @param id
     * @param value
     */
    public static void updateUnit(String id, String value) {
        MaterialOutboundUnit unit = MaterialOutboundUnit.findById(Long.parseLong(id));
        unit.outQty = Integer.parseInt(value);
        unit.save();
        renderJSON(new Ret());
    }

    /**
     * 出库单修改页面ajax 解绑
     *
     * @param ids
     */
    public static void deleteUnit(Long[] ids) {
        List<MaterialOutboundUnit> list = MaterialOutboundUnit.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        String outId = list.get(0).materialOutbound.id;
        MaterialOutbound outbound = MaterialOutbound.findById(outId);
        for(MaterialOutboundUnit unit : list) {
            unit.materialOutbound = null;
            unit.save();
        }
        if(outbound.units.size() == 0) {
            outbound.status = Outbound.S.Cancel;
            outbound.save();
        }
        renderJSON(new Ret(true));
    }


    /**
     * 出库单列表页面 出库单出库ajax验证
     *
     * @param ids
     */
    public static void validMaterialOutboundQty(String[] ids) {
        List<MaterialOutbound> list = MaterialOutbound.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        List<Long> temp = new ArrayList<>();
        for(MaterialOutbound out : list) {
            temp.addAll(out.units.stream().filter(unit -> unit.material.availableQty() < unit.outQty)
                    .map(unit -> unit.id).collect(Collectors.toList()));
        }
        if(temp.size() > 0) {
            renderJSON(new Ret(false, SqlSelect.inlineParam(temp)));
        }
        renderJSON(new Ret(true));
    }

    /**
     * 出库单列表页面 出库单出库
     *
     * @param ids
     */
    public static void confirmMaterialOutbound(List<String> ids) {
        MaterialOutbound.confirmOutBound(ids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            index(new MaterialOutboundPost());
        }
        flash.success(SqlSelect.inlineParam(ids) + "出库成功!");
        index(new MaterialOutboundPost());
    }

    /**
     * MaterialOutbound 添加 MaterialOutboundUnit
     */
    public static void addunits(String id, String code) {
        Validation.required("materialOutbound.addunits", code);
        if(Validation.hasErrors()) edit(id);

        MaterialOutbound.addunits(id, code);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            edit(id);
        }
        flash.success("物料 %s 添加成功.", code);
        edit(id);
    }
}
