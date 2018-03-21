$(() => {

  $("#addFeeBtn").click(function () {
    $("#channel_fee_modal").modal("show");
  });

  $("#updateUnit_form").on("click", "#addWeight", function () {
    let index = $("input[name$='weightEnd']").length;
    let html = _.template($("#copy").text())({"num": index});
    $(this).parent().parent("div").after(html);
    $(this).remove();
  });

  $("#data-table td").attr("style", "vertical-align:middle");

  $("input[name='updateFee']").click(function () {
    $("#update_fee_modal").modal("show");
    $("#update_div").load($(this).data("url"));
  });

});