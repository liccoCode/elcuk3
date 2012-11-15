$ ->
  $('#ship_comment').click (e) ->
    e.preventDefault()
    o = $(@)
    mask = $('#container')
    mask.mask('更新 Comment')
    $.post('/shipments/comment', {id: $("input[name=ship\\.id]").val(), cmt: $("#ship_memo").val().trim()},
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
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, 'span1')
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
