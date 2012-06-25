$ ->
  initDate = do ->
    for tr in $('#procureUnit tr[uid]')
      o = $(tr)
      total = Number(o.find('td:eq(1)').html())
      o.data('ttQty', total)

  window.popover()

  # 获取转移数量的时候需要的值
  transformVals = (unitId) ->
    trObj = $('tr[uid=' + unitId + ']')

    tsQty: trObj.find('input').val()
    ttQty: trObj.data('ttQty')
    tr: trObj
    uid: unitId
    shipmentId: $('td[shipmentId]').attr('shipmentId')

  # 转移成功后页面的改变动作
  transformSucc = (r, vals) ->
  # 1. ProcureUnit Table 的数量改变. 如果等于最大值则变灰色
  # 2. Shipment 的列表增加. 如果页面上有相同 shipitem.id 的, 删除原来的
    if r['qty'] is vals['ttQty']
    #删除
      vals['tr'].css('background', '#eee')
    else
      vals['tr'].find('td:eq(1)').html(vals['ttQty'] + '(' + (Number(vals['ttQty']) - r['qty']) + ')')
    #                <tr>
    #                    <td>
    #                        <a href="@{Products.p_detail(t3._1)}" target="_blank">${t3._1}</a>|<a href="@{Sellings.selling(itm.unit.sid)}" target="_blank">${t3._2}
    #                        |${t3._3}</a></td>
    #                    <td>${itm.qty}</td>
    #                </tr>
    for item in $('#shipitem tr[shipitem]')
      $(item).remove() if item.getAttribute('shipitem') is r['id'] + ""

    sku = r['unit']['sku']
    sid_p2 = r['unit']['sid'].split('|')[1..2]
    trHtml = "<tr shipitem='" + r['id'] + "'><td><a href='/products/p_detail?sku=" + sku + "' target='_blank'>" + sku + "</a>|<a href='/sellings/selling?sid=" + r['unit']['sid'] + "' target='_blank'>" + sid_p2 + "</a></td>"
    trHtml += "<td>" + r['qty'] + "</td></tr>"
    $(trHtml).appendTo('#shipitem table')


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
          transformSucc(r, vals)
    )
