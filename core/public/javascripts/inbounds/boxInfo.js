$(() => {

  $("input[name$='mainBox.num']").change(function () {
    let total = $(this).parent().prev().text();
    let num = $(this).val();
    $(this).parent().parent().find("input[name$='mainBox.boxNum']").val(parseInt(total / num));
    $(this).parent().parent().find("input[name$='lastBox.boxNum']").val(total % num != 0 ? 1 : 0);
    $(this).parent().parent().find("input[name$='lastBox.num']").val(total % num);
  });

  $("#box_info_table").change(function () {
    $("input[name$='mainBox.num']").each(function () {
      let $tr = $(this).parent("td").parent("tr");
      let realTotalQty = $(this).data("real");
      let totalBoxNum = $(this).val() * $tr.find("input[name$='mainBox.boxNum']").val();
      let totalLastBoxNum = $tr.find("input[name$='lastBox.boxNum']").val() * $tr.find("[name$='lastBox.num']").val()

      if (totalBoxNum + totalLastBoxNum > realTotalQty) {
        $tr.next().find("td").text("输入的总数为" + (totalBoxNum + totalLastBoxNum) + ",大于收货数量" + realTotalQty);
        $tr.next().show();
      } else {
        $tr.next().hide();
      }
    });
  });

});