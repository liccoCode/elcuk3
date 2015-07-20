$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#reports").dataTable(
      sDom: "<'row-fluid'<'span10'l><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: false
      aaSorting: [
        [0, "desc"]
      ]
    )

  $("#reports_form").on("click", "#repeatcalculate", (r) ->
    $.ajax($(@).data('url'), {type: 'POST', data: "" })
    .done((r) ->
        msg = if r.flag is true
          "#{r.message}"
        else
          r.message
        alert msg
      )
    .fail((r) ->
        alert r.responseText
      )
  )
