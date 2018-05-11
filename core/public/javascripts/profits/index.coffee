$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    $("#profit").dataTable(
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

  # Form 搜索功能
  $("#profits_form").on("click", ".btn:contains(Excel)", (e) ->
    e.preventDefault()
    $form = $('#profits_form')
    window.open('/Excels/profit?' + $form.serialize(), "_blank")
  )

  $("#profits_form").on("click", ".btn:contains(Inventory)", (e) ->
    e.preventDefault()
    $form = $('#profits_form')
    $.post('/Profits/inventory', $form.serialize(),
      (r) ->
        alert(r.message)
    )
  )