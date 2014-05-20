$ ->
  $('#dealprocess').click ->
    # 触发 Ajax 事件
  LoadMask.mask()
  $div = $("#dealprocessinfo")
  $div.load("/Activitis/indexhistory", (r)->
    LoadMask.unmask()
  )

  $("#dealprocess").on("click", "#dealprocessSearch", () ->
    # 触发 Ajax 事件
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    $div = $("#dealprocessinfo")
    $div.load("/Activitis/indexhistory/#{id}", (r)->
      LoadMask.unmask()
    )
  )