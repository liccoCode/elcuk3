$ ->
  titles = {}
  $('a[rel=tooltip]:has(.icon-amazon)').each(
    (e) ->
      if $(e).attr('title') not in titles
        titles[$(e).attr('title')] = [e]
      else
        titles[$(e).attr('title')].push(e)
  )

  alert JSON.stringify(titles)
