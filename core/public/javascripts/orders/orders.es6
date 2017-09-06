$(() => {

  $('#funcs').on('click', 'button:contains(重新抓取费用)', function () {
    LoadMask.mask();
    $.post($(this).data("url"), function (r) {
      let type = r.flag ? "success" : "error";
      noty({
        text: r.message,
        type: type,
        timeout: 3000
      });
      LoadMask.unmask();
    });
  });

  $("#downExcel").click(function (e) {
    e.preventDefault()
    let from = new Date($("#p_from").val());
    let to = new Date($("#p_to").val());
    if (((to - from) / 1000 / 60 / 60 / 24) > 31) {
      alert("暂时只能导出一个月之内的数据！");
    }
    else {
      let $form = $("#search_form");
      window.open('/Excels/orderReports?' + $form.serialize(), "_blank");
    }
  });

  $('#downSaleFeeExcel').click(function (e) {
    e.preventDefault()
    let from = new Date($("#p_from").val());
    let to = new Date($("#p_to").val());
    if ($("selec[name='p.category']").val()) {
      if (((to - from) / 1000 / 60 / 60 / 24) > 31) {
        alert("暂时只能导出一个月之内的数据！");
      } else {
        let $form = $("#search_form")
        window.open('/Excels/orderSaleFeeReports?' + $form.serialize(), "_blank");
      }
    } else {
      alert("请先输入品线");
    }
  });

});