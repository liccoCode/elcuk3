$ ->

  # 切换供应商, 自行寻找价格
  $("select[name='dmt.cooperator.id']").change (e) ->
    id = $(@).val()
    if id
      LoadMask.mask()
      $.get('/Cooperators/price', {id: id, sku: $('#unit_sku').val()}, 'json')
        .done((r) ->
          if r.flag is false
            alert(r.message)
          else
            $("#unit_currency option:contains('#{r.currency}')").prop('selected', true)
            $("#unit_price").val(r.price)
          LoadMask.unmask()
        )
      # 恢复默认
    else
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").val('')

  $('#box_num').change (e) ->
    e.preventDefault()
    coperId = $("select[name='dmt.cooperator.id']").val()
    if coperId
      $.post('/cooperators/boxSize', {size: $('#box_num').val(), coperId: coperId, sku: $('#unit_sku').val()})
        .done((r) ->
          if r.flag is false
            alert(r.message)
          else
            $("input[name='unit.attrs.planQty']").val(r['message'])
        )
    else
      alert('请先选择 供应商')

  $('#new_procureunit').on('change', "[name='unit.product.sku']", ->
    $cooperators = $("select[name='dmt.cooperator.id']")
    # 当输入的 sku 长度大于5才发起 Ajax 请求获取供应商列表
    if this.value.length > 5
      # Ajax 加载供应商列表
      $.get('/products/cooperators', {sku: this.value})
        .done((r) ->
          $cooperators.empty()
          $cooperators.append("<option value=''>请选择</option>")
          r.forEach (value) ->
            $cooperators.append("<option value='#{value.id}'>#{value.name}</option>")
        )
  )