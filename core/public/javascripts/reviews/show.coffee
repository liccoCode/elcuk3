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

  $('#review_sync').click (e) ->
    mask = $('#container')
    mask.mask('同步中...')
    $.post('/reviews/sync', rid: reviewId(),
      (r) ->
        if r.flag is true
          alert('同步成功, 3s 后自动刷新')
        else
          alert('同步失败, 3s 后自动刷新')
        setTimeout(
          ->
            window.location.reload()
          , 2000
        )
    )
    e.preventDefault()

  window.$ui.init()
