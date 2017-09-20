/**
 * Created by even on 2016/11/14.
 */
$(() => {

  //列表状态查询下拉框样式处理
  $("#stage").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '状态',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  //点击明细修改按钮，显示弹出框,并初始化明细数据
  $("#unit_table a[name='unitUpdateBtn']").click(function (e) {
    let id = $(this).attr("uid");
    let $input = $(this);
    $.get('/MaterialUnits/findMaterialUnit', {
      id: id
    }, function (r) {
      //赋值
      $("#unit_id").val(r.id);
      $("#unit_planQty").val(r.planQty);
      $("#unit_planPrice").val(r.planPrice);
      $("#unit_planCurrency").val(r.planCurrency);
      $("#bom_modal").modal('show')
    });

  });

  //提交修改明细功能
  $("#submitUpdateBtn").click(function () {
    $("#updateUnit_form").submit();
  });

  $("#create_deliverplan_btn").click(function (e) {
    e.preventDefault();
    let $form = $("#create_materialUnit");
    window.open('/MaterialPlans/materialPlan?' + $form.serialize(), "_blank");
  });

  //创建 入库单,出库单,退货单 js处理
  $('#createInboundBtn,#createOutboundBtn,#createRefundBtn').click(function (e) {
    e.stopPropagation();
    let $btn = $(this);
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的采购单元',
        type: 'error'
      });
      return false;
    } else if (validProNameAndCooperName($btn)) {
      $.post('/MaterialUnits/createValidate', $("#create_materialUnit").serialize() + "&type=" + $btn.attr("id"), r => {
        if (r.flag) {
          $("#create_materialUnit").attr("action", $btn.data("url")).submit();
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }
  });

  function validProNameAndCooperName ($btn) {
    let firstProjectName = $('input[name="pids"]:checked').first().attr("project");
    let firstCooper = $("input[name='pids']:checked").first().attr("cooperName");
    let firstStage = $("input[name='pids']:checked").first().attr("stage");
    let firstWhouse = $("input[name='pids']:checked").first().attr("whouse");
    let firstShipType = $("input[name='pids']:checked").first().attr("shipType");
    if (firstProjectName == 'MengTop') {
      return true;
    }
    let o = 0;
    let i = 0;
    let j = 0;
    let k = 0;
    $("input[name='pids']:checked").each(function () {
      if ($(this).attr("project") != firstProjectName) {
        i++;
      }
      if ($(this).attr("cooperName") != firstCooper) {
        j++;
      }
      if ($(this).attr("whouse") != firstWhouse) {
        o++;
      }
      if ($(this).attr("shipType") != firstShipType) {
        k++;
      }
    });
    if (j > 0 && $btn.attr("id") != 'createOutboundBtn') {
      noty({
        text: '请选择【供应商】一致的采购计划！',
        type: 'error'
      });
      return false;
    }

    if ($btn.attr("id") == 'createOutboundBtn') {
      if (!firstWhouse) {
        noty({
          text: '采购计划的【目的国家】未填写！',
          type: 'error'
        });
        return false;
      }
      if (!firstShipType) {
        noty({
          text: '采购计划的【运输方式】未填写！',
          type: 'error'
        });
        return false;
      }
      if (o > 0) {
        noty({
          text: '请选择【目的国家】一致的采购计划！',
          type: 'error'
        });
        return false;
      }
      if (i > 0) {
        noty({
          text: '请选择【项目名称】一致的采购计划！',
          type: 'error'
        });
        return false;
      }
      if (k > 0) {
        noty({
          text: '请选择【运输方式】一致的采购计划！',
          type: 'error'
        });
        return false;
      }
    }

    if ($btn.attr("id") == 'createRefundBtn') {
      i = 0;
      $("input[name='pids']:checked").each(function () {
        if ($(this).attr("stage") != firstStage) {
          i++;
        }
      });
      if (i > 0) {
        noty({
          text: '请选择相同【阶段】的采购单元',
          type: 'error'
        });
        return false;
      }
    }
    return true;
  }

});

