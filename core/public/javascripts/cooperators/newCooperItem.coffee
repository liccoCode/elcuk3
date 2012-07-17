$ ->
  $('#copItem_sku').change (e) ->
    o = $(@)
    o.find('~ input').first().val(o.val())
    e.preventDefault()

