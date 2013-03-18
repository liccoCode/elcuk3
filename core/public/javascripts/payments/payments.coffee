$ ->
  # ----------------------- payments.show -----------------
  paymentId = -> $('#paymentId').val()

  exchangeRate = () ->
    LoadMask.mask()
    $('#exchange_rate').load('/payments/rates', -> LoadMask.unmask())

  $('#refreshRate').click(exchangeRate)
  $('#exchange_rate').ready(exchangeRate) if $('#exchange_rate').size() > 0

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
  $('#paid').click -> $('#unit_list').attr('action', self.attr('url')).submit()


  uploadInit = ->
    # 1. 首先初始化 dropbox
    # 2. Load 图片
    window.dropUpload.iniDropbox(->
      fid: paymentId()
      p: 'PAYMENTS'
    , $('#dropbox'), '/payment/files/upload')
    window.dropUpload.loadImages(paymentId(), $('#dropbox'), 'PAYMENTS')

  $('#dropbox').ready(uploadInit) if $('#dropbox').size() > 0

