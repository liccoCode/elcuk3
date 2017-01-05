$ ->


  $("[name='unit.attrs.planShipDate']").change () ->
    shipType = $("[name='unit.shipType']:checked").val()
    if shipType != 'EXPRESS'
      return
    planShipDate = $("[name='unit.attrs.planShipDate']").val()
    warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {
      planShipDate: planShipDate,
      shipType: shipType,
      warehouseid
    })
      .done((r) ->
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate'])
    )

  $(document).ready ->
    $shipType = $("[name='unit.shipType']")
    $shipType.trigger('change') if $shipType.val() != undefined && $shipType.val() != 'EXPRESS'





