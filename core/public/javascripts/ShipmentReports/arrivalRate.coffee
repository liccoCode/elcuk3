$ ->
  $("#submit_btn").click ->
    flushArrivalRateLine()

  flushArrivalRateLine = ->
    new LineChart("arrival_rate_line").percent($("select[name='select_year']").val(),
      $("select[name='ship_type']").val(), $("select[name='count_type']").val(), "#arrival_rate_line")

  class LineChart
    constructor: (@container) ->
    percent: (@year, @shipType, @countType, mask_selector = '#arrival_rate_line') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {year: @year, shipType: @shipType, countType: @countType}, (r) ->
        $div.highcharts({
          credits:
            text: 'EasyAcc'
            href: ''
          title: {text: r.title},
          legend:
            enabled: true
          xAxis:
            categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
          yAxis: {min: 0}
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

  $(".search_form").on("click", ".btn:contains(Excel)", (e) ->
    e.preventDefault()
    $form = $('#search_form')
    window.open('/Excels/arrivalRateReport?' + $form.serialize(), "_blank")
  )

  $(document).ready ->
    $('#firstTab').click ->
      $('#activeprocess').fadeIn('fast')
      $("#runprocess").fadeOut('fast')

    $('#secondTab').click ->
      $('#runprocess').fadeIn('fast')
      $("#activeprocess").fadeOut('fast')

    oTable = $("#shipmentTable").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: true
      sPaginationType: "full_numbers"
      aaSorting: [[0, "asc"]]
      iDisplayLength: 25
      aoColumnDefs: [{sDefaultContent: '', aTargets: ['_all']}]
    )


  # Form 搜索功能
  $("#exceldown").click((e)->
    e.preventDefault()
    $form = $('#search_form')
    window.open('/Excels/arrivalRateReport?' + $form.serialize(), "_blank")
  )