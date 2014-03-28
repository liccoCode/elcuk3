$ ->
  class PieChart
    constructor: (@container) ->
    percent: (mask_selector = '#c_column') =>
      self = @
      LoadMask.mask(mask_selector)
      $.get('/CategoryInfos/ajaxCategorySalesAmount', {id: $("#cateId").val()}, (r) ->
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
              s = "<span style=\"color:#{@point.series.color}\">#{@point.series.name}:<b>#{@point.y}</b></span><br/>"
              s
          plotOptions:
            pie:
            #cursor: 'point'
              dataLabels:
                enabled: true
        #color: '#000'
          series: r['series']
        })
        LoadMask.unmask(mask_selector)
      )

  new PieChart("c_column").percent()
