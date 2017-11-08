window.$dataui = {
  dateinput: function () {
    $("input[role='date']").datepicker({
      format: 'yyyy-mm-dd',
      autoclose: true
    });
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
    trigger: 'hover'
  });

});