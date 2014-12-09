$ ->

  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"


  $(document).ready ->
    oTable = $("#lossrate").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: false
      aaSorting: [[0, "desc"]]
    )


# Form 搜索功能
  $(".search_form").on("click",".btn:contains(Excel)",(e) ->
    e.preventDefault()
    $form = $('#search_form')
    window.open('/Excels/lossRateReport?'+$form.serialize(),"_blank")
  )

$ ->
  $(document).on('click', '#reflush_lossrate', (e) ->
    alert 'aaaaaaaaa'
    $btn = $(@)
    alert $btn.data('uri')
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    alert $form.attr('action')
    $form.submit()
  )