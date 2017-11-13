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
  })

});