$ ->
  $('#goToDeliverymentApply').click ->
    $('#deliverys_form').attr('action', @getAttribute('url')).submit()