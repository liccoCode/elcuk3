$ ->
  $('#sid').on('loadSid',() ->
    # 1. 收集 Market 的参数
    # 2. 加载数据
    LoadMask.mask()
    $(@).load('/sellingrecords/sid', $('.search_form').serialize(), (r) ->
      LoadMask.unmask()
    )
  ).on('click', '.pagination a[page]',(e) ->
    $('#post_page').val($(@).attr('page'))
    $('#sid').trigger('loadSid')
    false
  ).trigger('loadSid')

  $('.search_form [name=p\\.market]').change((e) ->
    # 1. trigger #sid and set page to 1
    $('#post_page').val(1)
    $('#sid').trigger('loadSid')
    false
  )
