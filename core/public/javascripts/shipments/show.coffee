$ ->
  $('#ship_comment').click (e) ->
    e.preventDefault()
    o = $(@)
    mask = $('container')
    mask.mask('更新 Comment')
    $.post('/shipments/update', {id: $("input[name=ship\\.id]").val(), cmt: $("input[name=ship\\.memo]").val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('更新成功.')
        mask.unmask()
    )
