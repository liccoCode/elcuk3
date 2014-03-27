$ ->

  class PieChart
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



  # 重新绘制所有的 Pie 图
  drawPies = (year,team) ->
    new PieChart("sale_column").percent('salecolumn', year,team)
    new PieChart("profitrate_line").percent('profitrateline', year,team)


  $('#orders button[name="search"]').click ->
    drawPies($('select[name="year"]').val(),$('select[name="team"]').val())

  drawPies($('select[name="year"]').val(),$('select[name="team"]').val())
