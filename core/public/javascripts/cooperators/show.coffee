$ ->
  $('#cop_update').click (e) ->
    form = $('#cooperator')
    e.preventDefault() if form.valid() is false
    form.mask('更新中...')
    $.post('/Cooperators/edit', form.formSerialize(),
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('更新成功.')
        form.unmask()
    )
