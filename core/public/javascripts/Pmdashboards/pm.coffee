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
          tooltip:
            formatter: ->
              "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%<br/>销量: #{@y} / #{@total}"
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
  drawPies = (year,team) ->
    new PieChart("sale_percent").percent('sale', year,team)
    new PieChart("profit_percent").percent('profit', year,team)
    new PieChart("sale_column").percent('salecolumn', year,team)
    new PieChart("profitrate_line").percent('profitrateline', year,team)
    new PieChart("teamsale_percent").percent('teamsale', year,team)
    new PieChart("teamprofit_percent").percent('teamprofit', year,team)
    new PieChart("salefee_line").percent('salefeeline', year,team)


  $('#orders button[name="search"]').click ->
    drawPies($('select[name="year"]').val(),$('select[name="team"]').val())

  drawPies($('select[name="year"]').val(),$('select[name="team"]').val())
