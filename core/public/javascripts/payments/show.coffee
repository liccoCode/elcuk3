$ ->
  currencyMap = {
    CNY: '人民币',
    EUR: '欧元',
    GBP: '英镑',
    HKD: '港币',
    USD: '美元'
  }

  $('#pay_form').on('change', 'select[name=currency]', (e) ->
    $slt = $(@)
    updateMainInfo($slt.val()) if $slt.val()
    false
  )

  updateMainInfo = (target) ->
    $mainInfo = $('#mainInfo')
    ratio = extraRatio(target)
    # 汇率
    $('#currencyFromTo').text($('#request_currency').text() + " -> " + target)
    $('#ratio').text(ratio)

    # 需要支付的 币种/金额
    $('#paidCurrency').text(target)
    $('#paidCurrencyAmount').text(($('#finalAppied').text() * ratio).toFixed(4))

    # 汇率时间
    $('#ratioTime').text($.DateUtil.fmt3(extraRatioTime(target)))

    $mainInfo.data('ratio', ratio)

  # 抽取两个挂牌价中的汇率
  extraRatio = (target) ->
    if 'CNY' == target
      tr = $("#boc_rate tr td:contains(#{currencyMap[$('#request_currency').text()]})").parents('tr')
      (parseFloat(tr.find('td:eq(1)').css('color', 'red').text()) / 100).toFixed(4)
    else
      tr = $("#ex_rate tr td:contains(#{target})").parents('tr')
      (parseFloat(tr.find('td:eq(2)').css('color', 'red').text())).toFixed(4)

  # 抽取挂牌价时间
  extraRatioTime = (target) ->
    if 'CNY' == target
      tr = $('#boc_rate tr:eq(1)')
      new Date("#{tr.find('td:eq(6)').text()} #{tr.find('td:eq(7)').text()}")
    else
      new Date()

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
