$(() => {

  $("input[name='unit.mainBox.num']").change(function () {
    let total = $("#totalQty").val();
    let num = $(this).val();
    $("input[name='unit.mainBox.boxNum']").val(parseInt(total / num));
    $("input[name='unit.lastBox.boxNum']").val(total % num != 0 ? 1 : 0);
    $("input[name='unit.lastBox.num']").val(total % num);
  });

  $("#box_info_table").change(function () {
    let totalBoxNum = $("input[name='unit.mainBox.boxNum']").val() * $("input[name='unit.mainBox.num']").val();
    let totalLastBoxNum = $("input[name='unit.lastBox.boxNum']").val() * $("input[name='unit.lastBox.num']").val();

    if (totalBoxNum + totalLastBoxNum > $("#totalQty").val()) {
      $("#noticeTr").find("td").text("输入的总数为" + (totalBoxNum + totalLastBoxNum) + ",大于收货数量" + $("#totalQty").val());
      $("#noticeTr").show();
    } else {
      $("#noticeTr").hide();
    }

  });

});