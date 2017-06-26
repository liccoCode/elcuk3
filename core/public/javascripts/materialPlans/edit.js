/**
 * Created by licco on 2016/11/17.
 */

$(() => {
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
    $.get('/MaterialPlans/confirmValidate', {
      id: $('#deliverymentId').val()
    }, function (r) {
      if (r.flag) {
        if (confirm(r.message)) {
          $("#confirm_form").submit();
        }
      } else {
        $("#confirm_form").submit();
      }
    })
  });

  //快速添加物料编码js处理
  $('#addPlanUnitBtn').click(function (e) {
    e.stopPropagation();
    let code = $("#code").val() ;
    //实际交货数量
    if (code == '' || code == null) {
      noty({
        text: '请输入物料编码!',
        type: 'error'
      });
      return;
    }

    $("#unit_code").val(code);
    $("#addunits_form").submit();

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


});

