$ ->
# 对参数进行 trim 处理
  $('#copItem_sku').parents('form').find(':input').change (e) ->
    o = $(@)
    o.val(o.val().trim())
    e.preventDefault()


  $("#addBtn").click(->
    num = $("#form_table table").length
    params =
      num: num
    html = _.template($('#add_template').html())(params)
    $("#addTd").append(html)
  )

  $("#delBtn").click(->
    num = $("#form_table table").length
    if num > 1
      $("#add_div_" + num).remove()
      $("#add_table_" + num).remove()
    else
      noty({text: "最后一个方案无法删除！", type: 'error'})
  )




