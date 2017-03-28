$(() => {

  $("#secondPayBtn").click(function (e) {
    e.preventDefault();
    let ids = [];
    $("input[name='unitids']:checked").each(function () {
      ids.push($(this).val());
    });
    alert (ids);
    $.post("/ProcureUnits/batchMediumPay", {unitIds: ids}, function (r) {
      if (r.flag) {
        alert('申请中期请款成功.');
        window.location.reload();
      } else {
        alert(r.message);
      }
    });
  });

});
