$ ->
  $('.search_form').on('click', "#exportBtn", (e) ->
    $btn = $(@)
    form = $('<form method="post" action=""></form>')
    form.attr('action', $btn.data('url')).attr('target', $btn.data('target'))
    form.hide().append($btn.parents('form').find(":input").clone()).appendTo('body')
    form.submit().remove()
  )
