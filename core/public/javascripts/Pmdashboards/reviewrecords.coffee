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
        $div.highcharts({
          title: { text: r.title },
          legend:
            enabled: true
          xAxis:
            type: "datetime"
          yAxis: { min: 0 }
          tooltip:
            shared: true
            crosshairs: true
            xDateFormat: '%Y-%m-%d'
          plotOptions:
            pie:
            #cursor: 'point'
              dataLabels:
                enabled: true
              #color: '#000'
                formatter: ->
                  "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%"
          series: r['series']
        })
        LoadMask.unmask(mask_selector)
      )

  $(document).ready ->
    $('#reviewRecords').trigger('click')
