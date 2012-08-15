$ ->
  $('#do_cal').click (e) ->
    mask = $('#container')
    mask.mask('计算中...')
    $('#feedbacks').load('/TicketAnalyzes/feedbacks', $("#param_form :input").fieldSerialize(),
      (r) ->
        bindSortMethod('feedbacks')
    )
    $('#reviews').load('/TicketAnalyzes/reviews', $('#param_form :input').fieldSerialize(),
      (r) ->
        bindSortMethod('reviews')
        mask.unmask()
    )


  bindSortMethod = (tab)->
    $("##{tab} table th[id]").css('cursor', 'pointer').click (e) ->
      mask = $('#container')
      mask.mask('计算中...')
      col = @getAttribute('id')
      return false if col is null
      console.log("#{$('#param_form :input').fieldSerialize()}&#{$.param({col: col})}")
      $("##{tab}").load("/TicketAnalyzes/#{tab}", "#{$('#param_form :input').fieldSerialize()}&#{$.param({col: col})}",
        (r) ->
          bindSortMethod(tab)
          mask.unmask()
      )


  $('[name=from]').val($.DateUtil.fmt2($.DateUtil.addDay(-30, new Date())))
  $('[name=to]').val($.DateUtil.fmt2($.DateUtil.addDay(-1, new Date())))

