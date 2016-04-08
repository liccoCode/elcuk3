$ ->
  $('checkbox.inboundrecord_check_all').change(e) ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  $("form.search_form").on('click', 'a[name=confirm]', (e) ->
    checkids = []
    for checkbox in $('input[name="rids"]') when checkbox.checked then checkids.push(checkbox.value)
    if checkids.length is 0
      noty({text: '请选择入库记录！', type: 'error'})
      return false
    else
      $("form[name=confirm_form]").submit()
  )

  $("form.confirm_form").on('change', "input[name=qty], input[name=badQty], select[name=targetWhouse]", (e) ->
    $(@).data('has_changed', 'true')
  ).on('blur', "input[name=qty], input[name=badQty], select[name=targetWhouse]", (e) ->
    $input = $(@)
    $.post("/InboundRecords/update", {id: $input.parents('tr').find('checkbox[name=rids]').val()},
      attr: $input.attr('name'),
      value: $input.val(),
      (r) ->
        if r.flag is false
          noty({text: r.message, type: 'error'})
        else
          noty({text: "更新 #{$input.attr('name')} 成功!", type: 'success'})
    )
  )