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

    planDeliveryDate = $("#planDeliveryDate").val()
    if shipType == 'EXPRESS'
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.')
    else
      LoadMask.mask()
      $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType, planDeliveryDate: planDeliveryDate})
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

  $("#del_sid").click(->
    $("#sellingId").val("")
    $("#warehouse_select option[value='']").prop("selected", "selected")
  )

  # Ajax 加载 Sellingid
  $sellingId = $("input[name='newUnit.selling.sellingId']")
  $sellingId.typeahead({
    source: (query, process) ->
      msku = $("#unit_sku").val()
      return if _.isEmpty(msku)
      $.get('/sellings/sameFamilySellings', {msku: msku})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      checkCoopertorBySelling(item)
      item
  })

  checkCoopertorBySelling = (selling) ->
    for select, i in $("#warehouse_select option")
      name = $(select).text()
      name = "A_" + name.split('_')[1]
      if selling.indexOf(name) > -1
        $(select).attr("selected", true)

  $("#warehouse_select").change(->
    if $(@).val()
      country = $("#warehouse_select :selected").text().split('_')[1]
      sku = $("#unit_sku").val()
      $.get("/sellings/findSellingBySkuAndMarket", {sku: sku, market: "AMAZON_" + country})
      .done((c) ->
        $("#sellingId").val(c)
        if !$("#sellingId").val()
          noty({text: "市场对应无Selling", type: 'error'})
      )
    else
      $("#sellingId").val("")
      $("input[name='newUnit.shipType']").each(->
        $(@).attr("checked", false)
      )
  )
