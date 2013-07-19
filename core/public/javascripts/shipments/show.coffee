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

  $('#previewBtn')

  # 所有的 btnFucs 下的 button action
  $('#btnFucs').on('click', '.func', ->
    funcsForm = $('#funcsForm').find('form').attr('action', @getAttribute('url')).end()
      .find('#action').text(@textContent).end()
      .find("input[name=date]").val($.DateUtil.fmt2(new Date())).end()
      .modal('show');
  )

  $('#adjust_shipitems').on('keyup change', 'input.logged,input.unlogged',(e) ->
    if e.which == 13
      $input = $(@)
      LoadMask.mask()
      $.ajax($input.parents('tr').data('weight-url'), {type: 'PUT', dataType: 'json', data: {wt: $input.val()}})
        .done((r) ->
          if r.flag == false
            noty({text: r.message, type: 'error'})
          else
            noty({text: "运输项目 ##{r.id} 重量记录成功", type: 'success', timeout: 3000})
            $input.removeClass('unlogged').addClass('logged') if r.weight > 0
          LoadMask.unmask()
        )
      console.log "input.logged #{e.which}"
    false
  ).on('click', '.btn.adjust',->
    shipmentId = $("input[name='shipmentId']").val()
    if shipmentId
      $('#adjust_shipitems').attr('action',(i, v) ->
        "#{v[0...v.lastIndexOf('/')]}/#{shipmentId}"
      ).submit()
    false
  ).on('click', '.btn.preview',(e) ->
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
    false
  ).on('click', '.btn[data-url]:contains(L)',(e) ->
    $i = $(@)
    params =
      url: $i.data('url')
      select_currency: $("[name='fee.currency']")[0].outerHTML
      itm:
        id: $i.parents('tr').attr('id')
    $('#popLogModel').html(_.template($('#form-logfee-model-template').html(), params)).modal('show')
    false
  ).on('dblclick', '[name=recivedQty]', (e) ->
    self = $(@)
    params =
      url: self.parents('tr').data('received-url')
      qty: self.text()
    $('#popLogModel').html(_.template($('#form-logreceive-qty-model-template').html(), params)).modal('show')
    false
  )

