$ ->
# 绑定翻译按钮
  bindTransBtn = ->
    $('button.trans').click (e) ->
      review = $(@).parents('tr').find('.review').text()
      window.open("http://translate.google.com/?text=#{review}")
      e.preventDefault()

  # 绑定 UP 按钮
  bindUpBtn = ->
    $('button.makeUp').click (e) ->
      reviewId = $(@).parents('tr').attr('id').split('_')[1]
      mask = $('#container')
      mask.mask('点击中...')
      $.post('/amazonReviews/click', {reviewId: reviewId, isUp: true}
        (r) ->
          if r.flag is false
            alert("点击失败. #{r.message}")
          else
            alert("点击成功.")
            $("#after_#{reviewId}").html(JSON.stringify(r._2))
            $("#accleft_#{reviewId}").html(r._1)
          mask.unmask()
      )
      e.preventDefault()

  # 绑定 Down 按钮
  bindDownBtn = ->
    $('button.makeDown').click (e) ->
      reviewId = $(@).parents('tr').attr('id').split('_')[1]
      mask = $('#container')
      mask.mask('点击中...')
      $.post('/amazonReviews/click', {reviewId: reviewId, isUp: false}
        (r) ->
          if r.flag is false
            alert("点击失败. #{r.message}")
          else
            alert("点击成功.")
            $("#after_#{reviewId}").html(JSON.stringify(r._2))
            $("#accleft_#{reviewId}").html(r._1)
          mask.unmask()
      )
      e.preventDefault()


  # 绑定 Table 的事件
  bindTableToggleClick = ->
    $('tr[drop]').css('cursor', 'pointer').click (e) ->
      o = $(@)
      $("#review_#{o.attr('drop')}").toggle('fast')
      e.preventDefault()

  # Ajax 加载 Review 页面
  reviewLoadFun = ->
    mask = $('#container')
    mask.mask('加载中...')
    $('#reviews').load('/amazonReviews/ajaxMagic', $('#search_form :input').fieldSerialize(),
      () ->
      # 如果没有一个元素, 那么则需要重新抓取.
        mask.unmask()
        if $('#reviews tr').size() is 1
          alert('此 Listing 为全新的 Listing, 重新抓取 Listing 中...')
          mask.mask('重新抓取中...')
          $.post('/amazonreviews/reCrawl', $('#search_form :input').fieldSerialize(),
            (r) ->
              mask.unmask()
              if r.flag is false
                alert(r.message)
              else
                $('#recrawl_review').click()
          )
        else
          bindTableToggleClick()
          bindTransBtn()
          bindUpBtn()
          bindDownBtn()
          window.$ui.init()
    )

  # 绑定重新抓取事件
  $('#recrawl_review').click (e) ->
    mask = $('#container')
    mask.mask("重新抓取 Review 信息中...")
    $.get('/amazonReviews/reCrawl', $('#search_form :input').fieldSerialize(), (r) ->
        if r.flag is false
          alert(r.message)
        else
          if r.message is '0'
            alert('此 Listing 暂时无 Review.')
          else
            reviewLoadFun()
        mask.unmask()
    )
    e.preventDefault()

  # Listing 重新抓取
  $('#recrawl_listing').click (e) ->
    mask = $('#container')
    mask.mask('重新抓取 Listing 信息中...')
    $.get('/listings/reCrawl', $('#search_form :input'),
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          $('#search_form button:eq(0)').click()
        mask.unmask()
    )

  # 绑定点击 Like 按钮
  $('#click_like').click (e) ->
    if $('#search_form [name=asin]').val().length isnt 10
      alert('请先输入正确的 ASIN')
      return false
    mask = $('#container')
    mask.mask('点击 Like 中...')
    $.post('/amazonreviews/like', $('#search_form :input').fieldSerialize(),
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert(r._2)
        mask.unmask()
    )
    e.preventDefault()

  # 检查每个 Review 的可点击数
  $("#check_left_clicks").click (e) ->
    o = $(@)
    mask = $('#container')
    params = {}
    for tr, i in $("tr[drop]")
      params["rvIds[#{i}]"] = tr.getAttribute('drop')
    mask.mask('计算中...')
    $.post('/AmazonReviews/checkLeftClicks', params,
      (r) ->
        if r.flag is false
          alert(r.message)
          o.button('reset')
        else
          o.button('loading')
          for t2 in r
            $("#accleft_#{t2._1}").html(t2._2)
        mask.unmask()
    )
    e.preventDefault()

  $('#search_form button:eq(0)').click (e) ->
    o = $(@).prev()
    o.val(o.val().toUpperCase())
    #B007LE0UT4
    return false if o.val().length isnt 10
    reviewLoadFun()
    e.preventDefault()
