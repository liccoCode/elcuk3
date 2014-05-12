$ ->
  $('#create_selling_btn').click (e) ->
    $from = $('#new_selling')
    $from.mask('创建 Selling')
    $.ajax($from.attr('action'), {type: 'POST', data: $from.serialize()})
    .done((r) ->
        message = if r.flag is true
          {text: "添加成功，SellingId：#{r.message} ", type: 'success', timeout: 5000}
        else
          {text: "添加失败： #{r.message}", type: 'error', timeout: 5000}
        noty(message)
        $from.unmask()
      )