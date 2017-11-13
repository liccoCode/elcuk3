$(() => {

  $("#sortTable").dataTable({
    "sDom": "<'row-fluid'<'col-sm-3'l><'col-sm-9'f>r>t<'row-fluid'<'col-sm-6'i><'col-sm-6'p>>",
    "sPaginationType": "full_numbers",
    "iDisplayLength": 50,
    "aaSorting": [[9, "desc"]],
    "columnDefs": [{
      "width": "28px",
      "targets": [2]
    }]
  });

});