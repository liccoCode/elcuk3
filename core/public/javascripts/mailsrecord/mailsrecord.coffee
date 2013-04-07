$ ->
  #延迟加载
  setTimeout(()->
    Highcharts.setOptions(global:
         {useUTC: false})


    defaultDate = $.DateUtil.addDay(-30)
    now = $.DateUtil.addDay(30, defaultDate)

    $('#_from').data('dateinput').setValue(defaultDate)
    $('#_to').data('dateinput').setValue(now)

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


    mailRecordLines=->lineOp("mail_records","Mail Records")

    mailops_line = (params) ->
        #LoadMask.mask(BELOWTAb.content_selector())

        $.getJSON('/mailsrecords/ajaxRecord', params, (r) ->
          try
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
          finally
            #LoadMask.unmask(BELOWTAb.content_selector())
        )


    mailops_line($('#params').formToArray())


    #这里刷新数据后,搜索栏的条件保存不了是为什么???? 这可是异步。。。。
    $('#search_btn').click(->
         mailops_line($('#params').formToArray())
    )
  ,100)

