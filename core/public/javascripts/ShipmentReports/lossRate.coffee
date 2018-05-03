$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#lossrate").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: false
      aaSorting: [[0, "desc"]]
    )

    oTable = $("#shipItemTable").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: true
      sPaginationType: "full_numbers"
      aaSorting: [[0, "desc"]]
      iDisplayLength: 25
      aoColumnDefs: [{
        sDefaultContent: '',
        aTargets: ['_all']
      }]
    )

    $('#runprocess [class=span9]').append('<b style="color:#0e90d2">赔偿明细</b>')

  # Form 搜索功能
  $("#exceldown").click((e)->
    e.preventDefault()
    $form = $('#search_form')
    window.open('/Excels/lossRateReport?' + $form.serialize(), "_blank")
  )

$ ->
  $(document).on('click', '#reflush_lossrate', (e) ->
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    $form.submit()
  )

