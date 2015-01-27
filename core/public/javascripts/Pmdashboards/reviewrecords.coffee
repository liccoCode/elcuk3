$ ->
  $('.search_form').on('click', '#reviewRecords', (r) ->
    new LineChart("reviews_rating_line").percent()
    new LineChart("poor_ratings_line").percent()
  ).on('click', '#exportReviewRecords', (r) ->
    $btn = $(@)
    $btn.parents('form').submit()
  )

  class LineChart
    constructor: (@container) ->
    percent: (mask_selector='#reviewAnalyzes') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), $('.search_form').serialize(), (r) ->
        $div.highcharts('StockChart', {
          credits:
            text:'EasyAcc'
            href:''
          title:
            text: r.title
          legend:
            enabled: true
          navigator:
            enabled: true
          scrollbar:
            enabled: false
          type: 'datetime'
          yAxis: { min: 0 }
          tooltip:
            xDateFormat: '%Y-%m-%d'
        series: r['series']
        })
        LoadMask.unmask(mask_selector)
      )

  $(document).ready ->
    $('#reviewRecords').trigger('click')
