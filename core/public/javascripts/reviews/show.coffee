$ ->
  reviewId = -> $('#reviewId').html().trim()

  $('#try_order').click (e) ->
    mask = $('#container')
    mask.mask('尝试计算中, 如果计算成功, 刷新后则有 OrderId...')
    $.post('/reviews/tryOrder', rid: reviewId(),
      (r) ->
        if r.flag is false
          alert('没有找到 Order...')
        else
          alert('找到 Order 啦! 3s 后刷新')
          setTimeout(
            () ->
              window.location.reload()
            , 2000
          )
        mask.unmask()
    )

  # 取消某一个 Ticket 的原因
  reasonToUnTagReason = (span) ->
    o = $(span)
    mask = $('#reason_div')
    mask.mask('取消原因中...')
    $.post('/reviews/unTagReason', {reason: o.html(), reviewId: reviewId()},
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
    $.post('/reviews/tagReason', {reason: o.html(), reviewId: reviewId()},
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

  $('#take_it').click (e) ->
    mask = $('#container')
    mask.mask('处理中...')
    $.post('/reviews/iTakeIt', tid: @getAttribute('tid'),
      (r) ->
        if r.flag is false
          alert(r.message)
          mask.unmask()
        else
          window.location.reload()
    )
    e.preventDefault()

  $('#close_btn').click (e) ->
    return false if !confirm('确认要关闭这个 Ticket?')
    mask = $('#close_div')
    mask.mask("关闭中...")
    $.post('/reviews/close', {tid: @getAttribute('tid'), reason: $('#close_reason').val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert(99)
        mask.unmask()
    )
    e.preventDefault()

  window.$ui.init()
