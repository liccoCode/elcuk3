$ ->
  $('#ship_comment').click (e) ->
    e.preventDefault()
    o = $(@)
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
        $('#paymentInfo tr:last').after(_.template(unitTrTemplate, {fee: r}))
      LoadMask.unmask()
    , 'json')
  ).on('keyup change', "[name='fee.unitPrice'],[name='fee.unitQty']", ->
    $('#amount').val($("[name='fee.unitPrice']").val() * $("[name='fee.unitQty']").val());
  )


unitTrTemplate = '<tr>
    <td><%= fee.id %></td>
    <td><%= fee.feeType.nickName %></td>
    <td><%= fee.currency %></td>
    <td><%= fee.unitPrice %></td>
    <td><%= fee.unitQty %></td>
    <td><%= fee.amount %></td>
    <td><%= fee.memo %></td>
    <td><%= fee.username %></td>
  </tr>'
