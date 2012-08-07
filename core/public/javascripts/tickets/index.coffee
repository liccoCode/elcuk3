$ ->
  $('.close_btn').click (e) ->
    return false if !confirm('确认要关闭这个 Ticket?')
    mask = $("#tab_content_#{@getAttribute('rid')}")
    mask.mask("关闭中...")
    $.post('/tickets/close', {tid: @getAttribute('tid'), reason: $("#close_reason_#{@getAttribute('rid')}").val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert(99)
        mask.unmask()
    )
    e.preventDefault()

  $('#ostickt_sync').click (e) ->
    $.post('/tickets/syncAll',
      (r) ->
        alert(r.message)
    )
    e.preventDefault()
