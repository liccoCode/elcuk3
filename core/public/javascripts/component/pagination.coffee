$ ->
  # 需要借助 Post 进行(post请求)分页的功能, 都需要引入这个 script
  $('.pagination a[page]').click (e) ->
    e.preventDefault()
    $('[name=p\\.page]').val($(@).attr('page')).parents('form').submit()

  $('.pagination select').change((e)->
    e.preventDefault()
    $('[name=p\\.page]').val($(@).val()).parents('form').submit()
  )
