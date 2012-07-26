$ ->
  $('#try_order').click (e) ->
    mask = $('#container')
    mask.mask('尝试计算中, 如果计算成功, 刷新后则有 OrderId...')
    reviewId = $("#reviewId").html().trim()
    $.post('/reviews/tryOrder', rid: reviewId,
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

  window.$ui.init()
