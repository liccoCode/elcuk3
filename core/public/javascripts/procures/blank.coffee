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


  loadShipment = (shipment, whouseId, shipType) ->
    if whouseId is  ''
      return
    mask = $('#container')
    mask.mask("加载关联运输单中...")
    shipment.load("/shipments/unitShipments", {whouseId: whouseId, shipType: shipType},
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
      ##初始化加载页面的toggle事件.
      toggle_init()
    )

  initShipments = (shipment) ->

    whouseSelect = $('[name=unit\\.whouse\\.id]')
    shipTypeSelect = $('input[name="unit.shipType"]')

    ##如果页面加载时unit.shipType为空,默认选中第一项
    if !shipTypeSelect.is(":checked")
      shipTypeSelect.eq(0).attr("checked",'checked')

    ##判断是否选择仓库,否则不加载数据
    if whouseSelect.val()  isnt ''
      loadShipment(shipment, whouseSelect.val(), shipTypeSelect.val())
    whouseSelect.change(->
      loadShipment(shipment, whouseSelect.val(), shipTypeSelect.filter(":checked").val()))
    shipTypeSelect.change(->
      loadShipment(shipment, whouseSelect.val(),this.value))

  initShipments($('#shipments'))
  # 计算时间到库日期与运输日期的差据
  $('[name=unit\\.attrs\\.planArrivDate]').change () ->
    planShipDate = $('[name=unit\\.attrs\\.planShipDate]')
    planArrivDate = $(@)
    if planArrivDate.val() and planShipDate.val()
      planArrivDate.next().text("#{(new Date(planArrivDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000)} 天")

