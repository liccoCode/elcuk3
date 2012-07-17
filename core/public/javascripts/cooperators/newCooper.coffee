$ ->
  $('[name=cop\\.name]').change (e) ->
    o = $(@)
    o.val(o.val().toUpperCase()) if o.val()
    e.preventDefault()

