$ ->
  # 检查字符串长度
  validateMaxLength = (maxLength, obj) ->
    o = $(obj)
    length = unescape(encodeURI(jsEscapeHtml(o.val().trim()))).length
    o.find('~ span').html((maxLength - length) + " bytes left")
    if length > maxLength then o.css('color', 'red') else o.css('color', '')
    false

  jsEscapeHtml = (string) ->
    $("<div/>").text(string).html()

  # bullet_point 的检查
  $('input[bullet_point]').keyup(
    (e) ->
      return false if e.keyCode is 13
      validateMaxLength(2000, @)
  ).keyup().blur ->
    validateMaxLength(2000, @)

  # search Terms 的检查
  $('input[searchterms]').keyup(
    (e) ->
      return false if e.keyCode is 13
      validateMaxLength(50, @)
  ).keyup().blur ->
    validateMaxLength(50, @)

  # 预览 Desc 的方法
  previewBtn = (e) ->
    e.preventDefault()
    ownerDiv = $(@).parent()
    invalidTag = false
    for tag in ownerDiv.siblings('div').html(ownerDiv.find(':input').val()).find('*')
      switch tag.nodeName.toString().toLowerCase()
        when 'br','p','b','#text'
          break
        else
          invalidTag = true
          $(tag).css('background', 'yellow')
    alert('使用了 Amazon 不允许使用的 Tag, 请查看预览中黄色高亮部分!') if invalidTag is true

  # Product DESC 输入, 字数计算
  $('textarea[name=s\\.aps\\.productDesc]').blur(previewBtn).keyup(
    ->
      validateMaxLength(2000, @)
  )
  # 自己按一下, 再页面开始的时候计算一次
    .keyup().find('~ button').click(previewBtn).click()

  #  自动补全的 sid 的功能条
  SID_PREVIEW_TEMPLATE = "<div><h3>Technical</h3><p id='t'></p><hr><h3>SearchTerms</h3><p id='s'></p><hr><h3>ProductDesc</h3><p id='p'></p></div>"
  $('#sid_helper').change ->
    o = $(@)
    if o.data('sids') is undefined
      o.data('sids', JSON.parse(o.attr('data-source')))

    return false if !(@value in o.data('sids'))

    toolBar = o.parent()
    toolBar.mask('加载数据中...')
    $.getJSON('/sellings/tsp', sid: @value,
      (json) ->
        html = $(SID_PREVIEW_TEMPLATE)
        html.find('#t').html(json['t'].join('<br/><br/>'))
        html.find('#s').html(json['s'].join('<br/><br/>'))
        html.find('#p').html(json['p'][0])
        $('#sid_preview_popover').attr('data-content', html.html()).data('tsp', json).click()
        toolBar.unmask()
    )

  # 加载 tsp 数据的按钮
  $('#sid_helper + button').click(
    (e) ->
      e.preventDefault()
      json = $('#sid_preview_popover').data('tsp')
      if json is undefined
        alert('还没有数据, 请先预览!')
        return false
      # product Desc
      $('[name=s\\.aps\\.productDesc]').val(json['p'][0]).blur()
      # technical
      for t, i in json['t']
        $('[name=s\\.aps\\.keyFeturess\\[' + i + '\\]]').val(t).blur()
      # searchTerms
      for s, i in json['s']
        $('[name=s\\.aps\\.searchTermss\\[' + i + '\\]]').val(s).blur()
  )

  # 显示 Selling 上架信息的 Modal 窗口
  show_selling_modal = (title, sellings, callback, close_callback = undefined) ->
    $.Deferred()
    modal = $('#check_modal').find('#upc_num').html(title).end()
    if sellings.length == 0
      modal.find('.innder-modal').html('<p>暂时没有上架 Selling</p>')
    else
      template = modal.find('.innder-modal').html('').end().find('.template')
      sellings.forEach (obj, index, arr) ->
        modal.find('.innder-modal').append(
          template.clone().removeClass('template').find('.check_id').html('SKU: ' + obj.sellingId).end()
            .find('.check_title').html(obj.aps.title).end()
        )
    modal.modal('show')
    cancel_btn = $('#check_cancel').off('clicl')
    if close_callback
      cancel_btn.on('click', close_callback)
    else
      cancel_btn.on('click', -> modal.modal('hide'))
    $('#check_apply').off('click').on('click', callback)

  # upc 检查 Selling 的关闭事件
  modal_upc_check_close =  (e) ->
    e.preventDefault()
    $('#msku').val(->
      $('#check_modal').modal('hide')
      $('#check_upc').removeClass('btn-warning').addClass('btn-success')
      "#{@value},#{$('[name=s\\.aps\\.upc]').val()}"
    )

  modal_sku_check_close = (e) ->
    e.preventDefault()
    $('#check_modal').modal('hide')
    currency = ''
    switch $('#market').val()
      when 'AMAZON_UK', 'EBAY_UK'
        currency = '&pound;'
      when 'AMAZON_US'
        currency = "$"
      else
        currency = '&euro;'
    $('span.currency').html(currency)

  modal_sku_check_cancel = (e) ->
    $('#market').val(0)
    $('#check_modal').modal('hide')


  #Market 更换价格单位按钮
  $('#market').change ->
    # 1. SKU + Market 的 Selling 提示
    market = $(@).val()
    $.getJSON('/products/skuMarketCheck', {sku: $('#sku').val(), market: market})
      .done((r) ->
        if r.flag is false
          alert r.message
        else
          show_selling_modal("#{$('#sku').val()} (#{r.length})", r, modal_sku_check_close, modal_sku_check_cancel)
      );
    # 2. 货币符号的变化. -> modal_sku_check_close
    false


  # 账号对应的市场切换
  $('#account').change ->
    switch $(@).find("option:selected").text().split('.')[0]
      when 'A_US'
        val = "com"
      when 'A_DE'
        val = "de"
      when 'A_UK'
        val = 'co.uk'
      else
        val = ''
    $("#market option:contains(amazon.#{val})").prop('selected', true).change()

  # UPC 检查
  $('#check_upc').click (e) ->
    e.preventDefault()
    $('#msku').val(-> @value.split(',')[0])
    upc = $(@).removeClass('btn-warning btn-success').addClass('btn-warning').prev().val()
    if !$.isNumeric(upc)
      alert('UPC 必须是数字')
      return false

    $.getJSON('/products/upcCheck', {upc: upc})
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          show_selling_modal("#{$('#sku').val()} (#{r.length})", r, modal_upc_check_close)
      )

