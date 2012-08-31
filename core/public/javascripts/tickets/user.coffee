$ ->
  $("form :checkbox").click (e) ->
    $('form :checkbox').prop('checked', false)
    $(@).prop('checked', true)
