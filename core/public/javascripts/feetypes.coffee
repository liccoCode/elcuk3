$ ->
  $("button.submit").click ->
    self = @
    name = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    params =
      memo: $(self).prev().val(),
      nickName: $(self).parents('tr').find("td:eq(1) input").val()

    $.post("/feetype/#{name}/update", params, (r) ->
      try
        if r.flag
          Notify.ok("更新成功", r.message)
        else
          Notify.alarm("更新失败", r.message)
      finally
        LoadMask.unmask()
    )


  $("button.delete").click ->
    return unless confirm('确认删除这个费用类型吗?!')
    self = @
    name = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    $.post("/feetype/#{name}/delete", (r) ->
      try
        if r.flag
          $(self).parents('tr').remove()
          Notify.ok("删除 FeeType", r.message)
        else
          Notify.alarm("删除 FeeType 失败", r.message)
      finally
        LoadMask.unmask()
    )


