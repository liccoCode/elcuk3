$ ->
  refreshReport = ->
    $("#costReportDiv").mask("加载中...")
    $("#costReportDiv").load("/ShipmentReports/costReport", {year: $("#select_year").val(), month: $("#select_month").val()}, (r) ->
      $("#costReportDiv").unmask()
    )


  $("#count_btn").click ->
    $("#column_home").trigger("flushColumnChart")
    refreshReport()

  $(document).on("flushColumnChart", "#column_home", (r) ->
    year = $("#select_year").val()
    month = $("#select_month").val()
    refreshReport()
    new PieChart("shipfee_by_type_column").percent(year, month, "#shipfee_by_type_column")
    new PieChart("shipweight_by_type_column").percent(year, month, "#shipweight_by_type_column")
  ).on("flushShipfeePieChart", "#shipfee_by_market_pie", (r, type) ->
    year = $("#select_year").val()
    month = $("#select_month").val()
    new PercentChart("shipfee_by_market_pie").percent(year, month, type, "#shipfee_by_market_pie")
  ).on("flushShipweightPieChart", "#shipweight_by_market_pie", (r, type) ->
    year = $("#select_year").val()
    month = $("#select_month").val()
    new PercentChart("shipweight_by_market_pie").percent(year, month, type, "#shipweight_by_market_pie")
  )

  class PieChart
    constructor: (@container) ->
    percent: (@year, @month, mask_selector = '#column_home') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {year: @year, month: month}, (p) ->
        title = if p.title == undefined then p['series'][0]['name'] else p.title
        $div.highcharts({
          credits:
            text: 'EasyAcc'
            href: ''
          title: {text: title},
          legend:
            enabled: true
          xAxis:
            lineWidth: 0,
            minorGridLineWidth: 0,
            lineColor: 'transparent',
            labels: {
              enabled: false
            },
            minorTickLength: 0,
            tickLength: 0
          yAxis: {min: 0}
          plotOptions:
            series:
              cursor: 'pointer',
              events: {
                click: ->
                  if(title.indexOf("费用") >= 0)
                    $("#shipfee_by_market_pie").trigger("flushShipfeePieChart", this.name)
                  else
                    $("#shipweight_by_market_pie").trigger("flushShipweightPieChart", this.name)
              }
            pie:
              dataLabels:
                enabled: true
          series: p['series']
        })
        LoadMask.unmask(mask_selector)
      )


  class PercentChart
    constructor: (@container) ->
    percent: (@year, @month, @type, mask_selector = '#pie_home') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {year: @year, month: @month, type: @type}, (p) ->
        title = if p.title == undefined then p['series'][0]['name'] else p.title
        $div.highcharts({
          credits:
            text: 'EasyAcc'
            href: ''
          title: {text: title},
          legend:
            enabled: true
          tooltip:
            formatter: ->
              "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%<br/>#{@y} / #{@total}"
          plotOptions:
            pie:
              dataLabels:
                enabled: true
                formatter: ->
                  "<b>#{@point.name}</b>: #{@y.toFixed(2)} [#{@percentage.toFixed(2)}%]"
          series: p['series']
        })
        LoadMask.unmask(mask_selector)
      )


  $("#column_home").trigger("flushColumnChart")




