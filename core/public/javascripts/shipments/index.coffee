$ ->
  SHIPMENT_DETAIL = $("#shipment_detail")
  SHIPMENTS_CONTENT = $('#shipments_content')

  # 不同 Tab 中 table 的 row Click 选中样式
  fourTabClickActive = (tab, o) ->
    $.tableRowClickActive('#' + tab + ' tr[row]', o)

  bindPaymentBtn = (remove = true) ->
  # 付款按钮
    $('#pay_for_the_Obj').click ->
      payment = $('#payment')
      $.varClosure.params = {}
      payment.find(':input').map($.varClosure)
      payment.mask('更新中...')
      $.post('/shipments/payment', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            window.$payment.renderToTable(r)
          payment.unmask()
      )
    $('#payment_clear').remove() if remove is true

  ['pending', 'shipping', 'clearAndReciving', 'done'].forEach(
    (tab, i)->
      $("#" + tab + " tr[row]").click ->
        o = $(@)
        fourTabClickActive(tab, o)
        SHIPMENTS_CONTENT.mask('加载中...')
        SHIPMENT_DETAIL.load('/shipments/' + tab, id: o.find('td:eq(0)').html(),
          (r) ->
            bindPaymentBtn()
            SHIPMENTS_CONTENT.unmask()
        )
  )

