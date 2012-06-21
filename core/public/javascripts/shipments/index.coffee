$ ->
  SHIPMENT_DETAIL = $("#shipment_detail")
  SHIPMENTS_CONTENT = $('#shipments_content')

  # 不同 Tab 中 table 的 row Click 选中样式
  fourTabClickActive = (tab, o) ->
    $.tableRowClickActive('#' + tab + ' tr[row]', o)

  # 为 peding 中的 shipments 绑定加载事件
  $('#pending tr[row]').click ->
    o = $(@)
    fourTabClickActive('pending', o)
    SHIPMENTS_CONTENT.mask("加载中...")
    SHIPMENT_DETAIL.load('/shipments/pending', id: $(@).find('td:eq(0)').html(),
      (r) ->
        SHIPMENTS_CONTENT.unmask()
    )
