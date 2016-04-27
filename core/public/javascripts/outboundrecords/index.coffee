$ ->
  $('#outboundrecord_check_all').change ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox:not(:disabled).#{region}").prop("checked", o.prop("checked"))

  $("form.search_form").on('click', 'a[name=confirm]', (e) ->
    checkids = []
    for checkbox in $('input[name="rids"]') when checkbox.checked then checkids.push(checkbox.value)
    if checkids.length is 0
      noty({text: '请选择出库记录！', type: 'error'})
      return false
    else
      $("form[name=confirm_form]").submit()
  )

  $("form[name=confirm_form]").on('change', "input[name=qty], input[name=memo], select[name=whouse],
 select[name=targetId]", (e) ->
    $input = $(@)
    value = $input.val()
    return if value == null || value == undefined || value == ""

    $.post("/OutboundRecords/update", {
      id: $input.parents('tr').find('input:checkbox[name=rids]').val(),
      attr: $input.attr('name'),
      value: value
    },
      (r) ->
        if r.flag is false
          noty({text: r.message, type: 'error'})
        else
          noty({text: "更新 #{$input.attr('name')} 成功!", type: 'success'})
    )
  ).on('disabledInput', "table", (e) ->
    _.each($(@).find("tr"), (tr) ->
      $tr = $(tr)
      state = $tr.find('input[name=state]').val()
      if state != 'Pending'
        _.each($tr.find(":input[name]"), (input) ->
          $(input).parent().text($(input).val())
        )
    )
  )

  $("form[name=confirm_form] table").trigger("disabledInput")

