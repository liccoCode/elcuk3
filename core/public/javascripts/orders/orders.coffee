$ ->
  $('#order_list .sortable').click(
    ->
      $('#orderBy').val($(@).attr('name'))
      $('#search_form').submit()
  )
