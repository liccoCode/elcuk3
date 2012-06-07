$ ->
  $('button[rel=tooltip]').tooltip()

  ALL_SKU_LINKS = $('#slider a[level=sku]')
  SELLINGS = $("#s_list")
  LISTINGS = $('#l_list')
  ACCOUNT = $('#account')
  MARKET = $('#market')


  # 根据 listingId, accountId 加载 Selling 页面
  loadSellings = (lid, aid) ->
    SELLINGS.mask('加载中...')
    SELLINGS.load('/listings/listingSellings', 'l.listingId': lid, 'a.id': aid, -> SELLINGS.unmask())


  # 返回所需要的本地值
  loadGlobalVar = ->
    account: ACCOUNT.val()
    market: MARKET.val()


  # 当 account 元素发生改变的时候重新加载 Selling
  ACCOUNT.change ->
    loadSellings($(@).data('lid'), @value)


  # 给现在页面上的所有 Listing 列表中的链接绑定加载 Selling 的事件
  bindListingAnchorClick = ->
    LISTINGS.find('a[lid]').click ->
      o = $(@)
      o.parents('table').find('tr').removeClass('active')
      o.parent().parent().addClass('active')
      aAndm = loadGlobalVar()
      SELLINGS.mask('加载中...')
      # 每点击一次将 listingId 记录到 Account 身上,以便 Account 转换时候提供参数
      ACCOUNT.data('lid', o.attr('lid'))
      loadSellings(ACCOUNT.data('lid'), aAndm['account'])
      false

  # 给导航栏左侧的所有 SKU 链接添加加载 Listing 的事件
  ALL_SKU_LINKS.click ->
    ALL_SKU_LINKS.parent().removeClass('active')
    $(@).parent().addClass('active')
    LISTINGS.mask('加载中...')
    aAndm = loadGlobalVar()

    LISTINGS.load('/listings/prodListings', 'p.sku': $(@).attr('pid'), m: aAndm['market'],
      ->
        LISTINGS.unmask()
        SELLINGS.html('')
        bindListingAnchorClick()
    )

  # Listing 页面重新抓取这个 Listing
  $('button[lid]').click ->
    alert(999)
    o = $(@)
    lid = o.attr('lid')
    o.button('loading').addClass('disabled')
    $.post('/listings/reCrawl', 'l.listingId': lid,
      (r) ->
        if r.flag is true
          o.after("<span><a style='background-color:#DFF0D8;margin-left:10px;padding:8px;' href='/listings/listing?lid=" + lid + "'>更新成功</a></span>")
        else
          alert(r.message)
        o.button('reset').removeClass('disabled')
    )

