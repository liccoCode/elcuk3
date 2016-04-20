$ ->
  $('#check_div').on("click", "#reset_edit", (r) ->
    return unless confirm('确认重新编辑?')
    LoadMask.mask()
    $.ajax($(@).data('href'))
      .done((r) ->
        type = if r.flag
          'success'
        else
          'error'
        noty({text: r.message, type: type})
        LoadMask.unmask()
      )
  )

  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#check_table").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )