$ ->
  Highcharts.setOptions(global:
    {useUTC: false})

  defaultDate = $.DateUtil.addDay(-30)
  now = $.DateUtil.addDay(30, defaultDate)

  $('#_from').data('dateinput').setValue(defaultDate)
  $('#_to').data('dateinput').setValue(now)

  #在选择类型时隐藏之前的data-target 绑定对应的target 并取消选中的模板
  $('[name=type]').change(->
    target= $("#tmp").attr("data-target")
    $(target).toggleClass('collapse')
    $(target).collapse('hide')
    $('[name=tmp]').attr('checked', false)
    $("#tmp").attr("data-target", '#TMP_' + this.value)
  )

  #在target隐藏后取消选中的模板
  $('#TMP_NORMAL').on('hidden', ->
    $('[name=tmp]').attr('checked', false)
  )
  $('#TMP_FBA').on('hidden', ->
    $('[name=tmp]').attr('checked', false)
  )
  $('#TMP_SYSTEM').on('hidden', ->
    $('[name=tmp]').attr('checked', false)
  )

  lineOp = (container, yName) ->
    chart:
      renderTo: container
    title:
      text: 'Chart Title'
    xAxis:
      type: 'datetime'
      dateTimeLabelFormats:
        day: '%Y-%m-%d %e'
      tickInterval: 5 * (24 * 3600 * 1000)
    yAxis:
      title:
        text: yName
      min: 0
    plotOptions:
      series: # 需要从服务器获取开始时间
        pointStart: new Date().getTime()
        # 1day
        pointInterval: 24 * 3600 * 1000
        cursor: 'pointer'
        point:
          events:
            {}
    tooltip:
      shared: true
      formatter: ->
        s = "<b>#{Highcharts.dateFormat('%Y-%m-%d', @x)}</b><br>"
        @points.forEach((point) ->
          totalY = point.series.yData.reduce((a, b)-> a + b)
          s += "<span style=\"color:#{point.series.color}\">#{point.series.name}</span>: <b>#{point.y}</b><br/>"
        )
        s
      crosshairs: true
      xDateFormat: '%Y-%m-%d %A'
    series: []
    # 设置这条线的'标题'
    head: (title) ->
      @title.text = title
      @
    click: (func) ->
      @plotOptions.series.point.events.click = func
      @
    mouseOver: (func) ->
      @plotOptions.series.point.events.mouseOver = func
      @
    mouseOut: (func) ->
      @plotOptions.series.point.events.mouseOut = func
      @
    formatter: (func) ->
      @tooltip.formatter = func
      @
    clearLines: () ->
      @series = []
      @
    xStart: (datetime_millions) ->
      @plotOptions.series.pointStart = datetime_millions
      @
    id: -> container


  mailRecordLines=->lineOp("mail_records", "Mail Records")

  mailops_line = (params) ->
    $.getJSON('/mailsrecords/ajaxRecord', params, (r) ->
      if r.flag is false
        alert(r.message)
      else
        type = $('[name=type]').val()
        unitLines = mailRecordLines()
        unitLines.head("Mail Records [ " + type.toUpperCase() + "]")
        for line in r['series']
          unitLines.series.push(line)
        unitLines.xStart(r['pointStart'])
        console.log(unitLines)
        $('#' + unitLines.id()).data('char', new Highcharts.Chart(unitLines));
    )


  mailops_line($('#params').formToArray())


  $('#search_btn').click (e) ->
    e.preventDefault()
    mailops_line($('#params').formToArray())

