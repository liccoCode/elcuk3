$ ->

  # 计算可提交的 Unit 数量
  sumShipSize = -> $('#units_form input:checked').size()

  # table 的全选
  $(':checkbox[class=checkbox_all]').change (e) ->
    o = $(@)
    o.parents('form').find(':checkbox[id*=checkbox]').prop("checked", o.prop('checked'))
    if o.attr('id') == "units"
      $('#ship_size').text(sumShipSize())
      if o.prop('checked')
        $("#units_form input[name=shipQty]").removeAttr('disabled')
      else
        $("#units_form input[name=shipQty]").attr("disabled", "disabled")


  $('#submit_delivery').click (e) -> $(@).parents('form').attr('action', '/shipments/ship')
  $('#cancel_shipitem').click (e) -> $(@).parents('form').attr('action', '/shipments/cancelShip')

