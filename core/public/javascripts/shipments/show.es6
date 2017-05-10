$(() => {

  $("#b2b_select").change(function () {
    let id = $(this).val();
    $.get($(this).data("url"), {id: id}, function (r) {
      $("input[name='ship.receiverPhone']").val(r['receiverPhone']);
      $("input[name='ship.receiver']").val(r['receiver']);
      $("input[name='ship.countryCode']").val(r['countryCode']);
      $("input[name='ship.postalCode']").val(r['postalCode']);
      $("input[name='ship.city']").val(r['city']);
      $("input[name='ship.address']").val(r['address']);
    });
  });

});