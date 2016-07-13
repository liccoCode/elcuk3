$ ->
  $(document).ready ->
    $shipType = $("[name='unit.shipType']")
    $shipType.trigger('change') if $shipType.val() != undefined && $shipType.val() != 'EXPRESS'
    if $("#sellingId")
      checkCoopertorBySelling($("#sellingId").val())
      getCooperItemBySku($("#unit_sku").val())
    if $("#unit_sku").val()
      getStockBySku($("#unit_sku").val())

  $('#box_num').change (e) ->
    e.preventDefault()
    coperId = $("select[name='unit.cooperator.id']").val()
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

  # 计算时间到库日期与运输日期的差据
  $("[name='unit.attrs.planArrivDate']").change () ->
    planShipDate = $("[name='unit.attrs.planShipDate']")
    planArrivDate = $(@)
    if planArrivDate.val() and planShipDate.val()
      planArrivDate.next().text("#{(new Date(planArrivDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000)} 天")

  $("[name='unit.attrs.planShipDate']").change () ->
    shipType = $("[name='unit.shipType']:checked").val()
    if shipType != 'EXPRESS'
      return
    planShipDate = $("[name='unit.attrs.planShipDate']").val()
    warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {planShipDate: planShipDate, shipType: shipType, warehouseid})
    .done((r) ->
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate'])
    )


  # Ajax 加载 Shipment
  $('#new_procureunit,#unitEditForm,#update_form,#splitUnitForm').on('change', "[name='unit.shipType'],[name='unit.whouse.id']", ->
    whouseId = $("[name='unit.whouse.id']").val()
    shipType = $("[name='unit.shipType']:checked").val()
    shipment = $("#shipments")
    return unless (whouseId && shipType && shipment.size() > 0)

    if shipType == 'EXPRESS'
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.')
    else
      LoadMask.mask(shipment)
      $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType})
      .done((html) ->
        shipment.html(html)
        LoadMask.unmask()
      )

    if shipType != 'EXPRESS'
      return
    planShipDate = $("[name='unit.attrs.planShipDate']").val()
    if planShipDate == ''
      return
    warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {planShipDate: planShipDate, shipType: shipType, warehouseid})
    .done((r) ->
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate'])
    )
  )

  $('#shipments').on('change', '[name=shipmentId]', (e) ->
    LoadMask.mask()
    $.get("/shipment/#{@getAttribute('value')}/dates")
    .done((r) ->
      $("#planShipDate").data('dateinput').setValue(r['begin'])
      $("#planArrivDate").data('dateinput').setValue(r['end'])
      LoadMask.unmask()
    )
  )

  $('#new_procureunit').on('change', "[name='unit.product.sku']", ->
    $cooperators = $("select[name='unit.cooperator.id']")
    # 当输入的 sku 长度大于5才发起 Ajax 请求获取供应商列表
    if this.value.length > 5
      LoadMask.mask()
      # Ajax 加载供应商列表
      $.get('/products/cooperators', {sku: this.value})
      .done((r) ->
        $cooperators.empty()
        $cooperators.append("<option value=''>请选择</option>")
        r.forEach (value) ->
          $cooperators.append("<option value='#{value.id}'>#{value.name}</option>")
        LoadMask.unmask()
      )
  ).on('click', "#create_unit", (e) ->
    e.preventDefault()
    if !$("#unit_sku").val()
      $("#unit_sku").focus()
      noty({text: "请先填写SKU或者Selling！", type: 'error'})
      return false
    if !$("#planQty").val()
      $("#planQty").focus()
      noty({text: "请先填写采购数量！", type: 'error'})
      return false
    if !$("#planDeliveryDate").val()
      noty({text: "请先填写预计交货日期！", type: 'error'})
      $("#planDeliveryDate").focus()
      return false
    $("#new_procureunit").submit()
  )

  $sku = $("#unit_sku")
  $sku.typeahead({
    source: (query, process) ->
      sku = $sku.val()
      $.get('/products/sameSku', {sku: sku})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      getStockBySku(item)
      getProductNmae(item)
      getCooperItemBySku(item)
      item
  })

  coop_hash = {}

  $sellingId = $("#sellingId")
  $sellingId.typeahead({
    source: (query, process) ->
      name = $sellingId.val()
      $.get('/sellings/sameSelling', {name: name})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      sku = item.split(',')[0]
      $("#unit_sku").val(sku)
      $("#unit_sku").attr("readonly", true)
      getStockBySku(sku)
      getProductNmae(sku)
      checkCoopertorBySelling(item)
      getCooperItemBySku(sku)
      item
  })

  getProductNmae = (sku) ->
    $.post('/products/findProductName', sku: sku, (r) ->
      $("#productName").val(r.name)
    )
  #获取供应商
  getCooperItemBySku = (sku) ->
    $.getJSON("/cooperators/findCoopItemBySku", sku: sku, (list) ->
      coop_hash = {}
      for item, i in list
        cooper_id = item["cooper_id"]
        cooper_name = item["cooperator"]["name"]
        $("#cooperator").append("<option value='" + cooper_id + "'>" + cooper_name + "</option>")
        coop_hash[cooper_id] = item
      if $("#cooperator").attr("coop_value")
        $("#cooperator option[value='" + $("#cooperator").attr("coop_value") + "']").attr("selected", true)
    )

  checkCoopertorBySelling = (selling) ->
    for select, i in $("#warehouse_select option")
      name = $(select).text()
      name = "A_" + name.split('_')[1]
      if selling.indexOf(name) > -1
        $(select).attr("selected", true)


  $("#cooperator").change(->
    value = coop_hash[$(@).val()]
    period = value["period"]
    $("#coop_text").text("生产周期(day): " + period + ";  每箱数量: " + value["boxSize"] + ";  最低采购量:" + value["lowestOrderNum"])
    $("#planDeliveryDate").val($.DateUtil.fmt2($.DateUtil.addDay(period, new Date())))
  )

  getStockBySku = (sku) ->
    $("#stockDiv").load('/ProcureUnits/showStockBySellingOrSku', {name: sku, type: "SKU"})








