$ ->
  # 订单列表页面
  $('#order_list .sortable').click ->
    $('#orderBy').val($(@).attr('name'))
    $('#search_form').submit()

  # 订单详细页面的功能条
  $('#funcs').on('click', 'button:contains(重新抓取费用)', (e) ->
    LoadMask.mask()
    $.ajax($(@).data('url'), {dataType: 'json', type: 'POST'})
      .done((r) ->
        type = if r.flag
          "success"
        else
          "error"
        noty({text: r.message, type: type, timeout: 3000})
        LoadMask.unmask()
      )
    false
  )

