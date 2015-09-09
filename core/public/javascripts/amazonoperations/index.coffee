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
      $.post('/amazonOperations/click', {reviewId: reviewId, isUp: true}
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
      $.post('/amazonOperations/click', {reviewId: reviewId, isUp: false}
        (r) ->
          if r.flag is false
            alert("点击失败. #{r.message}")
          else
            alert("点击成功.")
            $("#after_#{reviewId}").html(JSON.stringify(r._2))
            #这个 - 1 是因为需要除开已经点击的这一次
            $("#accleft_#{reviewId}").html(r._1 - 1)
          mask.unmask()
      )
      e.preventDefault()


  # Ajax 加载 Review 页面
  reviewLoadFun = ->
    mask = $('#container')
    mask.mask('加载中...')
    $('#reviews').load('/amazonOperations/ajaxMagic', $('#search_form :input').fieldSerialize(), () ->
      # 如果没有一个元素, 那么则需要重新抓取.
      mask.unmask()
      if $('#reviews tr').size() is 1
        alert('此 Listing 为全新的 Listing, 重新抓取 Listing 中...')
        mask.mask('重新抓取中...')
        $.post('/amazonOperations/reCrawl', $('#search_form :input').fieldSerialize(), (r) ->
            mask.unmask()
            if r.flag is false
              alert(r.message)
            else
              $('#recrawl_review').click()
        )
      else
        toggle_init()
        bindTransBtn()
        bindUpBtn()
        bindDownBtn()
        $('#check_left_clicks').button('reset')
        window.$ui.init()
    )

  # 绑定重新抓取事件
  $('#recrawl_review').click (e) ->
    mask = $('#container')
    mask.mask("重新抓取 Review 信息中...")
    $.get('/amazonOperations/reCrawl', $('#search_form :input').fieldSerialize(), (r) ->
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

  # 检查每个 Review 的可点击数
  $("#check_left_clicks").click (e) ->
    o = $(@)
    mask = $('#container')
    params = {}
    for tr, i in $("tr[drop]")
      params["rvIds[#{i}]"] = tr.getAttribute('drop')
    mask.mask('计算中...')
    $.post('/AmazonOperations/checkLeftClicks', params,
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

  $('#load_review_btn').click (e) ->
    e.preventDefault()
    loadAsin = $('#load_asin').val(-> @value.toUpperCase())
    #B007LE0UT4
    if loadAsin.val().length isnt 10 && !$("#load_sku").val()
      return false
    href = $('#tabs li[class=active] a').attr('href')
    if href is '#review_table'
      loadReviewTable()
    else if href is '#wish_list'
      loadWishList()
    else
      reviewLoadFun()



  # 解析 hash
  activeAmazonReview = ->
    args = location.hash.substr(1).split('/')
    return false if args.length < 2
    market = args[1].split('.')[1..-1].join('.')
    if(market.indexOf('de') > -1)
      $('#search_form [name=m] [value=ade]').prop('selected', true)
    else if(market.indexOf('uk') > -1)
      $('#search_form [name=m] [value=auk]').prop('selected', true)
    else if(market.indexOf('fr') > -1)
      $('#search_form [name=m] [value=afr]').prop('selected', true)
    else if(market.indexOf('com') > -1)
      $('#search_form [name=m] [value=aus]').prop('selected', true)

    $('#search_form [name=asin]').val(args[0])
    $('#search_form button').click()
  activeAmazonReview()


  # -------- Review 表格
  $('a[href=#review_table]').on('shown',
  (e) ->
    if $("#search_form [name=asin]").val() == ""
      alert("请输入 ASIN")
      $("#search_form [name=asin]").focus()
      return
    loadReviewTable()

  )
  loadReviewTable = ->
    $('#review_table').load("/AmazonOperations/reviewTable", $('#search_form :input').fieldSerialize())

  #---------Wish List列表
  $('a[href=#wish_list]').on('shown',
  (e) ->
    if $("#search_form [name=asin]").val() == ""
      alert("请输入 ASIN")
      $("#search_form [name=asin]").focus()
      return
    loadWishList()
  )
  loadWishList = ->
    params = $('#search_form :input').fieldSerialize()
    $('#wish_list').load("/AmazonOperations/wishList", params,
    ->
      $('#add_wishlist').unbind('click').bind('click',
      (e)->
        mask = $('#container')
        mask.mask('添加到WishList中...')
        $.post("/AmazonOperations/addToWishList", params,
        (success) ->
          mask.unmask()
          if success then loadWishList() else alert '添加失败'
        )
        e.preventDefault()
      )
    )





