$ ->
  # ---------------------------------------- 采购单(deliveryPayments) 请款页面 -----------------
  # 采购单元的一行行数据
  class UnitRow
    constructor: (@uid) ->
      # 减去标题行与总结行
      @fees = $("#unit_#{@uid} .toggle_table_td tr").size() - 2

    remove: (feeId) ->
      self = @
      self.fees -= 1
      if self.fees <= 0
        $("#unit_#{self.uid} .toggle_table_td").html('<div style="padding:10px">没有支付信息</div>')
        $("[data-target=#unit_#{self.uid}] .badge").removeClass('badge-warning').text(-> parseInt($(@).text()) - 1)
      else
        $("#payunit_#{feeId}").remove()
        $("[data-target=#unit_#{self.uid}] .badge").text(-> parseInt($(@).text()) - 1)
      self

    # 计算总金额
    cal_amount: () ->
      self = @
      amount = self.amount('.amount')
      $("#fee_" + self.uid + "_amount").text(amount)
      self

    # 采购单元的总修正价格计算, 需要考虑数量
    cal_fix_value: () ->
      self = @
      amount = self.amount('.fix_value')
      $("#fee_" + self.uid + "_fix_value").text(amount * @qty())
      self

    cal_total: () ->
      self = @
      cal_amount = parseFloat($("#fee_" + self.uid + "_amount").text())
      cal_fix_value = parseFloat($('#fee_' + self.uid + '_fix_value').text())
      $('#fee_' + self.uid + '_amount_fixvalue').text("#{cal_amount} + #{cal_fix_value} = #{cal_amount + cal_fix_value}")
      self

    amount: (sub_selector) ->
      amount = 0
      $("#unit_#{@uid} #{sub_selector}").each(->
        if sub_selector.indexOf('amount') > -1
          amount += parseFloat($(@).text().split(' ')[1])
        else if sub_selector.indexOf('fix_value') > -1
          amount += parseFloat($(@).find('input').val())
      )
      amount

    # 计算 qty, 优先选择实际交货数量
    qty: () ->
      qty = $("#unit_qty_#{@uid}").text()
      if $.isNumeric(qty)
        parseInt(qty)
      else
        parseInt($("#unit_planQty_#{@uid}").text())


  class DmtRow
    @TABLEID = "deliveryment_fees"
    constructor: () ->
      @dmtId = $("#deliverymentId").val()
      @fees = $("##{DmtRow.TABLEID}").find('tr').size() - 1

    remove: (feeId) ->
      self = @
      self.fees -= 1
      if self.fees <= 0
        $("##{DmtRow.TABLEID}").html('<tr><td>暂时全部是采购单元的请款</td></tr>')
      else
        $("#payunit_#{feeId}").remove()

    cal_amount: () ->
      amount = 0
      $("##{DmtRow.TABLEID} .amount").each ->
        amount = parseFloat($(@).text().split(' ')[1])
      $('#fee_amount').text(amount)
      @

    cal_fix_value: () ->
      amount = 0
      $("##{DmtRow.TABLEID} .fix_value").each ->
        amount = parseFloat($(@).find('input').val())
      $('#fee_fix_value').text(amount)
      @

    # 计算采购单的 ammount + fix_value
    cal_total: () ->
      cal_amount = parseFloat($('#fee_amount').text())
      cal_fix_value = parseFloat($('#fee_fix_value').text())
      $('#fee_amount_fixvalue').text("#{cal_amount} + #{cal_fix_value} = #{cal_amount + cal_fix_value}")
      @


  # 所有 Deliveryment 的删除按钮
  $('#deliveryment_fees button.remove').click ->
    return unless confirm('请不要着急点确定, 请先确认是否真的需要删除?')
    feeId = $(@).parents('tr').attr('id').split('_')[1]
    LoadMask.mask()
    $.ajax({url: "/paymentunit/#{feeId}", type: 'DELETE'}).done((r) ->
      try
        if(r.flag)
          new DmtRow().remove(feeId)
          Notify.ok("删除成功.", r.message)
        else
          Notify.alarm("删除失败.", r.message)
      finally
        LoadMask.unmask()
    )

  # 所有 PaymentUnit 删除按钮
  $('#procure_units_fees button.remove').click ->
    return unless confirm('请不要着急点确定, 请先确认是否真的需要删除?')
    self = @
    feeId = $(self).parents('tr').attr('id').split('_')[1]
    uid = $(self).parents('table').parents('tr').attr('id').split('_')[1]
    LoadMask.mask();
    $.post("/paymentunits/#{feeId}/remove", (r) ->
      try
        if(r.flag)
          new UnitRow(uid).remove(feeId)
          Notify.ok("删除成功.", r.message)
        else
          Notify.alarm("删除失败.", r.message)
      finally
        LoadMask.unmask()
    )

  # 计算 Deliveryment 的 amount
  new DmtRow().cal_amount().cal_fix_value().cal_total()

  # 为所有 ProcureUnit 的点击事件增加计算 amount
  $("#procure_units_fees [data-target]").click ->
    new UnitRow($(@).attr('data-target').split('_')[1]).cal_amount().cal_fix_value().cal_total()

  $('.fixValue').keyup((e) ->
    e.preventDefault()
    if e.keyCode == 13
      LoadMask.mask()
      $.post("#{@getAttribute('data-url')}", {fixValue: @value}).done((r) ->
        try
          if r.flag == false
            alert(r.message)
          else
            e.target.blur()
        finally
          LoadMask.unmask()
      )
  )


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

