$ ->
  currencyMap = {
    CNY: '人民币',
    EUR: '欧元',
    GBP: '英镑',
    HKD: '港币',
    USD: '美元'
  }

  $('#pay_form').on('change', 'select[name=currency]',(e) ->
    $slt = $(@)
    if $slt.val()
      updateMainInfo($slt.val())
    else
      $('#ratioInput').val(-1)
      $('#ratioDateTimeInput').val('')
    false
  ).on('change', 'select[name=paymentTargetId]', (e) ->
    $slt = $(@)
    text = $slt.find(':selected').text()
    name = text.split(']')[1]
    [username, account] = text.split(']')[0][1..-1].split(' ')
    $('#paymentTargetInfo').html(
      "账户: #{username}<br>账号: #{account}<br>名称: #{name}"
    )
    false
  )

  updateMainInfo = (target) ->
    $mainInfo = $('#mainInfo')
    ratio = extraRatio(target)
    # 汇率
    $('#currencyFromTo').text($('#request_currency').text() + " -> " + target)
    $('#ratioInfo').text(ratio)

    # 需要支付的 币种/金额
    $('#paidCurrency').text(target)
    $('#paidCurrencyAmount').text(($('#finalAppied').text() * ratio).toFixed(4))

    # 汇率时间
    $('#ratioTime').text($.DateUtil.fmt3(extraRatioTime(target)))

    $mainInfo.data('ratio', ratio)

  # 抽取两个挂牌价中的汇率
  extraRatio = (target) ->
    ratio = if 'CNY' == target
      tr = $("#boc_rate tr td:contains(#{currencyMap[$('#request_currency').text()]})").parents('tr')
      (parseFloat(tr.find('td:eq(1)').css('color', 'red').text()) / 100).toFixed(4)
    else
      tr = $("#ex_rate tr td:contains(#{target})").parents('tr')
      (parseFloat(tr.find('td:eq(2)').css('color', 'red').text())).toFixed(4)
    $('#ratioInput').val(ratio)
    ratio

  # 抽取挂牌价时间
  extraRatioTime = (target) ->
    date = if 'CNY' == target
      tr = $('#boc_rate tr:eq(1)')
      new Date("#{tr.find('td:eq(6)').text()} #{tr.find('td:eq(7)').text()}")
    else
      new Date()
    $('#ratioDateTimeInput').val($.DateUtil.fmt3(date))
    date

  ajaxBocRate = ->
    LoadMask.mask()
    $('#boc_rate').load('/payment/boc', ->
      LoadMask.unmask()
    )

  ajaxExRate = (from = $('#request_currency').text()) ->
    LoadMask.mask()
    $('#ex_rate').load("/payment/xe?currency=#{from}", ->
      LoadMask.unmask()
    )

  ajaxExRate()
  ajaxBocRate()

  # 初始化上传图片
  # 1. 首先初始化 dropbox
  # 2. Load 图片
  fid = ->
    fid: $('#dropbox').attr('paymentId')
    p: 'PAYMENTS'

  window.dropUpload.iniDropbox(fid, $('#dropbox'), '/payment/files/upload')
  window.dropUpload.loadImages($('#dropbox').attr('paymentId'), $('#dropbox'), 'PAYMENTS')

  do ->
    paymentUnitId = window.location.hash[1..-1]
    EF.colorAnimate("#paymentUnit_#{paymentUnitId}")
