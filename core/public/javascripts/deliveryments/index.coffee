$ ->
  $('#goToDeliverymentApply').click ->
    $('#deliverys_form').attr('method', 'post').attr('action', @getAttribute('url')).submit()
  $("#select_cooper").selectize()
