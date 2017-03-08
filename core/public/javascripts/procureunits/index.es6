/**
 * Created by licco on 2016/11/14.
 */
$(() => {
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
      $.post('/Inbounds/createValidate', $("#create_deliveryment").serialize() + "&type=" + $btn.attr("id"), r => {
        if (r.flag) {
          $("#create_deliveryment").attr("action", $btn.data("url")).submit();
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

  $("#stage").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '状态',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  $(".btn:contains(搜索)").click(function (e) {
    e.preventDefault();
    $("#type").val("");
    $("#search_Form").submit();
  });

  $("button[name='splitBtn']").click(function (e) {
    e.preventDefault();
    $("#type").val($(this).text() == '采购分拆' ? 'ProcureSplit' : 'StockSplit');
    $("#search_Form").submit();
  });

  $(document).ready(function () {
    $("#unit_table").dataTable({
      "sDom": "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
      "sPaginationType": "full_numbers",
      "iDisplayLength": 50,
      "aoColumnDefs":[
        {"bSortable": false, "aTargets": [0]}
      ]
    });
  });

  $("#unit_table").on("click", "input[name='editBoxInfo']", function (e) {
    e.stopPropagation();
    let ids = $(this).data("id");
    $("#fba_carton_contents_modal").modal('show');
    $("#refresh_div").load("/ProcureUnits/refreshFbaCartonContentsByIds", {ids: ids}, function () {
      $.getScript('/public/javascripts/inbounds/boxInfo.js');
    });
  });

  $("#submitBoxInfoBtn").click(function (e) {
    e.stopPropagation();
    let form = $("<form method='post'></form>");
    form = form.append($("#box_info_table").clone());
    $.post('/ProcureUnits/updateBoxInfo', form.serialize(), function (re) {
      if (re) {
        $("#fba_carton_contents_modal").modal('hide');
        noty({
          text: '更新包装信息成功!',
          type: 'success'
        });
        window.location.reload();
      } else {
        noty({
          text: r.message,
          type: 'error'
        });
      }
    });
  });

});

