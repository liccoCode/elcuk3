$ ->
  for tr in $("tr.twoTr")
    $(tr).css('cursor', 'pointer').click (e) ->
      $(@).next('tr').toggle('fast')
      e.preventDefault()
