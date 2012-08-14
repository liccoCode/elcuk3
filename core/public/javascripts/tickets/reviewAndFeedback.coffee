$ ->
  # 使用 Ticket Tag 的 html 代码例子
  #<div class="row-fluid" id="reason_div">
  #  <div class="span5">
  #  <!-- 结合 tag 使用的 -->
  #    <input type="hidden" id="ticketId" value="${review.ticket.id}">
  #    <span class="reason badge badge-important">${ra.name()}</span>
  #   </div>
  #  <div class="span7 input-append">
  #    <span class="unTagReason badge badge-info">${ura.name()}</span>
  #  </div>
  #</div>
  ticketId = -> $('#ticketId').val().trim()

  # 取消某一个 Ticket 的原因
  reasonToUnTagReason = (span) ->
    o = $(span)
    mask = $('#reason_div')
    mask.mask('取消原因中...')
    $.post('/tickets/unTagReason', {reason: o.html(), ticketId: ticketId()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          o.removeClass('reason')
          .removeClass('badge-important')
          .addClass('unTagReason')
          .addClass('badge-info')
          .unbind().click -> unTagReasonToReason(@)
          $("#reason_div div:eq(1)").append(o)
        mask.unmask()
    )

  # 为某一个 Ticket 添加原因
  unTagReasonToReason = (span) ->
    o = $(span)
    mask = $('#reason_div')
    mask.mask('添加原因中...')
    $.post('/tickets/tagReason', {reason: o.html(), ticketId: ticketId()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          o.removeClass('unTagReason')
          .removeClass('badge-info')
          .addClass('reason')
          .addClass('badge-important')
          .unbind().click -> reasonToUnTagReason(@)
          $("#reason_div div:eq(0)").append(o)
        mask.unmask()
    )


  # 为此 Listing 添加 Tag
  $('span.reason').css('cursor', 'pointer').click -> reasonToUnTagReason(@)
  $('span.unTagReason').css('cursor', 'pointer').click -> unTagReasonToReason(@)

  # 关闭按钮
  $('#close_btn').click (e) ->
    return false if !confirm('确认要关闭这个 Ticket?')
    mask = $('#close_div')
    mask.mask("关闭中...")
    $.post('/tickets/close', {tid: ticketId(), reason: $(@).prev().val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert(99)
        mask.unmask()
    )
    e.preventDefault()

  # 负责这个 Ticket
  $('#take_it').click (e) ->
    mask = $('#container')
    mask.mask('处理中...')
    $.post('/tickets/iTakeIt', tid: ticketId(),
      (r) ->
        if r.flag is false
          alert(r.message)
          mask.unmask()
        else
          window.location.reload()
    )
    e.preventDefault()

  ## --------------------- Review And Feedback index 页面使用的 js 代码
  # 添加 comment 功能
  $('#ticket_comment, .ticket_comment').click (e) ->
    mask = $('#container')
    mask.mask('更新中...')
    try
      tid = ticketId()
    catch e
      tid = @getAttribute('tid')
    $.post('/tickets/comment', {tid: tid, comment: $(@).prev().val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        mask.unmask()
    )
    e.preventDefault()


  # 为 index 页面的所有 .close_btn 添加关闭事件
  $('.close_btn').click (e) ->
    return false if !confirm('确认要关闭这个 Ticket?')
    tid = @getAttribute('tid')
    mask = $("#tab_content_#{tid}")
    mask.mask("关闭中...")
    $.post('/tickets/close', {tid: @getAttribute('tid'), reason: $(@).prev().val()},
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

  # 为 index 页面添加 osticket 同步按钮
  $('#ostickt_sync').click (e) ->
    mask = $('#container')
    mask.mask('更新中 5s 后自动刷新...')
    $.post('/tickets/syncAll',
      (r) ->
        setTimeout(
          ->
            window.location.reload()
          , 5000)
    )
    e.preventDefault()
