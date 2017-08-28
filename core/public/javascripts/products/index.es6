$(() => {

  $("#copyBtn").click(function () {
    let sku = $(this).data('sku');
    $("#target_choseid").val(sku);
    $('#copy_modal').modal('show');
  });



});