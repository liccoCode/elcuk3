$ ->

# 获取页面上的 shipmentId
  fidCallBack = () ->
    fid: $('#shipment_detail td[shipmentid]').html().trim()
    p: 'SHIPMENT'

  # 绑定更新国际快递信息的按钮
  $("#alter button").click ->
    alertE = $("#alter")
    spid = @getAttribute('shipmentId')
    alertE.mask('更新中...')
    $.post('/shipments/refreshIExpress', id: spid,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          $('#expressHtml').html(r.message)
        alertE.unmask()
    )

  # 修改 memo 按钮
  $('#add_memo').click ->
    trEl = $(@).parents('tr')
    memo = trEl.find('textarea').val()
    trEl.mask('更新中...')
    $.post('/shipments/editMemo', id: fidCallBack()['fid'], memo: memo,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('更新成功.')
        trEl.unmask()
    )

  # 运输完成
  $('#shipping_done').click ->
    tableE = $(@).parents('table')
    tableE.mask('更新中...')
    $.post('/shipments/makeDone', id: fidCallBack()['fid'],
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('更新成功 2s 内刷新')
          setTimeout(
            () ->
              window.location.reload()
            , 1500
          )
        tableE.unmask()
    )

  dropBox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropBox)
  window.dropUpload.iniDropbox(fidCallBack, dropBox)

  window.popover()


