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

  # 方便提供自动加载其他 Selling 的功能
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

  # hints...
  $('#productType').popover({trigger: 'focus', content: '修改这个值请非常注意, Amazon 对大类别下的产品的 Product Type 有严格的规定, 请参考 Amazon 文档进行处理'})
  $('#templateType').popover({trigger: 'focus', content: '为上传给 Amazon 的模板选择, 与 Amazon 的市场有关, 不可以随意修改'})
  $('#partNumber').popover({trigger: 'focus', content: '新 UPC 被使用后, Part Number 会被固定, 这个需要注意'})
  $('#state').popover({trigger: 'focus', content: 'NEW 状态 Selling 还没有同步回 ASIN, SELLING 状态为正常销售'})
