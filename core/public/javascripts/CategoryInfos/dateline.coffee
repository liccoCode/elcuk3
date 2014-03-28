$ ->
  class LineChart
    constructor: (@container) ->
    percent: (mask_selector = '#c_line') =>
      self = @
      LoadMask.mask(mask_selector)
      $.get('/CategoryInfos/ajaxCategorySalesProfit', {id: $("#cateId").val()}, (r) ->
        $("##{self.container}").highcharts({
          title: {
            text: r.title
          },
          legend:
            enabled: true
          xAxis:
            type: 'category'
          yAxis: { min: 0 }
          tooltip:
            formatter: ->
              s = "<span style=\"color:#{@point.series.color}\">#{@point.series.name}:</span><b>#{@point.y}</b><br/>"
              s
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

  new LineChart("c_line").percent()
