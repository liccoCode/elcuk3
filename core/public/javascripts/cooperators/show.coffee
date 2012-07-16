$ ->
  $('#cop_update').click (e) ->
    form = $('#cooperator')
    e.preventDefault() if form.valid() is false
    $.post('edit', form.formSerialize(),
      (r) ->
        alert(JSON.stringify(r))
    )
