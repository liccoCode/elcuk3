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