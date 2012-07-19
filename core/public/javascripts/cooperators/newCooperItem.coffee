$ ->
# 对参数进行 trim 处理
  $('#copItem_sku').parents('form').find(':input').change (e) ->
    o = $(@)
    o.val(o.val().trim())
    e.preventDefault()
