$ ->
  $('.close_btn').click (e) ->
    return false if !confirm('确认要关闭这个 Ticket?')
    rid = @getAttribute('rid')
    mask = $("#tab_content_#{rid}")
    mask.mask("关闭中...")
    $.post('/tickets/close', {tid: @getAttribute('tid'), reason: $("#close_reason_#{rid}").val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          toggleTr = $("#toggle_#{rid}")
          toggleTr.prev().remove()
          toggleTr.remove()
        mask.unmask()
    )
    e.preventDefault()

  $('#ostickt_sync').click (e) ->
    mask = $('#container')
    mask.mask('更新中 5s 后自动刷新...')
    $.post('/reviews/syncAll',
      (r) ->
        setTimeout(->
            window.location.reload()
          , 5000)
    )
    e.preventDefault()
