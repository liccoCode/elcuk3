# 必须保证这个脚本在 payment_listPayment.html tag 之后加载,这样才能狗确定 #paymentInfo 存在
window.$payment = {}

# 1. 付款信息的 DIV 需要设置 id=paymentInfo
# 2. 关闭的 Button 需要有 payId 属性,并且有值

# 绑定付款的 CLOSED 按钮功能
window.$payment.bindClosePaymentBtn = () ->
  $('#paymentInfo button[payId]').unbind().click ->
    if !confirm("确定要关闭此 Payment?")
      return false
    message = prompt('请输入关闭的原因!')
    return false if message is undefined or message.trim() is ''
    o = $(@)
    mask = o.parents('tr')
    mask.mask('关闭中...')
    $.post('/payments/paymentClose', {'pay.id': o.attr('payId'), msg: message},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          o.parent().html('<i class="icon-ban-circle"></i>')
          mask.css('color', '#DDD').find('td:eq(4)').html('CLOSE')
        mask.unmask()
    )


window.$payment.renderToTable = (r) ->
  tr = "<tr>" +
  "<td>" + r['id'] + "</td><td>" + r['price'] + "</td><td>" + r['currency'] + "</td><td>" + $.DateUtil.fmt3(new Date(r['payDate'])) + "</td><td>" + r['state'] + "</td><td>" + r['memo'] + "</td>" +
  "<td><button payId=" + r['id'] + " class='btn btn-mini'><i class='icon-remove'></i></button></td>" +
  "</tr>"
  $(tr).appendTo('#paymentInfo table')
  window.$payment.bindClosePaymentBtn()

window.$payment.bindClosePaymentBtn()