$ ->
  $('#check_div').on('click', 'a[action=remove]', () ->
    return unless confirm('确认删除?')
    LoadMask.mask()
    $li = $(@)
    $.ajax($li.data('url'))
      .done((r) ->
        type = if r.flag
          # 只删除最近的一个 tr 父元素
          $li.parents('tr')[0].remove()
          'success'
        else
        'error'
        noty({text: r.message, type: type})
        LoadMask.unmask()
      )
  )