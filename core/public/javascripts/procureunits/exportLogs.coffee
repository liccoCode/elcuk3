$ ->

  $('.search_form').on('click', 'a', (e) ->
    $btn = $(@)
    $form = $('.search_form')
    if $btn.data('href').indexOf('excels') >= 0
      $form.attr('target', '_blank').attr('action', $btn.data('href'))
    else
      $form.attr('action', $btn.data('href'))
    $form.submit()
  )