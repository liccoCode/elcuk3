$ ->

  # 切换供应商, 自行寻找价格
  $("select[name=unit\\.cooperator\\.id]").change (e) ->
    id = $(@).val()
    if not id
      # 恢复默认
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").val('')
      return false

    mask = $('#container')
    mask.mask()
    $.getJSON('/Cooperators/price', {id: id, sku: $('#unit_sku').val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          $("#unit_currency option:contains('#{r.currency}')").prop('selected', true)
          $("#unit_price").val(r.price)
        mask.unmask()
    )
