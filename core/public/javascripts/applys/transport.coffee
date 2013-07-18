$ ->
  $('#shipments').on('click', '.delete', (e) ->
    e.stopPropagation()
    id = $(@).data('id')
    $.ajax("/shipment/#{id}/departApply", {type: 'DELETE', dataType: 'json'})
      .done((r) ->
        if r.flag == true
          noty({text: "##{id} #{r.message}", type: 'success', timeout: 3000})
          $("#shipment_#{id.replace(/[|]/g, '\\|')}").next().remove().end().remove()
        else
          text = _.map(r,(err) ->
            err.message
          ).join('<br>')
          noty({text: text, type: 'error'})
      )
      .fail((r) ->
        noty({text: '服务器错误', type: 'error'})
      )
  )
