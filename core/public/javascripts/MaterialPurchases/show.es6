$(() => {

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
      $("#unit_planDeliveryDate").val(r.planDeliveryDate);
      $("#bom_modal").modal('show')
    });

  });

  //提交修改明细功能
  $("#submitUpdateBtn").click(function () {
    $("#updateUnit_form").submit();
  });

  //提交确认功能
  $("#confirmBtn").click(function () {
    $("#confirmForm").submit();
  });

  //提交取消功能
  $("#cancelBtn").click(function () {
    $("#cancelForm").submit();
  });

});