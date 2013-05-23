$ ->

  # 切换供应商, 自行寻找价格
  $("select[name='unit.cooperator.id']").change (e) ->
    id = $(@).val()
    if not id
      # 恢复默认
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").val('')
      return false

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
  $('#new_procureunit,#unitEditForm').on('change', "[name='unit.shipType'],[name='unit.whouse.id']", ->
    whouseId = $("[name='unit.whouse.id']").val()
    shipType = $("[name='unit.shipType']:checked").val()
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
