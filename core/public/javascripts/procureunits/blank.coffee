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
  loadShipment = ->
    whouseId = $("[name='unit.whouse.id']").val()
    shipType = $('input[name="unit.shipType"]').val()
    shipment = $('#shipments')
    return unless (whouseId && shipType && shipment.size() > 0)
    mask = $('#container')
    mask.mask("加载关联运输单中...")
    $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType})
      .done((html) ->
        shipment.html(html)
        mask.unmask()
      )

  do ->
    $("[name='unit.whouse.id']").change(-> loadShipment())
    $('input[name="unit.shipType"]').change(-> loadShipment()).change()
    $('#shipments').on('click', 'input:checked')

