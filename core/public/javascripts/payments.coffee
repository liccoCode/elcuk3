$ ->
  class UnitRow
    constructor: (@uid) ->
      # 减去标题行与总结行
      @fees = $("#unit_#{@uid} .toggle_table_td tr").size() - 2

    remove: (feeId) ->
      self = @
      self.fees -= 1
      console.log(self.fees)
      if self.fees <= 0
        $("#unit_#{self.uid} .toggle_table_td").html('<div style="padding:10px">没有支付信息</div>')
        $("[data-target=#unit_#{self.uid}] .badge").removeClass('badge-warning').text(-> parseInt($(@).text()) - 1)
      else
        $("#payunit_#{feeId}").remove()
        $("[data-target=#unit_#{self.uid}] .badge").text(-> parseInt($(@).text()) - 1)
      self

    # 计算总金额
    cal_ammount: () ->
      self = @
      amount = self.amount('.amount')
      $("#fee_" + self.uid + "_amount").text(amount)
      self

    # 计算总修正价格
    # todo: 算法需要加入数量的因素
    cal_fix_value: () ->
      self = @
      amount = self.amount('.fix_value')
      $("#fee_" + self.uid + "_fix_value").text(amount)
      self

    amount: (sub_selector) ->
      amount = 0
      $("#unit_#{@uid} #{sub_selector}").each(-> amount += parseFloat($(@).text().split(' ')[1]))
      amount


  # 所有 PaymentUnit 删除按钮
  $('button.remove').click ->
    feeId = $(@).parents('tr').attr('id').split('_')[1]
    self = @
    uid = $(self).attr('uid')
    $.post("/paymentunits/#{feeId}/remove", (r) ->
      if(r.flag)
        new UnitRow(uid).remove(feeId)
        Notify.ok("删除成功.", r.message)
      else
        Notify.alarm("删除失败.", r.message)
    )

  # 为所有 ProcureUnit 的点击事件增加计算 amount
  $("#procure_units_fees [data-target]").click ->
    new UnitRow($(@).attr('data-target').split('_')[1]).cal_ammount().cal_fix_value()
