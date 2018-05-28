window.$dataui = {
  dateinput: function () {
    $("input[role='date']").datepicker({
      todayHighlight: true,
      format: 'yyyy-mm-dd',
      autoclose: true
    });
    $("input[role='date']").attr("autocomplete","off");
  },

  selectize: function () {
    $("select").each(function () {
      let select = $(this);
      if (!select.hasClass('selectize')) {
        return;
      }
      select.selectize();
    });
  }
};

$(() => {
  window.$dataui.dateinput();
  window.$dataui.selectize();
  $("table").addClass("table-hover");

  $("[data-toggle='popover']").popover({
    html: true,
    trigger: 'hover',
    container: 'body'
  });

});