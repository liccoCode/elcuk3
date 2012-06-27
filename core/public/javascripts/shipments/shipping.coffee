$ ->
# 绑定更新国际快递信息的按钮
  bindIExpressHTMLRefreshBtn = ->
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

  do ->
    bindIExpressHTMLRefreshBtn()

  dropBox = $('#dropbox')

  fidCallBack = () ->
    fid: $('#shipment_detail td[shipmentid]').html().trim()
    p: 'SHIPMENT'

  window.dropUpload.loadImages(fidCallBack()['fid'], dropBox)
  window.dropUpload.iniDropbox(fidCallBack, dropBox)

  window.popover()


