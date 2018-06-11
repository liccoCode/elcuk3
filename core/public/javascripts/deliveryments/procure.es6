$(() => {

  $("button[name='applyBtn']").click(function (e) {
    e.preventDefault();
    let ids = [];
    $("input[name='unitids']:checked").each(function () {
      ids.push($(this).val());
    });
    LoadMask.mask();
    $.post($(this).data("url"), {unitIds: ids}, function (r) {
      LoadMask.unmask();
      if (r.flag) {
        alert(r.message);
        window.location.reload();
      } else {
        alert(r.message);
      }
    });
  });

  let switch_pay = $("input[name='switch_pay']").bootstrapSwitch();
  switch_pay.on('switchChange.bootstrapSwitch', function () {
    let feesize = $(this).attr('feesize')
    let url = $(this).attr('url')
    if (feesize > 0) {
      noty({
        text: '存在费用明细,不可以更改收款状态!',
        type: 'error'
      });
      setTimeout("window.location.reload()", 1500);
    } else {
      $('#edit_pay_form').attr('action', url);
      $("#edit_pay").modal('show');
    }
  });

});
