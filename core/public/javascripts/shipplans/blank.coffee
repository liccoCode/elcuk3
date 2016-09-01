$ ->
# Ajax 加载 Shipment
  $('#new_shipplan, #update_shipplan').on('change', "[name='plan.shipType'],[name='plan.whouse.id']", ->
    whouseId = $("[name='plan.whouse.id']").val()
    shipType = $("[name='plan.shipType']:checked").val()
    shipment = $("#shipments")
    return unless (whouseId && shipType && shipment.size() > 0)

    if shipType == 'EXPRESS'
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.')
    else
      LoadMask.mask(shipment)
      $.get('/shipments/unitShipments', {
        whouseId: whouseId,
        shipType: shipType,
        planDeliveryDate: $("input[name='plan.planShipDate']").val()
      })
      .done((html) ->
        shipment.html(html)
        LoadMask.unmask()
      )
  ).on('click', '#resetShipCondition', (e) ->
    $("input[name='plan.planShipDate']").val('')
    $("input[name='plan.planArrivDate']").val('')
    $("[name=shipmentId]").attr('checked', false)
    $("[name='plan.shipType']").attr('checked', false)
  )

  $('#shipments').on('change', '[name=shipmentId]', (e) ->
    LoadMask.mask()
    $.get("/shipment/#{@getAttribute('value')}/dates")
    .done((r) ->
      $("input[name='plan.planShipDate']").data('dateinput').setValue(r['begin'])
      $("input[name='plan.planArrivDate']").data('dateinput').setValue(r['end'])
      LoadMask.unmask()
    )
  )

  initTypeahead = () ->
    $sellingId = $("input[name='plan.selling.sellingId']")
    $sellingId.typeahead({
      source: (query, process) ->
        name = $sellingId.val()
        $.get('/sellings/sameSelling', {name: name})
        .done((c) ->
          process(c)
        )
      updater: (item) ->
        sku = item.split(',')[0]
        $("input[name='plan.product.sku']").attr("readonly", true).val(sku).trigger('change')
        getStockBySku(sku)
        getProductNmae(sku)
        checkCoopertorBySelling(item)
        item
    })

  getStockBySku = (sku) ->
    return if _.isEmpty(sku)
    $("#stockDiv").load('/ProcureUnits/showStockBySellingOrSku', {name: sku, type: "SKU", flag: "shipPlan"})

  getProductNmae = (sku) ->
    $.post('/products/findProductName', sku: sku, (r) ->
      $("input[name='plan.product.abbreviation']").val(r.name)
    )

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

  $(document).ready ->
    initTypeahead()
    getStockBySku($("input[name='plan.product.sku']").val())