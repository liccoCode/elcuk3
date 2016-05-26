$ ->
  $('#deliverplan_check_all').change ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox:not(:disabled).#{region}").prop("checked", o.prop("checked"))

  $('.search_form').on('click', "a[name='triggerReceiveRecordsBtn']", (e) ->
    idCheckboxs = $('input[name="ids"]:checked')
    if idCheckboxs.size() is 0
      noty({text: '请选择一条出货单！', type: 'error'})
      return false
    else
      return unless confirm("确认发货?")
      $btn = $(@)
      form = $('<form method="post" target="_blank" action="' + $btn.data('url') + '"></form>')
      form.hide().append(idCheckboxs.clone()).appendTo('body')
      form.submit()
  )