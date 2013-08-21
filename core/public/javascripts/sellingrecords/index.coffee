$ ->
  $('#selling, #sku, #category').on('ajaxLoad',() ->
    # 1. 收集 Market 的参数
    # 2. 加载数据
    $div = $(@)
    $('#post_type').val($div.attr('id'))
    LoadMask.mask()
    $div.load('/sellingrecords/sid', $('.search_form').serialize(), (r) ->
      LoadMask.unmask()
    )
  ).on('click', '.pagination a[page]', (e) ->
    $a = $(@)
    $('#post_page').val($a.attr('page'))
    $a.parents('table').parent().trigger('ajaxLoad')
    false
  )

  $('a[data-toggle=tab]').on('shown', (e) ->
    $target = $(e.target)
    $("##{$target.attr('href')[1..-1]}").trigger('ajaxLoad')
  )


  $('.search_form [name=p\\.market]').change((e) ->
    # 1. trigger #sid and set page to 1
    $('#post_page').val(1)
    type = $('#divTabs li.active a').attr('href')[1..-1]
    $("##{type}").trigger('ajaxLoad')
    false
  )


  $('a[data-toggle=tab]:contains(Selling)').tab('show')
