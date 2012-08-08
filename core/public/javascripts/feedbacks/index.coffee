$ ->
  $('.close_btn').click (e) ->
  #close_reason_
    return false if !confirm('确认要关闭这个 Ticket?')
    tid = @getAttribute('tid')
    mask = $("#tab_content_#{tid}")
    mask.mask('关闭中...')
    $.post('/tickets/close', {tid: tid, reason: $("#close_reason_#{tid}")},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          toggleTr = $("#toggle_#{tid}")
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