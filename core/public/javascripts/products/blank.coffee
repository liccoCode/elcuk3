$ ->
  $('#pro_family').change (e) ->
    $('#pro_sku').val($(@).val() + "-").focus()

  $('#pro_sku').keyup (e) ->
    $(@).val($(@).val().toUpperCase())
