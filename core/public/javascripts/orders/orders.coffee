$ ->
  $('#order_list .sortable').click ->
    $('#orderBy').val($(@).attr('name'))
    $('#search_form').submit()

  $('#fix_warnning_orders').click (e)->
    e.preventDefault()
    params =
      from: $('#p_from').val()
      to: $('#p_to').val()
      market: $('#o_market option:selected').val()
      return unless confirm("From: #{params.from} To: #{params.to} Market: #{params.market}
                确认要处理吗? 如果没有处理成功更新订单还会处理重复回来.")
    if params.market == ''
      alert('请至少选择一个市场')
      return
    window.location = "/orders/warnfix?#{$.param(params)}"


