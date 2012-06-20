$ ->
  dropbox = $('#dropbox')
  message = $('#dropbox .message')
  uploaded = $('#uploaded')

  fidCallBack = () ->
    {fid: $('#deliveryId').html(), p: 'DELIVERYMENT'}
  window.dropUpload.loadImages(fidCallBack()['fid'], message, uploaded)
  window.dropUpload.iniDropbox(fidCallBack, dropbox, message, uploaded)

  # 绑定付款的 CLOSED 按钮功能
  bindClosePaymentBtn = () ->
    $('#paymentInfo button[payId]').unbind().click ->
      if !confirm("确定要关闭此 Payment?")
        return false
      message = prompt('请输入关闭的原因!')
      return false if message is undefined or message.trim() is ''
      o = $(@)
      mask = o.parents('tr')
      mask.mask('关闭中...')
      $.post('/deliveryments/paymentClose', {'pay.id': o.attr('payId'), msg: message},
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            o.parent().html('<i class="icon-ban-circle"></i>')
            mask.css('color', '#DDD').find('td:eq(4)').html('CLOSE')
          mask.unmask()
      )

  bindClosePaymentBtn()

  # 付款按钮
  $('#pay_for_the_deliveryment').click ->
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
          $(tr).appendTo('#paymentInfo')
          bindClosePaymentBtn()
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

