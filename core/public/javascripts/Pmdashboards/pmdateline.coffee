$ ->

  class LineChart
    constructor: (@container) ->

    # units/sales
    percent: (type = 'units', @year  ,@team , mask_selector = '#orders') =>
      self = @
      LoadMask.mask(mask_selector)
      $.get('/pmdashboards/percent', {type: type, year: @year,team: @team}, (r) ->
        title = r['series'][0]['name']
        console.log(r['series'][0]['name'])
        $("##{self.container}").highcharts({
          title: {
            text: title
          },
          legend:
            enabled: true
          xAxis:
           type: 'datetime'
          yAxis: { min: 0 }
          tooltip:
            formatter: ->
              s = "<b>#{Highcharts.dateFormat('%Y-%m-%d', @x)}</b><br>"
              s += "<span style=\"color:#{@point.series.color}\">#{@point.series.name}:</span><b>#{@point.y}</b><br/>"
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



  # 重新绘制所有的 Pie 图
  linePies = (year,team) ->
    new LineChart("salefee_line").percent('salefeeline', year,team)
    new LineChart("saleqty_line").percent('saleqtyline', year,team)


  $('#orders button[name="search"]').click ->
    linePies($('select[name="year"]').val(),$('select[name="team"]').val())

  linePies($('select[name="year"]').val(),$('select[name="team"]').val())
