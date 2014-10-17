$ ->
  $('.profits_form').ajaxForm({
  dataType: 'json',
  success: (r) ->
    alert(r.message)
  })


  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"


  $(document).ready ->
    oTable = $("#profit").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: false
      aaSorting: [[0, "desc"]]
    )


# Form 搜索功能
  $(".search_form").on("click",".btn:contains(Excel)",(e) ->
    e.preventDefault()
    $form = $('#profits_form')
    window.open('/Excels/profit?'+$form.serialize(),"_blank")
  )