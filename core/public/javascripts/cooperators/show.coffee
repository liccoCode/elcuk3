$ ->
  $("#cooperators_home").on("click", "#cop_update_basicinfo, #cop_update_qcinfo", (e) ->
    form = $(@).parents('form')
    e.preventDefault()
    form.mask('更新中...')
    $.post('/Cooperators/edit', form.serialize(), (r) ->
      if r.flag is false
        noty({
          text: r.message,
          type: 'error'
        });
      else
        noty({
          text: '更新成功.',
          type: 'success'
        });
      form.unmask()
    )
  )

  $('button.delelte').click (e) ->
    return false if !confirm("确认删除?")
    btn = $(@)
    mask = $('#cooperItemList')
    mask.mask('删除中...')
    $.post('/Cooperators/removeCooperItem', {'copItem.id': btn.attr('copItemId')},
      (r) ->
        if r.flag is false
          noty({
            text: r.message,
            type: 'error'
          });
        else
          noty({
            text: '删除成功.',
            type: 'success'
          });
          btn.parents('tr').remove()
        mask.unmask()
    )
    e.preventDefault()
