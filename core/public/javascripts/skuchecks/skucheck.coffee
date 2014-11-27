$ ->
  $("button.submit").click ->
    self = @
    id = $(self).parents('tr').find("input").val()
    LoadMask.mask()
    params =
      checkRequire: $(self).parents('tr').find("td:eq(1) textarea").val(),
      checkMethod: $(self).parents('tr').find("td:eq(2) textarea").val()

    $.post("/skuchecks/#{id}/update", params, (r) ->
      try
        if r.flag
          alert("更新成功: #{r.message}")
        else
          alert("更新失败: #{r.message}")
      finally
        LoadMask.unmask()
    )


  $("button.delete").click ->
    return unless confirm('确认删除?')
    self = @
    id = $(self).parents('tr').find("input").val()
    LoadMask.mask()
    $.post("/skuchecks/#{id}/delete", (r) ->
      try
        type = if r.flag
          $(self).parents('tr').remove()
          'success'
        else
        'error'
        noty({text: r.message, type: type})
      finally
        LoadMask.unmask()
    )


