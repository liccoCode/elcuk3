/**
 * Created by licco on 2016/11/30.
 */
$(() => {
  $("#confirmOutboundBtn").click(function(e) {
    e.stopPropagation();
    let num = $("input[name='ids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要出库的数据!',
        type: 'error'
      });
    } else if (confirm("确认出库 " + num + " 条出库单吗?")) {
      let i = 0;
      $("input[name='ids']:checked").each(function() {
        if ($(this).attr("status") != "Create") {
          i++;
          noty({
            text: $(this).val() + '已经出库，请选择【已创建】的出库单',
            type: 'error'
          });
          return false;
        }
      });
      if (i == 0) {
        $("#submit_form").submit();
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

  $("#printBtn").click(function(e) {
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

  $("input[name='editBoxInfo']").click(function(e) {
    e.stopPropagation();
    $("#fba_carton_contents_modal").modal('show');
    let id = $(this).data("id");
    $("#refresh_div").load("/ProcureUnits/refreshFbaCartonContentsByIds", {id: id});
  });

  $("#submitBoxInfoBtn").click(function(e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>")
    form = form.append($("#box_info_table").clone())
    $.post('/ProcureUnits/updateBoxInfo', form.serialize(), function(re) {
      if (re) {
        $("#fba_carton_contents_modal").modal('hide');
        noty({
          text: '更新包装信息成功!',
          type: 'success'
        });
      } else {
        noty({
          text: r.message,
          type: 'error'
        });
      }
    });
  });

  $("#deleteBtn").click(function(e) {
    e.stopPropagation();
    if ($("#unit_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    if ($("#unit_table input[type='checkbox']").not("input:checked").length == 0) {
      noty({
        text: "不能全部删除采购计划!",
        type: 'error'
      });
      return false;
    }

    if (confirm("确认解除选中采购计划吗？")) {
      let ids = [];
      $("#unit_table input[type='checkbox']:checked").each(function() {
        ids.push($(this).val());
      });
      $.post("/ProcureUnits/deleteUnit", {ids: ids}, function(r) {
        if (r) {
          $("#unit_table input[type='checkbox']:checked").each(function() {
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

});
