$ ->
  $('#deliveryment_check_all').change ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox.#{region}").prop("checked", o.prop("checked"))

  $('#goToDeliverymentApply').click ->
    $('#deliverys_form').attr('method', 'post').attr('action', @getAttribute('url')).submit()

  $('#deliverys_form').on('click', 'a[name=confirmBtn]', (e) ->
    checkboxs = $('input[name="deliverymentIds"][data-state="PENDING"]:checked')
    if checkboxs.size() is 0
      noty({text: '请选择待确认的采购单！', type: 'error'})
      return false
    else
      return unless confirm("确认后采购单不能再修改，确认?")
      window.location.replace("/Deliveryments/confirm?#{checkboxs.serialize()}")
  )