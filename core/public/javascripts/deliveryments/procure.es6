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

});
