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
    $.post('/reviews/syncAll',
      (r) ->
        alert(r.message)
    )
    e.preventDefault()
