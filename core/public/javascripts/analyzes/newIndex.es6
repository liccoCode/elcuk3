$(() => {

  $("#sortTable").dataTable({
    "sDom": "<'row-fluid'<'col-sm-3'l><'col-sm-9'f>r>t<'row-fluid'<'col-sm-6'i><'col-sm-6'p>>",
    "sPaginationType": "full_numbers",
    "iDisplayLength": 50,
    "aaSorting": [[10, "desc"]],
    "columnDefs": [{
      "width": "28px",
      "targets": [2]
    }, {
      "width": "38px",
      "targets": [3]
    }]
  });

  $("#below_tabContent").on("ajaxFresh", "#sid,#sku", function () {
    let $data_table = $("#below_tabContent");
    let $div = $(this);
    $("#postType").val($div.attr("id"));
    LoadMask.mask($data_table);
    $div.load("/Analyzes/analyzes", $('#click_param').serialize(), function (r) {
      $div.find('table').dataTable({
        "sDom": "<'row-fluid'<'col-sm-3'l><'col-sm-9'f>r>t<'row-fluid'<'col-sm-6'i><'col-sm-6'p>>",
        "sPaginationType": "full_numbers",
        "iDisplayLength": 50,
        "aaSorting": [[17, "desc"]],
        "scrollX": true,
        "columnDefs": paramWidth($div.attr("id"))
      });
      LoadMask.unmask($data_table)
    });
  });

  $("#click_param").on("change", "[name='p.market']", function () {
    ajaxFreshActiveTableTab();
  }).on("click", ".btn:contains(Excel)", function (e) {
    e.preventDefault();
    window.location.href = '/Excels/analyzes?' + $('#click_param').serialize();
  }).on("click", ".btn:contains(搜索)", function (e) {
    e.preventDefault();
    ajaxFreshActiveTableTab()
  });

  function ajaxFreshActiveTableTab () {
    let type = $("#below_tab li.active a").attr("href");
    $(type).trigger("ajaxFresh");
  }
  
});