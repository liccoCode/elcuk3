$ ->
  $("#search_Form").on('click', '#batch_create_fba_btn', (e) ->
    if getCheckedIds().length is 0
      noty({text: '请选择需要批量创建FBA的采购单元', type: 'error'})
      return false
    window.location.replace('/ShipPlans/batchCreateFBA?' + $("[name='pids'], [name^='p.']").serialize())
  )

  getCheckedIds = () ->
    ids = []
    checkboxs = $('input[name="pids"]:checked')
    for checkbox in checkboxs
      ids.push(checkbox.value)
    return ids
