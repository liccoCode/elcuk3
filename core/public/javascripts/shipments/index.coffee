$ ->
  SHIPMENT_DETAIL = $("#shipment_detail")
  SHIPMENTS_CONTENT = $('#shipments_content')

  # 不同 Tab 中 table 的 row Click 选中样式
  fourTabClickActive = (tab, o) ->
    $.tableRowClickActive('#' + tab + ' tr[row]', o)


  ['pending', 'shipping', 'clearAndReciving', 'done'].forEach(
    (tab, i)->
      $("#" + tab + " tr[row]").click ->
        o = $(@)
        fourTabClickActive(tab, o)
        SHIPMENTS_CONTENT.mask('加载中...')
        SHIPMENT_DETAIL.load('/shipments/' + tab, id: o.find('td:eq(0)').html(),
          (r) ->
            SHIPMENTS_CONTENT.unmask()
        )
  )

