$ ->
  $('#profits_form').ajaxForm({
    dataType: 'json',
    success: (r) ->
      alert(r.message)
  })

  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    $("#profit").dataTable(
      bPaginate: false
      aaSorting: [
        [0, "desc"]
      ]
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