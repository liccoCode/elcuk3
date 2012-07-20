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
      $.post('/amazonReviews/makeUp', reviewId: reviewId,
      (r) ->
        if r.flag is false
          alert("点击失败. #{r.message}")
        else
          alert("点击成功. #{JSON.stringify(r._1)}")
          $("#after_#{reviewId}").html(r._2)
      )
      e.preventDefault()

  # 绑定 Down 按钮
  bindDownBtn = ->
    $('button.makeDown').click (e) ->
      reviewId = $(@).parents('tr').attr('id').split('_')[1]
      $.post('/amazonReviews/makeDown', reviewId: reviewId,
      (r) ->
        if r.flag is false
          alert("点击失败. #{r.message}")
        else
          alert("点击成功. #{JSON.stringify(r._1)}")
          $("#after_#{reviewId}").html(r._2)
      )
      e.preventDefault()


  # 绑定 Table 的事件
  bindTableToggleClick = ->
    $('tr[drop]').css('cursor', 'pointer').click (e) ->
      o = $(@)
      $("#review_#{o.attr('drop')}").toggle('fast')
      e.preventDefault()


  $('#search_form [name=asin]').keyup (e) ->
    o = $(@)
    if e.keyCode isnt 13
      o.val(o.val().toUpperCase())
      return false
    #B007LE0UT4
    return false if o.val().length isnt 10
    reviews = $('#reviews')
    reviews.mask('加载中...')
    reviews.load('/amazonReviews/ajaxMagic', $('#search_form :input').fieldSerialize(),
    () ->
      # 如果没有一个元素, 那么则需要重新抓取.
      if $('#reviews tr').size() is 1
        alert('需要重新抓取.')
      else
        bindTableToggleClick()
        bindTransBtn()
        bindUpBtn()
        bindDownBtn()
      reviews.unmask()
    )
    e.preventDefault()
