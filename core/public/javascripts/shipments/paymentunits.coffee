$ ->
  feeStateLabel = (state) ->
    label = if state == 'APPLY'
      'inverse'
    else if state == 'DENY'
      'danger'
    else if state == 'APPROVAL'
      'info'
    else if state == 'PAID'
      'success'

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
        text = JSON.parse(r.message).map((err) ->
          err.message
        ).join(', ')
        noty({text: text, type: 'error'})
      else
        label = feeStateLabel(r['state'])
        $form.parents('div.top').find('.paymentInfo tr:last')
          .after(_.template($('#tr-paymentunit-template').html(), {fee: r, label: label}))
        noty({text: "成功添加 #{r['currency']} #{r['amount']} #{r.feeType.nickName}", type: 'success', timeout: 3000})
        $form.trigger('reset')
      LoadMask.unmask()
    , 'json')
  )

  # 请款信息相关的功能
  # 编辑
  $('table.paymentInfo').on('click', 'button.btn-info:contains(编辑)',(e) ->
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

    # 取消编辑
  ).on('click', 'button.btn-danger:contains(取消)',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    fee = JSON.parse(sessionStorage["tr-edit-paymentunit-template-#{id}"])
    label = feeStateLabel(fee['state'])
    trHtml = _.template(
      $('#tr-paymentunit-template').html(), {fee: fee, label: label}
    )
    $tr.replaceWith(trHtml)
    delete sessionStorage["tr-edit-paymentunit-template-#{id}"]

    # 更新
  ).on('click', 'button.btn-success:contains(更新)',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    LoadMask.mask()
    $.ajax({
      url: "/paymentunit/#{id}.json",
      type: 'PUT',
      data: $tr.find(':input').serialize()
    }).done((r) ->
      if r.flag is false
        noty({text: r.message, type: 'warning'})
      else
        label = feeStateLabel(r['state'])
        $tr.replaceWith(_.template($('#tr-paymentunit-template').html(), {fee: r, label: label}))
        noty({text: '更新成功', type: 'success', timeout: 3000})
      LoadMask.unmask()
    ).fail((r) ->
      noty({text: '服务器发生错误!', type: 'error', timeout: 5000})
      LoadMask.unmask()
    )

    # 删除
  ).on('click', 'button.btn-warning:contains(删除)',(e) ->
    id = $(@).parents('tr').find('td:eq(0)').text().trim();
    params =
      id: id
      url: "/paymentunit/#{id}/shipment",
    $('#popModal').html(_.template($('#form-destroyfee-model-template').html(), {fee: params})).modal('show')

    # 批准
  ).on('click', 'button.btn-success:contains(批准)',(e) ->
    $btn = $(@)
    $tr = $btn.parents('tr')
    id = $tr.find('td:eq(0)').text().trim();
    LoadMask.mask()
    $.ajax("/paymentunit/#{id}/approve.json", {type: 'POST', dataType: 'json'})
      .done((r) ->
        if r.flag == false
          try
            text = _.map(JSON.parse(r.message),(err)->
              err.message
            ).join('<br>')
          catch e
            text = r.message
          noty({text: text, type: 'error'})
        else
          noty({text: "请款项目 ##{r['id']} 通过审核.", type: 'success', timeout: 3000})
          label = feeStateLabel(r['state'])
          nickName = $tr.find('td:eq(1)').text()
          $tr.find('td:eq(1)').html("<span class='label label-#{label}'>#{nickName}</span>")
        LoadMask.unmask()
      )
      .fail((r) ->
        noty({text: '服务器发生错误!', type: 'error', timeout: 5000})
        LoadMask.unmask()
      )
    false
  ).on('mouseenter', 'td:has(.icon-search)',(e) ->
    $td = $(@)
    if $td.data('shipitemid')
      if $td.data('shipItem')
        text = _.template($('#shipItem-template').html(), {itm: $td.data('shipItem')})
        $td.popover({content: text, container: 'body', trigger: 'click', placement: 'top', html: true})
      else
        $.ajax("/shipitem/#{$td.data('shipitemid')}.json", {dataType: 'json', type: 'GET'})
          .done((r) ->
            $td.data('shipItem', r)
            text = _.template($('#shipItem-template').html(), {itm: r})
            $td.popover({content: text, container: 'body', trigger: 'click', placement: 'top', html: true})
          )
    false
  ).on('mouseleave', 'td:has(.icon-search)', (e) ->
#    $(@).popover('hide');
    false
  )
