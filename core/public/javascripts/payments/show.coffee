$ ->
  $('.paymentUnitDeny').click (e) ->
    e.preventDefault()
    self = $(@)
    $('#deny_form').attr('action', self.attr('url'))
    $('#deny_title').text(self.parents('td').next().text())
    $('#deny_modal').modal('show')

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
      # 619.76 -> 6.1976
      bocRate.data('usdRate', parseFloat(usdRate) / 100)
      $('#usd_rates').html((parseFloat(usdRate) / 100).toFixed(4))
      calculateUsdCosts()
    )
  ).click()