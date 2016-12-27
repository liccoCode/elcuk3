/**
 * Created by licco on 2016/12/13.
 */
$(() => {
  $("input[name='editBoxInfo']").click(function(e) {
    e.stopPropagation();
    $("#fba_carton_contents_modal").modal('show');
    let id = $(this).data("id");
    $("#refresh_div").load("/Refunds/refreshFbaCartonContentsByIds", {id: id});
  });

  $("#submitBoxInfoBtn").click(function(e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>")
    form = form.append($("#box_info_table").clone())
    $.post('/Refunds/updateBoxInfo', form.serialize(), function(re) {
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

  $("input[name$='qty']").change(function() {
    let $input = $(this);
    let value = $input.val();
    let origin = $input.data('origin');
    if (value > origin) {
      noty({
        text: '退货数超过计划数量!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }
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
    let ids = [];
    $("#unit_table input[type='checkbox']:checked").each(function() {
      ids.push($(this).val());
    });
    let msg = "确认解除选中采购计划吗？";
    if ($("#unit_table input[name='ck']").not("input:checked").length == 0) {
      msg = "确定全部删除此单下面全部采购计划吗，确定之后此单状态将变为【已作废】？";
    }

    if (confirm(msg)) {
      $.post("/Refunds/deleteUnit", {ids: ids}, function(r) {
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

  $("#deleteBtnByCreate").click(function(e) {
    e.stopPropagation();
    if ($("#unit_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    if ($("#unit_table input[name='ck']").not("input:checked").length == 0) {
      noty({
        text: "不能全部删除此单下的采购计划，如需删除，请点击取消按钮!",
        type: 'error'
      });
      return false;
    }
    if (confirm("确认解除选中采购计划吗？")) {
      $("#unit_table input[type='checkbox']:checked").each(function() {
        $(this).parent("td").parent("tr").remove();
      });
    }
  });

});
