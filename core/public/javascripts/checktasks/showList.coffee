$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
      sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#show_list_table").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )