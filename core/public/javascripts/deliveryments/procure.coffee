$ ->

  # 处理 hash
  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("#paymentUnit#{paymentUnitId}")
    targetTr.parents('tr').prev().find('td[data-toggle]').click()
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
