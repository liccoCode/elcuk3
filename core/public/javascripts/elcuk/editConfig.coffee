$ ->
  $('#cigForm').on('click', '#submitBtn', (e) ->
    vals = {}
    for input in $("input[name=val]")
      $input = $(input)
      unless $input.val() is ""
        vals[$input.data('id')] = $input.val()
    $("input[name=vals]").val(JSON.stringify(vals))
    $('#cigForm').submit()
  ).on('change', "input[name=val]", (e) ->
    $input = $(@)
    if isNaN($input.val())
      noty({text: '只允许填写数字!', type: 'error', timeout: 3000})
      $input.val("")
  )