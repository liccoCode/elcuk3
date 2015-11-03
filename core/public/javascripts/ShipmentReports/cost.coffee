$ ->
  refreshReport = ->
    $("#costReportDiv").mask("加载中...")
    from = $("#select_from").val()
    to = $("#select_to").val()
    $("#costReportDiv").load("/ShipmentReports/costReport", {from: from, to: to}, (r) ->
      $("#costReportDiv").unmask()
      $("#exportFreightBtn").click(->
        window.open('/Excels/downloadFreightReport?from=' + from + '&to=' + to, "_blank")
      )
      $("#exportVATbtn").click(->
        window.open('/Excels/downloadVATReport?from=' + from + '&to=' + to, "_blank")
      )
    )


  $("#count_btn").click ->
    $("#column_home").trigger("flushColumnChart")
    refreshReport()

  $(document).on("flushColumnChart", "#column_home", (r) ->
    from = $("#select_from").val()
    to = $("#select_to").val()
    refreshReport()
    new PieChart("shipfee_by_type_column").percent(from, to, "#shipfee_by_type_column")
    new PieChart("shipweight_by_type_column").percent(from, to, "#shipweight_by_type_column")
  ).on("flushShipfeePieChart", "#shipfee_by_market_pie", (r, type) ->
    from = $("#select_from").val()
    to = $("#select_to").val()
    new PercentChart("shipfee_by_market_pie").percent(from, to, type, "#shipfee_by_market_pie")
  ).on("flushShipweightPieChart", "#shipweight_by_market_pie", (r, type) ->
    from = $("#select_from").val()
    to = $("#select_to").val()
    new PercentChart("shipweight_by_market_pie").percent(from, to, type, "#shipweight_by_market_pie")
  )

  class PieChart
    constructor: (@container) ->
    percent: (@from, @to, mask_selector = '#column_home') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {from: @from, to: @to}, (p) ->
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
    percent: (@from, @to, @type, mask_selector = '#pie_home') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {from: @from, to: @to, type: @type}, (p) ->
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




