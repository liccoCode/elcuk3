$ ->
# 检查字符串长度
  validateMaxLength = (maxLength, obj) ->
    o = $(obj)
    length = unescape(encodeURI(o.val().trim())).length
    o.find('~ span').html((maxLength - length) + " bytes left")
    if length > maxLength then o.css('color', 'red') else o.css('color', '')
    false

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
  previewBtn = ->
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
