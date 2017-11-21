$(() => {

  $("button[name='applyBtn']").click(function (e) {
    e.preventDefault();
    if ($("input[name='unitids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的单元数据',
        type: 'error'
      });
      return false;
    }

    let ids = [];
    $("input[name='unitids']:checked").each(function () {
      ids.push($(this).val());
    });
    LoadMask.mask();
    $.post($(this).data("url"), {unitIds: ids}, function (r) {
      LoadMask.unmask();
      if (r.flag) {
        noty({
          text: r.message,
          type: 'success'
        });
        setTimeout("window.location.reload()",1500);
      } else {
        noty({
          text: r.message,
          type: 'error'
        });
      }
    });
  });

  let switchInput = $("input[name='my-checkbox']").bootstrapSwitch();

  $("input[name='my-checkbox']").on('switchChange.bootstrapSwitch', function (event, state) {
    let feesize = $(this).attr('feesize')
    let url = $(this).attr('url')
    if(feesize > 0) {
      noty({
        text: '存在费用明细,不可以更改收款状态!',
        type: 'error'
      });
      setTimeout("window.location.reload()",1500);
    }else{
      $('#edit_pay_form').attr('action', url)
      $("#edit_pay").modal('show')
    }
  });

  $("#close_modal").click(function (e) {
    e.preventDefault();
    $("#edit_pay").modal('hide');
    window.location.reload();
  })

});
