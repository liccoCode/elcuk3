$(() => {

  $("#date_from").datepicker({
    format: 'yyyy-mm-dd',
    autoclose: true
  });

  $("#date_to").datepicker({
    format: 'yyyy-mm-dd',
    autoclose: true
  });

  $("input[role='date']").datepicker({
    format: 'yyyy-mm-dd',
    autoclose: true
  });

  $("select").each(function () {
    let select = $(this);
    if (!select.hasClass('selectize')) {
      return;
    }
    select.selectize();
  });

  $("table").addClass("table-hover");

});