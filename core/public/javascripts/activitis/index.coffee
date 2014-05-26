$ ->
  $('#dealprocess').click ->
    # 触发 Ajax 事件
  LoadMask.mask()
  $div = $("#dealprocessinfo")
  $div.load("/Activitis/indexhistory", (r)->
    LoadMask.unmask()
  )

  $('#runprocess').click ->
    # 触发 Ajax 事件
  LoadMask.mask()
  $div = $("#runprocessinfo")
  $div.load("/Activitis/indexrun", (r)->
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

  $("#runprocess").on("click", "#runprocessSearch", () ->
    # 触发 Ajax 事件
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    $div = $("#runprocessinfo")
    $div.load("/Activitis/indexrun/#{id}", (r)->
      LoadMask.unmask()
    )
  )