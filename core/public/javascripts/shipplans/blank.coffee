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
        item
    })

  getStockBySku = (sku) ->
    return if _.isEmpty(sku)
    $("#stockDiv").load('/ProcureUnits/showStockBySellingOrSku', {name: sku, type: "SKU", flag: "shipPlan"})

  getProductNmae = (sku) ->
    $.post('/products/findProductName', sku: sku, (r) ->
      $("input[name='plan.product.abbreviation']").val(r.name)
    )

  $(document).ready ->
    initTypeahead()
    getStockBySku($("input[name='plan.product.sku']").val())