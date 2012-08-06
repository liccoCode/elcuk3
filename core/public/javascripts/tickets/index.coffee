$ ->
  for tr in $("tr.twoTr")
    $(tr).css('cursor', 'pointer').click (e) ->
      $(@).next('tr').toggle()
      e.preventDefault()
