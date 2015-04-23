$ ->
  $('#search_form').on('click', '.btn', (e) ->
    $('#search_form').attr('action', $(@).data('url'))
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    o = $(@)
    o.parents('table').find(':checkbox').not(':first').prop("checked", o.prop('checked'))
