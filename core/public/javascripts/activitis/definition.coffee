$ ->
  $("button.submit").click ->
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    params =
      menucode: $(self).parents('tr').find("td:eq(1) input").val(),
      menuname: $(self).parents('tr').find("td:eq(2) input").val(),
      processname: $(self).parents('tr').find("td:eq(3) input").val(),
      processxml: $(self).parents('tr').find("td:eq(4) input").val(),
      processid: $(self).parents('tr').find("td:eq(5) input").val()

    $.post("/activitis/#{id}/update", params, (r) ->
      try
        if r.flag
          Notify.ok("更新成功", r.message)
        else
          Notify.alarm("更新失败", r.message)
      finally
        LoadMask.unmask()
    )


  $("button.delete").click ->
    return unless confirm('确认删除这个流程吗?!')
    self = @
    id = $(self).parents('tr').find("td:eq(0)").text()
    LoadMask.mask()
    $.post("/activitis/#{id}/delete", (r) ->
      try
        if r.flag
          $(self).parents('tr').remove()
          Notify.ok("删除 流程", r.message)
        else
          Notify.alarm("删除 流程 失败", r.message)
      finally
        LoadMask.unmask()
    )


