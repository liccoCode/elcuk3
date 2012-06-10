$ ->
  $('#search_form :input[type=date]').dateinput({format: 'mm/dd/yyyy'})
  $('a[rel=tooltip]').tooltip({placement: 'top'})

  do_search = (o, page) ->
    $.varClosure.params = {}
    $('#search_form :input').map($.varClosure)
    now = new Date()

    switch(o.attr('day'))
      when '1', '7', '30'
        $.varClosure.params['p.from'] = $.DateUtil.fmt1($.DateUtil.addDay(Number('-' + o.attr('day')), now))
        $.varClosure.params['p.to'] = $.DateUtil.fmt1(now)
      when '-1'
        false
      else
        alert('输入的日期不合法!')
        return false
    $.varClosure.params['p.page'] = page if(page)

    order_list = $('#order_list')
    order_list.mask('查询中...')
    order_list.load('/Orders/o_search', $.varClosure.params,
      ->
        $('a[rel=tooltip]').tooltip({placement: 'top'})
        $('.pagination a[page]').click ->
          do_search(o, $(this).attr('page'))
          false
        order_list.unmask()
    )


  $('#o_search').keyup (e) ->
    do_search('a[day=30]', 1) if e.keyCode is 13

  # 搜索按钮组
  $('#search_btns a[class]').click ->
    do_search($(@), 1)

