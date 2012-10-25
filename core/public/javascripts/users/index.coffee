$ ->
  $('.privilege_form').ajaxForm({
  dataType: 'json',
  success: (r) ->
    alert(r.message)
  })
