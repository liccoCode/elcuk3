$ ->
  $("#unit_list").on('change', "select[name='customsClearanceTypeSetter']", (e) ->
    $.ajax('/procureUnits/updateAttr', {
      type: 'GET',
      data: {id: $(@).data('unitid'), attr: 'ClearanceType', value: $(@).val()},
      dataType: 'json'
    })
    .done((r) ->
      msg = if r.flag is true
        {text: r.message, type: 'success', timeout: 5000}
      else
        {text: "#{r.message}", type: 'error', timeout: 5000}
      noty(msg)
    )
  )