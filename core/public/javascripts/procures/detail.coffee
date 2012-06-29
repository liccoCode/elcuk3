$ ->
  dropbox = $('#dropbox')

  fidCallBack = () ->
    {fid: $('#deliveryId').html(), p: 'DELIVERYMENT'}
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox)
  window.dropUpload.iniDropbox(fidCallBack, dropbox)

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
          window.$payment.bindClosePaymentBtn()
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

  # 添加 memo
  $('#add_memo').click ->
    trE = $(@).parents('tr')
    memo = trE.find('textarea').val()
    trE.mask('更新中...')
    $.post('/deliveryments/comment', id: fidCallBack()['fid'], msg: memo,
      (r) ->
        if r.flag is false
          alert(r.message)
        trE.unmask()
    )

  # 取消 Delivery 的按钮
  $('#cancel_btn').click ->
    return false if !confirm("确认要取消这个采购单吗? 取消后所有 ProcureUnit 的数量都变为0!")
    $.post('/deliveryments/cancel', id: fidCallBack()['fid'],
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert("删除成功.")
    )

  window.$ui.htmlIni()
