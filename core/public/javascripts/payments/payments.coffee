$ ->
  # ----------------------- payments.show -----------------
  paymentId = -> $('#paymentId').val()

  # 从 Ajax 加载的中行数据中获取最新美元买入价
  latest_us_ratio = ->
    parseFloat($('#exchange_rate tr:eq(3) td:eq(1)').addClass('text-error').text())

  latest_us_publish_date = ->
    # 2013-03-19 15:32:38
    "#{$('#exchange_rate tr:eq(3) td:eq(6)').text()} #{$('#exchange_rate tr:eq(3) td:eq(7)').text()}"

  # 计算根据最新汇率的美金价格
  totalFee_USD = ->
    parseFloat($('#totalFee_CNY').text().split(' ')[1]) / (latest_us_ratio() / 100)

  exchangeRate = () ->
    LoadMask.mask()
    $('#exchange_rate').load('/payments/rates', ->
      $('#totalFee_USD').text("$ #{totalFee_USD().toFixed(4)}")
      $('#paid_usRatio').val(latest_us_ratio() / 100)
      $('#paid_ratioPublishTime').val(latest_us_publish_date())
      LoadMask.unmask()
    )

  $('#refreshRate').click(exchangeRate).click()

  # 表格上访的功能按钮
  $('#select_all').click ->
    $('#unit_list :checkbox[name=unitIds]').prop('checked', true)

  $('#select_inverse').click ->
    $('#unit_list :checkbox[name=unitIds]').prop('checked', (i, attr)-> !attr)

  check_checkbox = () ->
    $('#unit_list :checkbox[name=unitIds]').filter((index, el) -> return el.checked).size() > 0

  for id in ['#approval', '#deny']
    $(id).click (e) ->
      e.preventDefault()
      self = $(@)
      if check_checkbox()
        $('#unit_list').attr('action', self.attr('url')).submit()
      else
        alert('请先勾选需要处理的请款项')

  # form 表单提交要做检测
  $('#paid').click (e) ->
    unless confirm("币种:#{$('#paid_currency').val()}\r账户:#{$('#paid_targetId :selected').text()}\r金额:#{$('#paid_actualPaid').val()} \r\n确认支付吗?")
      e.preventDefault()


  do ->
    # 1. 首先初始化 dropbox
    # 2. Load 图片
    window.dropUpload.iniDropbox(->
      fid: paymentId()
      p: 'PAYMENTS'
    , $('#dropbox'), '/payment/files/upload')
    window.dropUpload.loadImages(paymentId(), $('#dropbox'), 'PAYMENTS')


