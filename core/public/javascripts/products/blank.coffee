$ ->
  $('#pro_family').change (e) ->
    $('#pro_sku').val($(@).val() + "-").focus()

  $('#pro_sku').keyup (e) ->
    $(@).val($(@).val().toUpperCase())

  # 产品定位和产品卖点点击新增一行
  $("#create_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", () ->
    alert(1)
  )
