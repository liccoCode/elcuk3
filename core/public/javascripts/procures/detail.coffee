$ ->
  dropbox = $('#dropbox')
  message = $('#dropbox .message')
  uploaded = $('#uploaded')

  fidCallBack = () ->
    {fid: $('#deliveryId').html(), p: 'DELIVERYMENT'}
  window.dropUpload.loadImages(fidCallBack()['fid'], message, uploaded)
  window.dropUpload.iniDropbox(fidCallBack, dropbox, message, uploaded)

  # 付款按钮
  $('#pay_for_the_Obj').click ->
    $.varClosure.params = {}
    $('#payment :input').map($.varClosure)
    if !$.isNumeric($.varClosure.params['pay.price'])
      alert("付款价格只能为数字")
      return false
    payment = $('#payment')
    payment.mask('更新中...')
    $.post('/deliveryments/payment', $.varClosure.params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          $('a[href=#payment]').click()
          tr = "<tr>" +
          "<td>" + r['id'] + "</td><td>" + r['price'] + "</td><td>" + r['currency'] + "</td><td>" + $.DateUtil.fmt3(new Date(r['payDate'])) + "</td><td>" + r['state'] + "</td><td>" + r['memo'] + "</td>" +
          "<td><button payId=" + r['id'] + " class='btn btn-mini'><i class='icon-remove'></i></button></td>" +
          "</tr>"
          $(tr).appendTo('#paymentInfo table')
          window.payment.bindClosePaymentBtn()
        payment.unmask()
    )


  # 此采购单清理付清全款按钮
  $('#payment_clear').click ->
    mask = $(@).parents('tr')
    mask.mask('检查中...')
    $.post('/deliveryments/paymentComplate', 'dlmt.id': fidCallBack()['fid'],
      (r) ->
        if r.flag is false
          alert(r.message)
          mask.unmask()
        else
          alert('付款完成, 3s 跳转')
          setTimeout(
            ->
              mask.unmask()
              window.location.reload()
            , 4000)
    )

