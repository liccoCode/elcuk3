$ ->
  $(document).on('click', '#reflush_trafficrate, #download_excel', (e) ->
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    $form.submit()
  )
