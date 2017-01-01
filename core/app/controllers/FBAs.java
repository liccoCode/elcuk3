package controllers;

import controllers.api.SystemOperation;
import helper.*;
import models.market.Account;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.qc.CheckTaskDTO;
import models.view.Ret;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Validation;
import play.libs.Files;
import play.modules.pdf.PDF;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.List;

import static play.modules.pdf.PDF.renderPDF;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:38 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class FBAs extends Controller {
    @Check("fbas.deploytoamazon")
    public static void deploysToAmazon(String target,
                                       String id,
                                       List<Long> pids,
                                       List<CheckTaskDTO> dtos) {
        if(pids == null || pids.size() == 0) {
            Validation.addError("", "必须选择需要创建 FBA 的采购计划");
        } else if(pids.size() != dtos.size()) {
            Validation.addError("", "FBA 箱内信息的个数与采购计划的数量不一致");
        }
        if(!Validation.hasErrors()) ProcureUnit.postFbaShipments(pids, dtos);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("选择的采购计划全部成功创建 FBA");
        }
        redirect(LinkHelper.getRedirect(id, target));
    }

    @Check("fbas.update")
    public static void update(Long procureUnitId) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        unit.fba.updateFBAShipment(null);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("FBA %s 更新成功.", unit.fba.shipmentId);
        }
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 更换FBA
     *
     * @param procureUnitId
     */
    public static void changeFBA(Long procureUnitId, List<CheckTaskDTO> dtos) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        unit.fba.removeFBAShipment();
        unit.postFbaShipment(dtos.get(0));
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("FBA %s 更换成功.", unit.fba.shipmentId);
        }
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 创建 FBAInboundShipmentPlan
     *
     * @param unitId 采购计划
     */
    public static void plan(Long unitId) {
        ProcureUnit unit = ProcureUnit.findById(unitId);
        FBAShipment fba = unit.planFBA();
        renderJSON(J.json(GTs.newMap("fba", fba)
                .put("message", Webs.V(Validation.errors()))
                .build()));
    }

    /**
     * 确认 FBAInboundShipmentPlan 并关联上采购计划
     *
     * @param unitId 采购计划
     * @param fbaId  FBAShipment
     * @param result 是否需要这个 FBA
     */
    public static void confirm(Long unitId, Long fbaId, boolean result) {
        ProcureUnit unit = ProcureUnit.findById(unitId);
        FBAShipment fba = FBAShipment.findById(fbaId);
        if(result) {
            //取出旧的 FBA 上已经提交过的 CheckTaskDTO 信息
            CheckTaskDTO dto = unit.loadCheckTaskDTO();
            //确认新的 FBA
            unit.confirmFBA(fba);
            //提交 FBA 包装信息到新的 FBA
            if(dto != null) unit.submitFBACartonContent(dto);
        } else {
            //向 Amazon 请求删除 FBA(最多三次)
            fba.removeFBAShipmentRetry(3);
            fba.delete();
        }
        renderJSON(new Ret(!Validation.hasErrors(), Webs.V(Validation.errors())));
    }

    /**
     * 箱內麦
     *
     * @param id
     */
    public static void packingSlip(Long id, boolean html) {
        final FBAShipment fba = FBAShipment.findById(id);
        renderArgs.put("shipmentId", fba.shipmentId);
        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));

        if(html) {
            render();
        } else {
            final PDF.Options options = new PDF.Options();
            options.pageSize = IHtmlToPdfTransformer.A4P;
            renderPDF(options);
        }
    }

    /**
     * 箱外麦
     *
     * @param id
     */
    public static void boxLabel(Long id, boolean html, Long boxNumber) {
        FBAShipment fba = FBAShipment.findById(id);

        renderArgs.put("fba", fba);
        renderArgs.put("shipFrom", Account.address(fba.account.type));

        ProcureUnit procureUnit = fba.units.get(0);


        String shipmentid = fba.shipmentId;
        if(procureUnit.shipType == Shipment.T.EXPRESS) {
            shipmentid = shipmentid.trim() + "U";
        }
        renderArgs.put("shipmentId", shipmentid);
        renderArgs.put("procureUnit", procureUnit);
        renderArgs.put("boxNumber", boxNumber);
        if(html) {
            render();
        } else {
            PDF.Options options = new PDF.Options();
            //只设置 width height    margin 为零
            options.pageSize = new org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);
            renderPDF(options);
        }
    }

    /**
     * 重新提交 Feed 给 Amazon 处理(只适用于 Rockend 和 API Limit 等造成的提交失败)
     *
     * @param id
     * @param feedId
     */
    public static void reSubmit(Long id, Long feedId) {
        FBAShipment fba = FBAShipment.findById(id);
        notFoundIfNull(fba);
        fba.reSubmit(feedId);
        renderText("成功提交 Feed 给 Amazon 处理, 请等待 2~3 分钟后再来查看处理结果.");
    }

    /**
     * 更新 FBA 的箱内包装信息
     *
     * @param target
     * @param id
     * @param pids
     * @param dtos
     */
    public static void updateCartonContents(String target,
                                            String id,
                                            List<Long> pids,
                                            List<CheckTaskDTO> dtos) {
        if(pids == null || pids.isEmpty()) {
            Validation.addError("", "必须选择需要创建 FBA 的采购计划");
        } else if(pids.size() != dtos.size()) {
            Validation.addError("", "FBA 箱内信息的个数与采购计划的数量不一致");
        }
        if(!Validation.hasErrors()) ProcureUnit.postFbaCartonContents(pids, dtos);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("选择的采购计划全部成功提交更新 FBA, 请等待 3~5 分钟后查看.");
        }
        redirect(LinkHelper.getRedirect(id, target));
    }

    /**
     * 将选定的 出货 FBA 打成 ZIP 包并下载
     *
     * @param target
     * @param id
     * @param pids
     * @param boxNumbers
     */
    public static void downloadZip(String target,
                                   String id,
                                   List<Long> pids,
                                   List<Long> boxNumbers) {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要下载的采购计划");
        if(boxNumbers == null || boxNumbers.size() == 0 || pids.size() != boxNumbers.size())
            Validation.addError("", "采购单元箱数填写错误");
        if(Validation.hasErrors()) {
            redirect(LinkHelper.getRedirect(id, target));
        } else {
            //创建FBA根目录，存放工厂FBA文件
            File dirfile = new File(Constant.TMP, "FBA");
            try {
                Files.delete(dirfile);
                dirfile.mkdir();

                //生成工厂的文件夹. 格式：选中的采购单的id的组合a,b,c
                File factoryDir = new File(dirfile,
                        String.format("采购单元-%s-出货FBA", StringUtils.join(pids.toArray(), ",")));
                factoryDir.mkdir();
                for(int i = 0; i < pids.size(); i++) {
                    ProcureUnit procureunit = ProcureUnit.findById(pids.get(i));
                    procureunit.fbaAsPDF(factoryDir, boxNumbers.get(i));
                }

            } catch(Exception e) {
                Logger.error(Webs.S(e));
            } finally {
                File zip = new File(Constant.TMP + "/FBA.zip");
                Files.zip(dirfile, zip);
                zip.deleteOnExit();
                renderBinary(zip);
            }
        }
    }
}
