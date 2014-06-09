$ ->
  $("#submit_btn").click ->
    flushArrivalRateLine()

  flushArrivalRateLine = ->
    new LineChart("arrival_rate_line").percent($("select[name='select_year']").val(), $("select[name='ship_type']").val(), $("select[name='count_type']").val() , "#arrival_rate_line")

  class LineChart
    constructor: (@container) ->
    percent: (@year, @shipType, @countType, mask_selector = '#arrival_rate_line') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {year: @year, shipType: @shipType, countType: @countType}, (r) ->
        $div.highcharts({
          credits:
            text:'EasyAcc'
            href:''
          title: { text: r.title },
          legend:
            enabled: true
          xAxis:
            categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
          yAxis: { min: 0 }
          tooltip:
            formatter: ->
              "<b>#{@point.name}</b>"
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

  # 初始化加载一下
  flushArrivalRateLine()
