$ ->
  $("#plan_update_btn").click (e) ->
    $form = $("#plan_form")
    LoadMask.mask()
    $.ajax('/deliverplans/update', {type: 'GET', data: $form.serialize(), dataType: 'json'})
    .done((r) ->
        msg = if r.flag is true
          {text: "保存成功.", type: 'success', timeout: 5000}
        else
          {text: "#{r.message}", type: 'error', timeout: 5000}
        noty(msg)
        LoadMask.unmask()
      )