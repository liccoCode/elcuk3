$(() => {


  $("a[name='improtPayment']").click(function (e) {
    e.preventDefault();
    $("#shipment_modal").modal('show');
    e.preventDefault();
  });

  $("#submitUpdateBtn").click(function () {
    $("#payment_form").submit();
  });

});