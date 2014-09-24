$.extend $.fn.dataTableExt.oStdClasses,
  sWrapper: "dataTables_wrapper form-inline"


$(document).ready ->
  oTable = $("#trafficrate").dataTable(
    sDom: "<'row-fluid'<'span9'><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
    sPaginationType: "full_numbers"
    iDisplayLength: 50
    aaSorting: [
      [0, "desc"]
    ]
    aoColumnDefs: [
      { sDefaultContent: '', aTargets: [ '_all' ] }
    ]
  )

$ ->
  $(document).on('click', '#reflush_trafficrate, #download_excel', (e) ->
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    $form.submit()
  )
