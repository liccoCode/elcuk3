$ ->
  #  $('#p_from').data('dateinput').setValue($.DateUtil.addDay(-7, new Date()))
  #  $('#p_to').data('dateinput').setValue(new Date())
  $('#order_list .sortable').click(
    ->
      $('#orderBy').val($(@).attr('name'))
      $('#search_form').submit()
  )


  $('#search_form :input[type=date]').dateinput({format: 'yyyy-MM-dd'})
  $('a[rel=tooltip]').tooltip({placement: 'top'})

  do_search = (o, page) ->
    params = $.formArrayToObj($('#search_form').formToArray())
    now = new Date()

    switch(o.attr('day'))
      when '1', '7', '30'
        params['p.from'] = $.DateUtil.fmt2($.DateUtil.addDay(Number('-' + o.attr('day')), now))
        params['p.to'] = $.DateUtil.fmt2(now)
      when '-1'
        false
      else
        alert('输入的日期不合法!')
        return false
    params['p.page'] = page if(page)

    order_list = $('#order_list')
    order_list.mask('查询中...')
    order_list.load('/Orders/search', params,
      ->
        $('a[rel=tooltip]').tooltip({placement: 'top'})
        $('.pagination a[page]').click ->
          do_search(o, $(this).attr('page'))
          false
        order_list.unmask()
    )


  # 搜索按钮组
  $('#search_btns a[class]').click ->
    do_search($(@), 1)

