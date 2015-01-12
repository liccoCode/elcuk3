$ ->
  $('#dealprocess').click ->
    # 触发 Ajax 事件
  $div = $("#dealprocessinfo")
  $div.load("/Activitis/indexhistory", (r)->
  )

  $('#runprocess').click ->
    # 触发 Ajax 事件
  $div = $("#runprocessinfo")
  $div.load("/Activitis/indexrun", (r)->
  )

  $("#dealprocess").on("click", "#dealprocessSearch", () ->
    # 触发 Ajax 事件
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    $div = $("#dealprocessinfo")
    $div.load("/Activitis/indexhistory/#{id}", (r)->
    )
  )

  $("#runprocess").on("click", "#runprocessSearch", () ->
    # 触发 Ajax 事件
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    $div = $("#runprocessinfo")
    $div.load("/Activitis/indexrun/#{id}", (r)->
    )
  )