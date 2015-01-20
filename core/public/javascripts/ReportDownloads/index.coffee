$ ->
  $("#profits_form").on("click", "#repeatcalculate", (r) ->
    $.ajax($(@).data('url'), {type: 'POST', data: "" })
      .done((r) ->
        msg = if r.flag is true
          "#{r.message}"
        else
          r.message
        alert msg
      )
      .fail((r) ->
        alert r.responseText
      )
  )