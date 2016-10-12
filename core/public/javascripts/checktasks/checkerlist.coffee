$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#check_table").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )

  $(document).ready ->
    oTable = $("#checked_table").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )

  $(document).ready ->
    oTable = $("#checkRepeat").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )

  $(document).on("change", "select[name='check.qcType'], select[name='cr.qcType']", (r) ->
    mask = $("#check_table")
    $select = $(@)
    mask.mask()
    if $select.val() != ""
      id = $select.parent('td')[0].previousElementSibling.previousElementSibling.innerHTML
      $.post('/checktasks/updateqctype', {qcType: $select.val(), id: id })
    mask.unmask()
  )