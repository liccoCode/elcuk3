$ ->
  $('#shipments').on('click', '.delete', (e) ->
    e.stopPropagation()
    id = $(@).data('id')
    $.ajax("/apply/transport/#{id}/shipment", {
      type: 'DELETE',
      dataType: 'json'
    })
    .done((r) ->
      if r.flag == true
        noty({
          text: "##{id} #{r.message}",
          type: 'success',
          timeout: 3000
        })
        $("#shipment_#{id.replace(/[|]/g, '\\|')}").next().remove().end().remove()
      else
        text = _.map(r, (err) ->
          err.message
        ).join('<br>')
        noty({
          text: text,
          type: 'error'
        })
    )
    .fail((r) ->
      noty({
        text: '服务器错误',
        type: 'error'
      })
    )
  )

  $("input[name='p.search']").typeahead({
    source: (query, process) ->
      $.get('/applys/source', {
        applyId: $("input[name='p.applyId']").val(),
        search: query
      }).done((c) ->
        process(c)
      )
  })

  # 处理 hash
  do ->
    paymentUnitId = window.location.hash[1..-1]
    targetTr = $("#fee_#{paymentUnitId}")
    if targetTr.size() > 0
      targetTr.parents('tr').prev().find('td[data-toggle]').click()
      EF.scoll(targetTr)
      EF.colorAnimate(targetTr)

