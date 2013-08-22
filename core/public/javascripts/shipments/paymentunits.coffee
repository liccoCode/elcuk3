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

  $(document).on('keyup change', "[name='fee.unitPrice'],[name='fee.unitQty']",->
    $context = $(@).parents('div[class!=controls][class!=control-group]:eq(0)')
    $context.find('.amount').val($context.find("[name='fee.unitPrice']").val() * $context.find("[name='fee.unitQty']").val())
  ).on('click', 'form .deny-paymentunit', (e) ->
    e.preventDefault()
    $form = $(@).parents('form')

    $('#popModal').modal('hide')
    LoadMask.mask()
    $.ajax($form.attr('action'), {type: 'POST', dataType: 'json', data: $form.serialize()})
      .done((r) ->
        if r.flag is true
          $("#fee_#{$form.data('feeid')}").find('td:eq(1) span').attr('class', 'label label-important')
          noty({text: r.message, type: 'success', timeout: 3000})
        else
          text = _.map(r,(err)->
            err.message
          ).join('<br>')
          noty({text: text, type: 'error'})

        LoadMask.unmask()
        false
      )
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
        #计算各币种的总费用
        id = $btn.data('data-id');
        $("table.paymentInfo tr[id=fee_#{id}]").parents('table').trigger("statistic_data");
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
      #计算各币种的总费用
      $(@).parents("table").trigger("statistic_data");
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
  ).on('click', 'button.btn-danger:contains(驳回)',(e) ->
    $btn = $(@)
    $tr = $btn.parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    formParam =
      url: "/paymentunit/#{id}/deny"
      title: "驳回 #{$btn.parents('tr').find('td:eq(1)').text()} 请款项目"
      id: id

    $('#popModal')
      .html(_.template($('#form-deny-paymentunit-template').html(), {form: formParam}))
      .modal('show')
  ).on('mouseenter', 'td:has(.icon-search)', (e) ->
    $td = $(@)
    if $td.data('shipitemid')
      if $td.data('shipItem')
        text = _.template($('#shipItem-template').html(), {itm: $td.data('shipItem')})
        $td.popover({content: text, container: 'body', trigger: 'click', placement: 'top', html: true})
      else
        LoadMask.mask()
        $.ajax("/shipitem/#{$td.data('shipitemid')}.json", {dataType: 'json', type: 'GET'})
          .done((r) ->
            $td.data('shipItem', r)
            text = _.template($('#shipItem-template').html(), {itm: r})
            $td.popover({content: text, container: 'body', trigger: 'click', placement: 'top', html: true})
            LoadMask.unmask()
          )
    false
  )

$ ->
  #为运输单费用信息的TABLE，增加计算方法
  $('table.paymentInfo').on('statistic_data', (e) ->
    e.preventDefault()
    $table = $(@)
    #删除生成的TR 防止统计错误
    if $table.find("tr:last").attr("id") == "_show_amount"
      $table.find("tr:last").remove();
      $table.find("tr:last").remove();
    #根据币种的不同 统计总金额
    amountMap = {}
    $table.find('tr:gt(0)').each((index, element)->
      tr_node = $(element)
      #获取是否有子元素 有子元素证明该行变成了编辑状态
      currency_children = tr_node.find("td:eq(2)").children(":first").length;
      if currency_children == 0
        currency = tr_node.find("td:eq(2)").text();
        total = tr_node.find("td:eq(6)").text().trim().substr(1)
      else
        currency = tr_node.find("td:eq(2)").children(":first").val();
        total = tr_node.find("td:eq(6)").children(":first").val();

      if amountMap[currency] == undefined
        amountMap[currency] = total;
      else
        amountMap[currency] = parseFloat(amountMap[currency]) + parseFloat(total);
    )
    #展示 统计结果
    $table.find("tr:last-child").after('<tr class="alert alert-success" style="text-align:left"><td colspan="12"><h4>运输单费用统计</h4></td></tr>');
    message = ""
    message += "&nbsp;&nbsp; #{key} : #{value}" for key, value of amountMap
    $table.find("tr:last-child").after("<tr id='_show_amount'><td colspan='11'>#{message}</td></tr>")
  )

  #计算页面所有运输单费用信息的各种币种的总和
  $('table.paymentInfo').trigger("statistic_data")
