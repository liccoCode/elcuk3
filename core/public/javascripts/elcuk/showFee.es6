$(() => {

  $("#addFeeBtn").click(function () {
    $("#channel_fee_modal").modal("show");
  });

  $("#updateUnit_form,#update_fee_modal").on("click", "#addWeight", function () {
    let index = $("input[name$='weightEnd']").length;
    let html = _.template($("#copy").text())({"num": index});
    $(this).parent().parent("div").after(html);
    $(this).remove();
  }).on("click", "a[name='deleteWeight']", function () {
    let refresh = $(this).data("refresh");
    $.post($(this).data("url"), {}, function (r) {
      if (r.flag) {
        $("#update_div").html("");

        $("#update_div").load(refresh);
      }
    });
  });

  $("#data-table td").attr("style", "vertical-align:middle");

  $("input[name='updateFee']").click(function () {
    $("#update_fee_modal").modal("show");
    $("#update_div").load($(this).data("url"));
  });

});