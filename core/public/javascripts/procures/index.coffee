$ ->

  # 全选按钮
  $('input:checkbox[id=checkbox_all]').change (e) ->
    $('input:checkbox[id=checkbox*][id!=checkbox_all]').prop("checked", $(@).prop("checked"))


  $('#create_deliveryment').submit (e) ->
    e.preventDefault()
    params = {}
    $('input:checked[id=checkbox*][id!=checkbox_all]').each((i) -> params["pids[#{i}]"] = $(@).val())
    mask = $('#container')
    mask.mask("创建中...")

    nameEl = $('#create_deliveryment input[name=name]')
    params['name'] = nameEl.val()

    $.post('/Deliveryments/save', params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          window.location.href = r.message
        mask.unmask()
    )

