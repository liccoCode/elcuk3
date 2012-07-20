$ ->
  bindTableClick = ->
    $('tr[drop]').css('cursor', 'pointer').click (e) ->
      $("#review_#{@getAttribute('drop')}").toggle('fast')
      e.preventDefault()


  $('#search_form [name=asin]').keyup (e) ->
    return false if e.keyCode isnt 13
    #B007LE0UT4
    o = $(@)
    return false if o.val().length isnt 10
    reviews = $('#reviews')
    reviews.mask('加载中...')
    reviews.load('/amazonReviews/ajaxMagic', $('#search_form :input').fieldSerialize(),
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          bindTableClick()
        reviews.unmask()
    )
    e.preventDefault()
