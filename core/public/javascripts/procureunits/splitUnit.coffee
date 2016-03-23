$ ->

  # 切换供应商, 自行寻找价格
  $("select[name='newUnit.cooperator.id']").change (e) ->
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
    coperId = $("select[name='newUnit.cooperator.id']").val()
    if coperId
      $.post('/cooperators/boxSize', {size: $('#box_num').val(), coperId: coperId, sku: $('#unit_sku').val()})
      .done((r) ->
          if r.flag is false
            alert(r.message)
          else
            $("input[name='newUnit.attrs.planQty']").val(r['message'])
        )
    else
      alert('请先选择 供应商')

  # Ajax 加载 Shipment
  $('#splitUnitForm').on('change', "[name='newUnit.shipType']", ->
    whouseId = $("[name='newUnit.whouse.id']").val()
    shipType = $("[name='newUnit.shipType']:checked").val()
    shipment = $('#shipments')
    return unless (whouseId && shipType && shipment.size() > 0)

    if shipType == 'EXPRESS'
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.')
    else
      LoadMask.mask()
      $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType})
      .done((html) ->
          shipment.html(html)
          LoadMask.unmask()
        )
  )

  $('#shipments').on('change', '[name=shipmentId]', (e) ->
    LoadMask.mask()
    $.get("/shipment/#{@getAttribute('value')}/dates")
    .done((r) ->
        $("input[name='newUnit.attrs.planShipDate']").data('dateinput').setValue(r['begin'])
        $("input[name='newUnit.attrs.planArrivDate']").data('dateinput').setValue(r['end'])
        LoadMask.unmask()
      )
  )

  # Ajax 加载供应商列表
  $('#splitUnitForm').on('change', "[name='newUnit.product.sku']", ->
    $cooperators = $("select[name='newUnit.cooperator.id']")
    msku = this.value
    # 当输入的 sku 长度大于5才发起 Ajax 请求获取供应商列表
    if msku.length > 5
      LoadMask.mask()
      $.get('/products/cooperators', {sku: msku})
      .done((r) ->
          $cooperators.empty()
          $cooperators.append("<option value=''>请选择</option>")
          r.forEach (value) ->
            $cooperators.append("<option value='#{value.id}'>#{value.name}</option>")
        )
      LoadMask.unmask()
  )

  # Ajax 加载 Sellingid
  $sellingId = $("input[name='newUnit.selling.sellingId']")
  $sellingId.typeahead({
    source: (query, process) ->
      msku = $("#unit_sku").val()
      return if msku is null || msku is "" || msku is undefined

      $.get('/sellings/sameFamilySellings', {msku: msku})
      .done((c) ->
          process(c)
        )
  })