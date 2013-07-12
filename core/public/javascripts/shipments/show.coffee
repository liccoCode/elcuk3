$ ->
  $('#ship_comment').click (e) ->
    e.preventDefault()
    mask = $('#container')
    mask.mask('更新 Comment')
    $.post('/shipments/comment',
    {id: $("input[name=ship\\.id]").val(), cmt: $("#ship_memo").val().trim(), track: $("[name=ship\\.trackNo]").val()},
    (r) ->
      if r.flag is false
        alert(r.message)
      else
        alert('更新成功.')
      mask.unmask()
    )

  fidCallBack = () ->
    {fid: $('#shipmentId').val(), p: 'SHIPMENT'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

  $("#split_shipment").click (e) ->
    if !confirm('确认提交?')
      e.preventDefault()
    else
      $(@).parents('form').attr('action', '/Shipments/splitShipment').submit()

  $('#create_fba').click (e) ->
    if !confirm('确认提交?')
      e.preventDefault()
    else
      $(@).button('loading').parents('form').attr('action', '/Shipments/deployToAmazon').submit()

  $('#previewBtn').click (e) ->
    shipment = $("[name='shipmentId']")
    unless shipment.val()
      EF.colorAnimate(shipment)
    else
      LoadMask.mask()
      $.getScript("/shipment/#{shipment.val()}/preview")
        .done(->
          LoadMask.unmask()
        )
        .fail(->
          LoadMask.unmask()
        )
    e.preventDefault()

  $('#adjust_shipitems').on('click', 'button.adjust', ->
    shipmentId = $("input[name='shipmentId']").val()
    if shipmentId
      $('#adjust_shipitems').attr('action', (i, v) ->
        "#{v[0...v.lastIndexOf('/')]}/#{shipmentId}"
      )
  )

  # 所有的 btnFucs 下的 button action
  $('#btnFucs').on('click', '.func', ->
    funcsForm = $('#funcsForm').find('form').attr('action', @getAttribute('url')).end()
      .find('#action').text(@textContent).end()
      .find("input[name=date]").val($.DateUtil.fmt2(new Date())).end()
      .modal('show');
  )

  $('#adjust_shipitems').on('dblclick', '[name=recivedQty]',(e) ->
    self = $(@)
    $('#origin_qty').text(self.text())
    $('#recivedQtyForm').modal('show').find('form').attr('action', "/shipitem/#{self.parents('tr').attr('id')}/recevied")
    e.stopPropagation()
  ).find('[name=recivedQty]').append('<i class="icon-wrench"></i>')

  $('#adjust_shipitems').on('click', 'button[data-url]', (e) ->
    e.preventDefault()
    $i = $(@)
    params =
      url: $i.data('url')
      select_currency: $("[name='fee.currency']")[0].outerHTML
      itm:
        id: $i.parents('tr').attr('id')
    $('#logFeeModel').html(_.template($('#form-logfee-model-template').html(), params)).modal('show')
  )

  $('#logFeeModel').on('keyup change', "[name='fee.unitPrice'],[name='fee.unitQty']", ->
    $context = $(@).parents('div')
    $context.find('.amount').val($context.find("[name='fee.unitPrice']").val() * $context.find("[name='fee.unitQty']").val())
  )

  $('#add_payment').on('click', '.btn',(e) ->
    e.preventDefault()
    $form = $('#add_payment')
    LoadMask.mask()
    $.post($form.attr('action'), $form.serialize(), (r) ->
      if r.flag == false
        errors = JSON.parse(r.message).map((err) ->
          err.message
        ).join(', ')
        $form.find('label span').html(errors).show()
      else
        $form.find('label span').html('').hide()
        $('#paymentInfo tr:last').after(_.template($('#tr-paymentunit-template').html(), {fee: r}))
        $form.trigger('reset')
      LoadMask.unmask()
    , 'json')
  ).on('keyup change', "[name='fee.unitPrice'],[name='fee.unitQty']", ->
    $context = $(@).parents('form')
    $context.find('.amount').val($context.find("[name='fee.unitPrice']").val() * $context.find("[name='fee.unitQty']").val());
  )

  $('#paymentInfo').on('click', 'button.btn-info',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    LoadMask.mask()
    $.get("/paymentunit/#{id}.json")
      .done((r) ->
        $tr.replaceWith(_.template($('#tr-edit-paymentunit-template').html(), {fee: r}))
        sessionStorage["tr-edit-paymentunit-template-#{id}"] = JSON.stringify(r)
        LoadMask.unmask()
      )
  ).on('click', 'button.btn-danger',(e) ->
    e.preventDefault()
    $tr = $(@).parents('tr')
    id = $tr.find('td:eq(0)').text().trim()
    trHtml = _.template(
      $('#tr-paymentunit-template').html(), {fee: JSON.parse(sessionStorage["tr-edit-paymentunit-template-#{id}"])}
    )
    $tr.replaceWith(trHtml)
    delete sessionStorage["tr-edit-paymentunit-template-#{id}"]
  ).on('click', 'button.btn-success', (e) ->
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
  )

