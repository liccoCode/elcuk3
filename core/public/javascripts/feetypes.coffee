$ ->
  $("button.submit").click ->
    name = $(@).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    $.post("/feetype/#{name}/update", {memo: $(@).prev().val()}, (r) ->
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


