/**
 * Created by licco on 2016/11/30.
 */
$(() => {
  $("#confirmOutboundBtn").click(function (e) {
    e.stopPropagation();
    let num = $("input[name='ids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要出库的数据!',
        type: 'error'
      });
    } else {
      let i = 0;
      let ids = [];
      $("input[name='ids']:checked").each(function () {
        if ($(this).attr("status") != "Create") {
          i++;
          noty({
            text: $(this).val() + '已经出库，请选择【已创建】的出库单',
            type: 'error'
          });
          return false;
        }
        ids.push($(this).val());
      });
      if (i == 0) {
        $.post('/Outbounds/validOutboundQty', {ids: ids}, function (re) {
          if (re.flag) {
            $("#submit_form").submit();
          } else {
            if (confirm(re.message + "的可用库存与计划出库数量不一致，确认出库吗？")) {
              $("#submit_form").submit();
            }
          }
        });
      }
    }
  });

  $("select[name='outbound.type']").change(() => {
    showTypeSelect();
  });

  function showTypeSelect () {
    let type = $("select[name='outbound.type']").val();
    let $select = $("select[name='outbound.targetId']");
    switch(type) {
      case 'Normal' :
      case 'B2B' :
        $select.empty().append($("#shipperOptions").clone().html());
        break;
      case 'Refund' :
        $select.empty().append($("#supplierOptions").clone().html());
        break;
      case 'Process' :
        $select.empty().append($("#processOptions").clone().html());
        break;
      case 'Sample' :
        $select.empty().append($("#sampleOptions").clone().html());
        break;
      default:
        $select.empty();
    }
  }

  showTypeSelect();

  $("#printBtn").click(function (e) {
    e.stopPropagation();
    if ($("input[type='checkbox']:checked").length == 0) {
      noty({
        text: '请选择需要打印的出库单',
        type: 'error'
      });
      return;
    }
    let $form = $("#submit_form");
    window.open("/Outbounds/printOutboundForm?" + $form.serialize(), "_blank");
  });

  $("#data_table").on("click", "input[name='editBoxInfo']", function (e) {
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

  $("#deleteByCreateBtn").click(function (e) {
    e.stopPropagation();
    if ($("#data_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    if (confirm("确认解除选中采购计划吗？")) {
      $("#data_table input[type='checkbox']:checked").each(function () {
        $(this).parent("td").parent("tr").remove();
      });
      noty({
        text: "解除计划成功!",
        type: 'success'
      });
    }
  });

  $("#deleteBtn").click(function (e) {
    e.stopPropagation();
    if ($("#data_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    if (confirm("确认解除选中采购计划吗？")) {
      let ids = [];
      $("#data_table input[type='checkbox']:checked").each(function () {
        ids.push($(this).val());
      });
      $.post("/ProcureUnits/deleteUnit", {ids: ids}, function (r) {
        if (r) {
          $("#data_table input[type='checkbox']:checked").each(function () {
            $(this).parent("td").parent("tr").remove();
          });
          noty({
            text: "解除计划成功!",
            type: 'success'
          });
        } else {
          noty({
            text: "解除计划失败，请稍后再试，或者联系管理员!",
            type: 'error'
          });
        }
      });
    }
  });

  let unit_id = window.location.hash.slice(1);
  let targetTr = $("#unit_" + unit_id);
  if (targetTr.size() > 0) {
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
  }

});
