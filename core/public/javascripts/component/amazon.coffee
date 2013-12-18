$ ->
  # 检查字符串长度
  validateMaxLength = (maxLength, obj) ->
    $text = $(obj)
    length = unescape(encodeURI(jsEscapeHtml($text.val().trim()))).length
    $text.find('~ .help-inline').html((maxLength - length) + " bytes left")
    if length > maxLength then $text.css('color', 'red') else $text.css('color', '')
    false

  jsEscapeHtml = (string) ->
    $("<div/>").text(string).html()

      # 预览 Desc 的方法
  previewBtn = (e) ->
    invalidTag = false
    for tag in $('#previewDesc').html($('#productDesc').val()).find('*')
      switch tag.nodeName.toString().toLowerCase()
        when 'br','p','b','#text'
          break
        else
          invalidTag = true
          $(tag).css('background', 'yellow')
    noty({text: '使用了 Amazon 不允许使用的 Tag, 请查看预览中黄色高亮部分!', type: 'error', timeout: 3000}) if invalidTag is true

  valid_length = (element) ->
    console.log(element.getAttribute('id'))
    if element.getAttribute('id').indexOf('bulletPoint') > -1
        2000
      else if element.getAttribute('id').indexOf('searchTerms') > -1
        50
      else if element.getAttribute('id').indexOf('productDesc') > -1
        2000
      else
        1000

  # bullet_point 的检查, search Terms 的检查, Product DESC 输入, 字数计算
  $('#saleAmazonForm').on('keyup blur', "[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss']", (e) ->
    return false if e.keyCode is 13
    validateMaxLength(valid_length(@), @)
  ).on('keyup', "[name='s.aps.productDesc']", (e) ->
    validateMaxLength(valid_length(@), @)
  ).on('blur', "[name='s.aps.productDesc']", (e) ->
    validateMaxLength(valid_length(@), @)
    previewBtn.call(@, e)
  ).on('click', '.btn:contains(Preview)', (e) ->
    previewBtn.call(@, e)
    false
  )

  $("[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss'],[name='s.aps.productDesc']").blur()

  $('#sellingPreview').on('click', '#sid_preview',(e) ->
    noty({text: _.template($('#tsp-show-template').html(), {tsp: $(@).data('tsp')})})
    false
  ).on('change', 'input',(e) ->
    #  自动补全的 sid 的功能条
    $input = $(@)
    if $input.data('sids') is undefined
      $input.data('sids', $input.data('source'))
    return false if !(@value in $input.data('sids'))

    LoadMask.mask()
    $.ajax('/sellings/tsp', {type: 'GET', data: {sid: @value}, dataType: 'json'})
      .done((r) ->
        $('#sid_preview').data('tsp', r)
        noty({text: '加载成功, 可点击 "放大镜" 查看详细信息或者点击 "填充" 进行填充', type: 'success', timeout: 3000})
        LoadMask.unmask()
      )
  ).on('click', 'button:contains(填充)', (e) ->
    # 加载 tsp 数据的按钮
    json = $('#sid_preview').data('tsp')
    if json is undefined
      noty({text: '还没有数据, 请先预览!', type: 'warning', timeout: 3000})
      return false
    # product Desc
    $("[name='s.aps.productDesc']").val(json['p'][0]).blur()
    # technical
    tech = json['t']
    $("[name='s.aps.keyFeturess']").each((i) ->
      $(@).val(if tech[i] then tech[i] else '').blur()
    )
    # searchTerms
    search = json['s']
    $("[name='s.aps.searchTermss']").each((i) ->
      $(@).val(if search[i] then search[i] else '').blur()
    )
    false
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
      cancel_btn.on('click', ->
        modal.modal('hide'))
    $('#check_apply').off('click').on('click', callback)

  # upc 检查 Selling 的关闭事件
  modal_upc_check_close = (e) ->
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
      )
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
    $('#msku').val(->
      @value.split(',')[0]
    )
    upc = $(@).removeClass('btn-warning btn-success').addClass('btn-warning').prev().val()
    if !$.isNumeric(upc)
      alert('UPC 必须是数字')
      return false

    $.ajax('/products/upcCheck', {type: 'GET', data: {upc: upc}, dataType: 'json'})
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          show_selling_modal("#{$('#sku').val()} (#{r.length})", r, modal_upc_check_close)
      )
    false
