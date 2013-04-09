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
    usdCosts = if $('#currency_label').text() == '¥'
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

  $('#shouldPaid').keyup((e) ->
    e.preventDefault()
    if e.keyCode == 13
      ajaxUpdateShouldPaid()
    else
      if $.isNumeric(@value)
        checkShouldPaidChange(@value)
      else
        @value = @value[0...-1] if e.keyCode != 8
        $('#shouldPaidChanges').removeClass('text-success').addClass('text-error').text('请输入数字')
  ).blur(-> ajaxUpdateShouldPaid())

  ajaxUpdateShouldPaid = ->
    shouldPaid = $('#shouldPaid')
    if $.isNumeric(shouldPaid.val())
      LoadMask.mask()
      $.post(shouldPaid.attr('url'), {shouldPaid: shouldPaid.val()})
        .done((r) ->
          if r.flag
            console.log('更新成功.')
          else
            alert('应付金额更新失败.[' + r.message + ']')
          LoadMask.unmask()
        )
    else
      shouldPaid.focus()
      $('#shouldPaidChanges').removeClass('text-success').addClass('text-error').text('应付金额应该为数字')

  checkShouldPaidChange = (val)->
    changes =  switch $('#currency').val()
      when 'CNY'
        parseFloat($('#costs').text()) - val
      when 'USD'
        parseFloat($('#usd_costs').text()) - val
      else
        alert('暂时只支持 USD, CNY 币种计算')
        0
    if changes > 0
      $('#shouldPaidChanges').removeClass('text-success').addClass('text-error')
    else
      $('#shouldPaidChanges').removeClass('text-error').addClass('text-success')
    $('#shouldPaidChanges').text(changes.toFixed(4))


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
