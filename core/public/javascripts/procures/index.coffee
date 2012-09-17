$ ->

  # 全选按钮
  $('input:checkbox[id=checkbox_all]').change (e) ->
    $('input:checkbox[id=checkbox*][id!=checkbox_all]').prop("checked", $(@).prop("checked"))


  $('#create_deliveryment_btn').click (e) ->
    form = $('#create_deliveryment')
    form.attr('action', '/Procures/createDeliveryment')
    form.submit()

  $("#name_label").change (e) ->
    $('#name_input').val($(@).val().trim())
