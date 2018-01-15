/**
 * Created by licco on 2016/11/17.
 */

$(() => {
  //创建出货单blank预览页面验证出货数量js方法
  $('#unit_table').on('change', 'td>:input[name$=outQty]', function () {
    let $input = $(this);
    let surplusConfirmQty = $input.attr('surplusConfirmQty');

    //交货数量
    if ($(this).val() < 0) {
      noty({
        text: '收货数量不能小于0!',
        type: 'error'
      });
      $(this).val(0);
      return;
    }
    //交货数量
    if (parseInt($(this).val()) > parseInt(surplusConfirmQty)) {
      noty({
        text: '收货数量不能大于采购余量!',
        type: 'error'
      });
      $(this).val(0);
      return;
    }
  });

  //修改出货单 show页面验证出货数量js方法
  $('#unit_table').on('change', 'td>:input[name$=qty]', function () {
    let $input = $(this);
    let id = $(this).parents('tr').find('input[name$=pids]').val();
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
      $(this).attr("style", "width:35px;");
    } else if (attr == 'qty' && $(this).val() != $(this).data('qty')) {
      if ($(this).val() / $(this).data('qty') < 0.9 && $(this).parent('td').next().find('select').val() == 'Delivery') {
        $(this).attr("style", "width:35px;background-color:yellow;");
      }
      $(this).parent('td').next().find('select').show();
    }

    if ($(this).val()) {
      $.post('/MaterialPlans/updateUnit', {
        id: id,
        attr: attr,
        value: value
      }, (r) => {
        if (r.flag) {
          noty({
            text: '更新交货数量成功!',
            type: 'success'
          });
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
          $(this).val(0);
        }
      });
    }
  });

  //点击明细修改按钮，显示弹出框,并初始化明细数据
  $("#unit_table a[name='unitUpdateBtn']").click(function (e) {
    let id = $(this).attr("uid");
    //赋值
    $("#unit_id").val(id);
    $("#bom_modal").modal('show');
  });

  //签收异常js处理
  $('#submitUpdateBtn').click(function (e) {
    e.stopPropagation();
    let $btn = $(this);
    let receiptQty = $("#unit_receiptQty").val();
    let $aobj = $("#qs_" + $("#unit_id").val());
    if (receiptQty == null || receiptQty == undefined || receiptQty == '' || isNaN(receiptQty)) {
      alert("请输入数字");
      $("#unit_receiptQty").focus();
      return false;
    } else {
      $.post('/MaterialPlans/updateMaterialPlanUnit', $("#updateUnit_form").serialize(), (r) => {
        if (r) {
          $aobj.parent("td").text(receiptQty);
          $aobj.remove();
          $('#bom_modal').modal('hide');
          noty({
            text: '更新成功!',
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

  //确认js处理
  $('#confirmPlanBtn').click(function (e) {
    var $form;
    e.preventDefault();
    $form = $("#confirm_form");

    return $form.submit();

  });

  //快速添加物料编码js处理
  $('#addPlanUnitBtn').click(function (e) {
    var $form;
    e.preventDefault();
    $form = $("#addunits_form");
    let code = $("#code").val();
    //实际交货数量
    if (code == '' || code == null) {
      noty({
        text: '请输入物料编码!',
        type: 'error'
      });
      return;
    }

    $("#unit_code").val(code);
    return $form.submit();

  });

  // 切换供应商, 自行查询目的地
  $("#outCooperator").change(function () {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.get('/Cooperators/findById', {
        id: id
      }, function (r) {
        //目的地赋值
        $("#whouse").val(r.address);
        LoadMask.unmask();
      });
    }
  });

  //解除js处理
  $("#delunit_form_submit").click(function (e) {
    e.preventDefault();

    let num = $("input[name='pids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要解除的出货单元!',
        type: 'error'
      });
    } else {
      $('#bulkpost').attr('action', $(this).data('url')).submit();
    }
  });

  // 切换供应商, 自行查询目的地
  $("#receipt").change(function () {
    let val = $(this).val();
    if (val == 'FACTORY') {
      //工厂代收
      $("#receiveTr").css('display', '');
      $('#outCooperator').removeAttr("disabled");
      $("#whouse").val("");
    } else if (val == 'WAREHOUSE') {
      //仓库自收
      $("#receiveTr").css('display', 'none');
      $('#outCooperator').attr("disabled", true);
      $("#whouse").val("深圳市光明新区玉律村第七工业区汉海达科技创新园1栋A区6楼");

    }

  });

});

