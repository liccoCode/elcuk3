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

  $('button.delelte').click (e) ->
    return false if !confirm("确认删除?")
    btn = $(@)
    mask = $('#cooperItemList')
    mask.mask('删除中...')
    $.post('/Cooperators/removeCooperItem', {'copItem.id': btn.attr('copItemId')},
    (r) ->
      if r.flag is false
        alert(r.message)
      else
        alert('删除成功.')
        btn.parents('tr').remove()
      mask.unmask()
    )
    e.preventDefault()
