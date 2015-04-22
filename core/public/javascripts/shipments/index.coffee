$ ->
  $('#search_form').on('click', '.btn', (e) ->
    $('#search_form').attr('action', $(@).data('url'))
  )

  $('#all_check').click (e) ->
    e.preventDefault()
    $("table [type='checkbox']").each(->
      $(@).attr('checked', 'checked')
    )

  $('#un_check').click (e) ->
    e.preventDefault()
    $("table [type='checkbox']").each(->
      $(@).attr('checked', !$(@).attr('checked'))
    )