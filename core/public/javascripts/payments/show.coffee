$ ->
  $('.paymentUnitDeny').click (e) ->
    e.preventDefault()
    self = $(@)
    $('#deny_form').attr('action', self.attr('url'))
    $('#deny_title').text(self.parents('td').next().text())
    $('#deny_modal').modal('show')

  # 计算美金金额
  calculateUsdCosts = ->
    # 如果本身支付的币种为 USD, 则不需要计算, 直接替换即可
    usdCosts = if $('#currency').text() == '¥'
      (parseFloat($('#costs').text()) / $('#boc_rate').data('usdRate')).toFixed(4)
    else
      $('#costs').text()
    $('#usd_costs').html(usdCosts)


  $('#boc_rate_btn').click((e) ->
    e.preventDefault()
    LoadMask.mask()
    bocRate = $('#boc_rate').html('加载中...')
    bocRate.load(@getAttribute('url'), ->
      LoadMask.unmask()
      usdRate = $('#boc_rate tr:eq(3) td:eq(1)').css('color', '#D94E48').text()
      # 除以 100:  619.76 -> 6.1976
      bocRate.data('usdRate', parseFloat(usdRate) / 100)
      $('#usd_rates').html((parseFloat(usdRate) / 100).toFixed(4))
      $('#usd_datetime').text($('#boc_rate tr:eq(3) td:eq(6)').text() + " " + $('#boc_rate tr:eq(3) td:eq(7)').text())
      calculateUsdCosts()
    )
  ).click()

  # 初始化上传图片
  # 1. 首先初始化 dropbox
  # 2. Load 图片
  window.dropUpload.iniDropbox(->
    fid: $('#dropbox').attr('paymentId')
    p: 'PAYMENTS'
  , $('#dropbox'), '/payment/files/upload')
  window.dropUpload.loadImages($('#dropbox').attr('paymentId'), $('#dropbox'), 'PAYMENTS')

  do ->
    paymentUnitId = window.location.hash[1..-1]
    EF.colorAnimate("#paymentUnit_#{paymentUnitId}")
