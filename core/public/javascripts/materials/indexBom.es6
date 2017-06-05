$(() => {

  $("#addBom").click(function (e) {
    $("#bom_modal").modal('show')
  });

  $("#submitCreateBtn").click(function () {
    $("#create_form").submit();
  });

});