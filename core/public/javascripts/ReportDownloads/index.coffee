$ ->
  $("#reports").on("click", "#repeatcalculate", (r) ->
    return false if !confirm("确认重新计算?")
    $.ajax($(@).data('url'), {
      type: 'POST',
      data: ""
    })
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

  $input = $("#cooperator_input")
  resultMap = {}
  $input.typeahead({
    source: (query, process) ->
      name = $input.val()
      $.get('/cooperators/findSameCooperator', {name: name})
        .done((c) ->
        $("#cooperator_id").val("")
        resultMap = c
        result = _.map(c, (n) ->
          n.split("-")[0]
        )
        process(result)
      )
    updater: (item) ->
      coo = _.find(resultMap, (n) ->
        if n.split("-")[0] == item
          n
      )
      $("#cooperator_id").val(coo.split("-")[1])
      item
  })
