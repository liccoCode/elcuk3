$ ->
  $('#reason_table button:eq(0)').click (e) ->
    $.post('/ListingReasons/save', $('#reason_table :input').fieldSerialize(),
      (r) ->
        alert(r)
    )
