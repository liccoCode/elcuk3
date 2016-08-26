$ ->
  $("#create_ship_btn").click(->
    if $("#new_shipment input[type='checkbox']:checked").length > 0
      return unless confirm("确定创建运输单吗?")
      $("#new_shipment").submit()
    else
      noty({text: "请先选中需要创建的运输单的出库计划!", type: 'error'})
  )