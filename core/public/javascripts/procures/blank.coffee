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

  $('#calculate_box').click (e) ->
    e.preventDefault()
    coperId = $("select[name=unit\\.cooperator\\.id]").val()
    if not coperId
      alert('请先选择 供应商')
      return false
    $.post('/procures/calculateBox', {size: $('#box_num').val(), coperId: coperId, sku: $('#unit_sku').val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          $('input[name=unit\\.attrs\\.planQty]').val(r['message'])
    )


  loadShipment = (shipment, whouseId) ->
    mask = $('#container')
    mask.mask("加载关联运输单中...")
    shipment.load("/shipments/unitShipments", {whouseId: whouseId},
      ->
        o = $(@)
        if $("[name=shipmentId]").val()
          o.find(":checkbox").each ->
            $(@).prop('checked', true) if $(@).val() == $("[name=shipmentId]").val()

        o.find(':checkbox').click(
          ->
            # 保留目标值
            checked = $(@).prop("checked")
            o.find(':checkbox').prop("checked", false)
            $(@).prop('checked', checked)
            $("[name=shipmentId]").val(checked and $(@).val() or "")
        )
        mask.unmask()
    )

  initShipments = (shipment) ->
    if(shipment.size() == 1)
      select = $('[name=unit\\.whouse\\.id]')
      loadShipment(shipment, select.val())
      select.change(-> loadShipment(shipment, select.val()))
  initShipments($('#shipments'))

  # 计算时间到库日期与运输日期的差据
  $('[name=unit\\.attrs\\.planArrivDate]').change () ->
    planShipDate = $('[name=unit\\.attrs\\.planShipDate]')
    planArrivDate = $(@)
    if planArrivDate.val() and planShipDate.val()
      planArrivDate.next().text("#{(new Date(planArrivDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000)} 天")

