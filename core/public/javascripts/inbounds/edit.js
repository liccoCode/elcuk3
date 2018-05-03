/**
 * Created by licco on 2016/11/17.
 */
$(() => {
  $('#confirmReceiveBtn,#confirmQCBtn,#confirmInboundBtn').click(function (e) {
    $('#edit_inbound_form').attr('action', $(this).data('url'));
    if ($("input[name='inbound.receiveDate']").val() == "") {
      noty({
        text: '收货日期必填!',
        type: 'error'
      });
      $("input[name='inbound.receiveDate']").focus();
    }
    if ($("input[name='inbound.name']").val() == "") {
      noty({
        text: '收货入库单名称必填!',
        type: 'error'
      });
      $("input[name='inbound.name']").focus();
    }

    let valid = true;
    $("input[name='qty']").each(function () {
      if ($(this).val() <= 0) {
        noty({
          text: '实际交货数量不能等于0!',
          type: 'warning'
        });
        $(this).focus();
        valid = false;
        return false;
      }
    });
    if (valid) {
      $('#edit_inbound_form').submit();
    }
  });

  $('#unit_table').on('change', 'td>:input[name]', function () {
    let $input = $(this);
    let id = $(this).parents('tr').find('input[name$=id]').val();
    let attr = $input.attr('name');
    let value = $input.val();

    //实际交货数量
    if (attr == 'qty' && $(this).val() < 0) {
      noty({
        text: '收货数量不能小于0!',
        type: 'error'
      });
      $(this).val(0);
      return;
    }

    if (attr == 'qty' && $(this).val() == $(this).data('qty')) {
      $(this).parent('td').next().find('select').hide();
      $(this).attr("style", "width:58px; padding:10px;");
    } else if (attr == 'qty' && $(this).val() != $(this).data('qty')) {
      if ($(this).val() != $(this).data('qty')) {
        $(this).attr("style", "width:58px; padding:10px; background-color:red;");
      } else {
        $(this).attr("style", "width:58px; padding:10px;");
      }
      $(this).parent('td').next().find('select').show();
    }

    //交货不足处理方式
    if (attr == 'handType' && value == 'Delivery') {
      let $input = $(this).parent('td').prev().find("input");
      if ($input.val() / $input.data('qty') < 0.9) {
        $input.attr("style", "width:35px;background-color:yellow;");
      }
    } else {
      $(this).parent('td').prev().find("input").attr("style", "width:58px; padding:10px;");
    }

    //质检结果
    if (attr == 'result' && value == 'Unqualified') {
      $(this).parent('td').next().next().find('input').hide();
      $(this).parent('td').next().find('input').show().val(0).prop("readonly", true);
      $(this).parent('td').next().next().find('input').show().val($(this).attr("qty")).prop("readonly", true);
    } else if (attr == 'result' && value == 'Qualified') {
      $(this).parent('td').next().next().find('input').show();
      $(this).parent('td').next().find('input').show();
      $(this).parent('td').next().find('input').val($(this).attr("qty")).prop("readonly", false);
      $(this).parent('td').next().next().find('input').show().val(0).prop("readonly", false);
    } else if (attr == 'result' && value == 'UnCheck') {
      $(this).parent('td').next().next().find('input').hide();
      $(this).parent('td').next().find('input').hide();
    }

    if (attr == 'qualifiedQty') {
      if (value < 0) {
        noty({
          text: '合格数不能小于0!',
          type: 'error'
        });
        $(this).val($(this).data("origin"));
        return;
      }
      let qty = $(this).data('qty');
      if (value > qty) {
        noty({
          text: '合格数超过实际收货数量!',
          type: 'error'
        });
        $(this).val(qty);
        return false;
      }
      $(this).parent('td').next().find('input').prop("value", qty - value);
    }

    if (attr == 'unqualifiedQty') {
      let qty = $(this).data('qty');
      if (value < 0) {
        noty({
          text: '不合格数不能小于0!',
          type: 'error'
        });
        $(this).val($(this).data("origin"));
        return;
      }
      if (value > qty) {
        noty({
          text: '不合格数超过实际收货数量!',
          type: 'error'
        });
        $(this).val($(this).data('origin'));
        return false;
      }
      $(this).parent('td').prev().find('input').val(qty - value);
      $(this).data('origin', value);
    }

    if (attr == 'inboundQty') {
      let qty = $(this).data('qty');
      if (value > qty) {
        noty({
          text: '入库数超过实际收货数量!',
          type: 'error'
        });
        $(this).val($(this).data('origin'));
        return false;
      }
      $(this).data('origin', value);
    }

    if ($(this).val()) {
      $.post('/Inbounds/updateUnit', {
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

  $("input[name='editBoxInfo']").click(function (e) {
    e.stopPropagation();
    let id = $(this).data("id");
    refreshDiv(id);
  });

  $("#submitBoxInfoBtn").click(function (e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>");
    form = form.append($("#box_info_table").clone());
    $.post('/Inbounds/updateBoxInfo', form.serialize(), function (re) {
      if (re) {
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

  $("#deleteBtn").click(function (e) {
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
      $("#unit_table input[type='checkbox']:checked").each(function () {
        ids.push($(this).val());
      });
      $.post("/Inbounds/deleteUnit", {ids: ids}, function (r) {
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

  $("#batchUpdateBoxInfoBtn").click(function () {
    let ids = [];
    $("input[type=checkbox]:checked").each(function () {
      if ($(this).val()) {
        ids.push($(this).val());
      }
    });
    refreshDiv(ids);
  });

  function refreshDiv (ids) {
    $("#refresh_div").load("/Inbounds/refreshFbaCartonContentsByIds", {ids: ids}, function () {
      $("#fba_carton_contents_modal").modal('show');
      $.getScript('/public/javascripts/inbounds/boxInfo.js');
    });
  }

  let inboundUnitId = window.location.hash.slice(1);
  let targetTr = $("#inboundUnit_" + inboundUnitId);
  if (targetTr.size() > 0) {
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
  }

});

const attrsFormat = {
  'qty': "实际交货数量",
  "handType": '交货不足处理方式',
  "result": '质检结果',
  "way": '质检不合格处理方式',
  "qualifiedQty": '合格数',
  "unqualifiedQty": '不良品数',
  "inboundQty": '实际入库数',
  "target": '目标仓库'
};



