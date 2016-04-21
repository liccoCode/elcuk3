$ ->
  $sku = $("#inputsku")
  $sku.typeahead({
    source: (query, process) ->
      sku = $sku.val()
      $.get('/products/sameSku', {sku: sku})
      .done((c) ->
        process(c)
      )
  })


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

  $('#downExcel').click((e)->
    e.preventDefault()
    from = new Date($("#p_from").val())
    to = new Date($("#p_to").val())
    if (to - from)/ 1000 / 60 / 60 / 24 != 0
      alert "只能导出一天之内的数据！"
    else
      $form = $("#search_form")
      window.open('/Excels/orderReports?' + $form.serialize(), "_blank")

  )

