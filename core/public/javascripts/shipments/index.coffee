$ ->
  $('#search_form').on('click', '.btn', (e) ->
    $('#search_form').attr('action', $(@).data('url'))
  ).on('click', '#download_excel', (e) ->
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('url'))
    $form.submit()
  )

  $(':checkbox[class=checkbox_all]').change (e) ->
    $ck = $(@)
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'))

