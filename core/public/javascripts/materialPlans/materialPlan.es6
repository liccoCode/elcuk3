$(() => {

  $("button[name='applyBtn']").click(function (e) {
    e.preventDefault();
    if ($("input[name='unitids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的出货单元',
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
        window.location.reload();
      } else {
        noty({
          text: r.message,
          type: 'error'
        });
      }
    });
  });

});
