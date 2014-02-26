$ ->

  $('#new_procureunit').on('change', "[name='unit.product.sku']", ->
    $cooperators = $("select[name='unit.cooperator.id']")
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