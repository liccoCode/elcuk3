$ ->
  $(document).on('keyup change', "[name='fee.unitPrice'],[name='fee.unitQty']", ->
    $context = $(@).parents('div[class!=controls][class!=control-group]:eq(0)')
    $context.find('.amount').val($context.find("[name='fee.unitPrice']").val() * $context.find("[name='fee.unitQty']").val())
  )
  $('#popModal').on('click', '.btn:contains(删除)', (e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $btn.parents('form')
    $.ajax({
      url: $form.attr('action'),
      type: 'DELETE',
      data: $form.serialize()
    }).done((r) ->
      if r.flag == true
        $("#fee_#{$btn.data('id')}").remove()
        noty({text: r.message, type: 'success', timeout: 3000})
      else
        text = _.map(r,(err)->
          err.message
        ).join('<br>')
        noty({text: text, type: 'error'})
      $('#popModal').modal('hide')
    )
  )

  $('form.add_payment').on('click', '.btn', (e) ->
    e.preventDefault()
    $form = $(@).parents('form')
    LoadMask.mask()
    $.post($(@).data('url'), $form.serialize(), (r) ->
      if r.flag == false
        errors = JSON.parse(r.message).map((err) ->
          err.message
        ).join(', ')
        $form.find('label span').html(errors).show()
      else
        $form.find('label span').html('').hide()
        $form.parents('div.top').find('.paymentInfo tr:last').after(_.template($('#tr-paymentunit-template').html(),
        {fee: r}))
        noty({text: "成功添加 #{r['currency']} #{r['amount']} #{r.feeType.nickName}", type: 'success', timeout: 3000})
        $form.trigger('reset')
      LoadMask.unmask()
    , 'json')
  )

  # 请款信息相关的功能
  $('table.paymentInfo').on('click', 'table button.btn-info',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    LoadMask.mask()
    $.get("/paymentunit/#{id}.json")
      .done((r) ->
        trDom = $(_.template($('#tr-edit-paymentunit-template').html(), {fee: r}))
          .find("[name='fee.currency']").val(r.currency).end()
        $tr.replaceWith(trDom)
        sessionStorage["tr-edit-paymentunit-template-#{id}"] = JSON.stringify(r)
        LoadMask.unmask()
      )

    # 取消更新
  ).on('click', 'button.btn-danger',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    trHtml = _.template(
      $('#tr-paymentunit-template').html(), {fee: JSON.parse(sessionStorage["tr-edit-paymentunit-template-#{id}"])}
    )
    $tr.replaceWith(trHtml)
    delete sessionStorage["tr-edit-paymentunit-template-#{id}"]

    # 更新
  ).on('click', 'button.btn-success',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    $.ajax({
      url: "/paymentunit/#{id}.json",
      type: 'PUT',
      data: $tr.find(':input').serialize()
    }).done((r) ->
      if r.flag is false
        noty({text: r.message, type: 'warning'})
      else
        $tr.replaceWith(_.template($('#tr-paymentunit-template').html(), {fee: r}))
        noty({text: '更新成功', type: 'success', timeout: 3000})
    ).fail((r) ->
      noty({text: '服务器发生错误!', type: 'error', timeout: 5000})
    )
  ).on('click', 'button.btn-warning', (e) ->
    id = $(@).parents('tr').find('td:eq(0)').text().trim();
    params =
      id: id
      url: "/paymentunit/#{id}/shipment",
    $('#popModal').html(_.template($('#form-destroyfee-model-template').html(), {fee: params})).modal('show')
  )