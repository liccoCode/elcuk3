/**
 * Created by licco on 2016/12/13.
 */
$(() => {
  $("input[name='editBoxInfo']").click(function (e) {
    e.stopPropagation();
    let ids = $(this).data("id");
    refreshBoxInfoModal(ids);
  });

  $("#submitBoxInfoBtn").click(function (e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>")
    form = form.append($("#box_info_table").clone())
    $.post('/Refunds/updateBoxInfo', form.serialize(), function (re) {
      if (re.flag) {
        $("#refund_box_info_modal").modal('hide');
        noty({
          text: '更新包装信息成功!',
          type: 'success'
        });
      } else {
        noty({
          text: re.message,
          type: 'error'
        });
      }
    });
  });

  $("#batchUpdateBoxInfoBtn").click(function () {
    let ids = [];
    $("input[type=checkbox]:checked").each(function () {
      if ($(this).val()) {
        ids.push($(this).val());
      }
    });
    refreshBoxInfoModal(ids);
  });

  function refreshBoxInfoModal (ids) {
    $("#refresh_div").load("/Refunds/refreshFbaCartonContentsByIds", {ids: ids}, function () {
      $("#refund_box_info_modal").modal('show');
      $.getScript('/public/javascripts/inbounds/boxInfo.js');
    });
  }

  $("input[name='qty'],input[name$='qty']").change(function () {
    let $input = $(this);
    let id = $(this).data("id");
    let attr = $input.attr('name');
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

    if (value <= 0) {
      noty({
        text: '退货数不能小于等于0!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }

    if ($(this).val()) {
      $.post('/Refunds/updateUnit', {
        id: id,
        attr: attr,
        value: value
      }, (r) => {
        if (r) {
          const msg = attrsFormat[attr];
          noty({
            text: '更新' + msg + '成功!',
            type: 'success'
          });
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }

  });

  $("#deleteBtn").click(function (e) {
    e.stopPropagation();
    if ($("#unit_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    let ids = [];
    $("#unit_table input[type='checkbox']:checked").each(function () {
      ids.push($(this).val());
    });
    let msg = "确认解除选中采购计划吗？";
    if ($("#unit_table input[name='ck']").not("input:checked").length == 0) {
      msg = "确定全部删除此单下面全部采购计划吗，确定之后此单状态将变为【已作废】？";
    }

    if (confirm(msg)) {
      $.post("/Refunds/deleteUnit", {ids: ids}, function (r) {
        if (r) {
          $("#unit_table input[type='checkbox']:checked").each(function () {
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

  $("#deleteBtnByCreate").click(function (e) {
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
      $("#unit_table input[type='checkbox']:checked").each(function () {
        $(this).parent("td").parent("tr").remove();
      });
    }
  });

  let index = $("#unit_table input[type='checkbox']").length - 1;

  $("#quickAdd").click(function (e) {
    e.preventDefault();
    let id = $("#procureId").val();
    let cooperId = $("#cooperId").val();
    if ($("#unit_" + id).html() == undefined) {
      $.get("/refunds/validateAddRefund", {
        id: id,
        cooperId: cooperId
      }, function (r) {
        if (r.flag) {
          $.get("/procureunits/findProcureById", {
            id: id,
            index: index
          }, function (r) {
            index++;
            $("#unit_table").append(r);
            slippingTr(id);
          });
          $("#procureId").val("");
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    } else {
      noty({
        text: "采购计划已经存在该单据中!",
        type: 'error'
      });
      slippingTr(id);
    }
  });

  $("#quickAddByEdit").click(function (e) {
    e.preventDefault();
    let id = $("#procureId").val();
    let cooperId = $("#cooperId").val();
    if ($("#unit_" + id).html() == undefined) {
      $.get("/refunds/validateAddRefund", {
        id: id,
        cooperId: cooperId
      }, function (r) {
        if (r.flag) {
          let form = $('<form method="post" action=""></form>');
          form.attr('action', $("#procureId").data('url'));
          form.hide().append($('#procureId').parent('div').clone()).appendTo("body");
          form.submit().remove();
        }
      });
    } else {
      noty({
        text: "采购计划已经存在该单据中!",
        type: 'error'
      });
      slippingTr(id);
    }
  });

  let unitId = window.location.hash.slice(1);
  let targetTr = $("#unit_" + unitId);
  if (targetTr.size() > 0) {
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
  }

  function slippingTr (id) {
    EF.scoll($("#unit_" + id));
    EF.colorAnimate($("#unit_" + id));
  }

});

const attrsFormat = {
  'qty': "实际退货数量"
};
