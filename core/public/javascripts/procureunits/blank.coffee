$ ->

  # 切换供应商, 自行寻找价格
  $("select[name='unit.cooperator.id']").change (e) ->
    id = $(@).val()
    if not id
      # 恢复默认
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").val('')
      return false

    mask = $('#container')
    mask.mask()
    $.get('/Cooperators/price', {id: id, sku: $('#unit_sku').val()}, 'json')
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          $("#unit_currency option:contains('#{r.currency}')").prop('selected', true)
          $("#unit_price").val(r.price)
        mask.unmask()
      )

  $('#box_num').change (e) ->
    e.preventDefault()
    coperId = $("select[name='unit.cooperator.id']").val()
    if not coperId
      alert('请先选择 供应商')
      return false
    $.post('/procures/calculateBox', {size: $('#box_num').val(), coperId: coperId, sku: $('#unit_sku').val()})
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          $("input[name='unit.attrs.planQty']").val(r['message'])
      )

  # 计算时间到库日期与运输日期的差据
  $("[name='unit.attrs.planArrivDate']").change () ->
    planShipDate = $("[name='unit.attrs.planShipDate']")
    planArrivDate = $(@)
    if planArrivDate.val() and planShipDate.val()
      planArrivDate.next().text("#{(new Date(planArrivDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000)} 天")


  # Ajax 加载 Shipment
  loadShipment = (shipment, whouseId, shipType) ->
    return unless (whouseId && shipType)
    mask = $('#container')
    mask.mask("加载关联运输单中...")
    $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType})
      .done((html) ->
        shipment.html(html)
        mask.unmask()
      )

  do ->
    shipment = $('#shipments')
    whouseSelect = $("[name='unit.whouse.id']")
    shipTypeSelect = $('input[name="unit.shipType"]')

    whouse = $("[name='unit.selling.sellingId']").val().split("|")[1]
    whouse = whouse.replace("A", "FBA")

    for value, option of whouseSelect.find("option")
      if option.text == whouse
        whouseSelect.val(value)
        break

    whouseSelect.change ->
      loadShipment(shipment, whouseSelect.val(), shipTypeSelect.filter(":checked").val())

    shipTypeSelect.change ->
      loadShipment(shipment, whouseSelect.val(), this.value)

