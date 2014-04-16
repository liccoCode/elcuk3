$.extend $.fn.dataTableExt.oStdClasses,
  sWrapper: "dataTables_wrapper form-inline"


$(document).ready ->
  oTable = $("#example").dataTable(
    sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
    bPaginate: false
  )