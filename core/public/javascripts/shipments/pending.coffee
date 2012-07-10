$ ->
# 点击 Plan tab 中选中的 tr
  trClick = ->
    $("#pending tr[row][class=active]").click()

  # 删除某一个 ShipItem
  bindShipItemDeleBtn = ->
    $("#shipitem button").unbind().click ->
      tr = $(@).parents('tr')
      $.post('/shipments/removeItemFromShipment', {shipItemId: tr.attr("shipitem")},
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            trClick()
      )


  # 确认此 Shipment 到 Shipping 状态
  bindConfirmShipmentBtn = ->
    $("#confirmShipment button").click ->
      $.varClosure.params = {}
      $('#confirmShipment :input').map($.varClosure2)
      if $.varClosure.params['sTmp.trackNo'] in ['', undefined]
        alert('请填写 trckNo')
        return false
      $.post('/shipments/confirmShipment', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            alert('运输单' + r['id'] + '确认成功. 2s 后刷新页面')
            setTimeout(
              () ->
                window.location.reload()
              , 1500
            )
      )

  # 获取页面上的 shipmentId
  shipmentId = -> $('td[shipmentId]').attr('shipmentId')

  # 获取转移数量的时候需要的值
  transformVals = (unitId) ->
    trObj = $('tr[uid=' + unitId + ']')

    tsQty: trObj.find('input').val()
    ttQty: trObj.data('ttQty')
    tr: trObj
    uid: unitId
    shipmentId: shipmentId()

  # ProcureUnit 转移为 ShipItem 的按钮
  $('#procureUnit tr[uid] button').click ->
    vals = transformVals(@getAttribute("uid"))
    if !$.isNumeric(vals['ttQty'])
      alert('总数量不为数字,非法!' + '[' + vals['ttQty'] + ']')
      return false
    if vals['tsQty'] is undefined or !$.isNumeric(vals['tsQty'])
      alert('输入的转移数量非法!')
      return false

    # 向后台发出请求, 成功后在页面上进行减少与添加
    $.post('/shipments/shipProcureUnit', {'unit.id': vals['uid'], qty: vals['tsQty'], shipmentId: vals['shipmentId']},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          trClick()
    )

  # 取消此 Shipment
  $('#shipment_cancel').click ->
    return false if !confirm("确认要取消此 Shipment?")
    maskEl = $(@).parents('table')
    maskEl.mask('取消中...')
    $.post('/shipments/cancel', {id: shipmentId()},
      (r) ->
        alert(r.message)
        maskEl.unmask()
    )

  do ->
    bindShipItemDeleBtn()
    bindConfirmShipmentBtn()
    for tr in $('#procureUnit tr[uid]')
      o = $(tr)
      total = Number(o.find('td:eq(1) a').text().trim())
      o.data('ttQty', total)

  window.$ui.init()

