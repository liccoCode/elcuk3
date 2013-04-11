$ ->
  Highcharts.setOptions(global:
    {useUTC: false})

  defaultDate = $.DateUtil.addDay(-30)
  now = $.DateUtil.addDay(30, defaultDate)

  $('#_from').data('dateinput').setValue(defaultDate)
  $('#_to').data('dateinput').setValue(now)

  #checkbox
  base_html=(value, label)->
    return "<label class=\"checkbox\"><input type=\"checkbox\" name=\"templates\" value=\"#{value}\">#{label}</label>"

  #初始化模板
  templates_init=->
    system_htmls=[
      base_html('daily_review', 'DAILY_REVIEW')
      base_html('daily_feedback', 'DAILY_FEEDBACK')
      base_html('product_picture_check', 'SKU_PIC_CHECK')
    ]
    systemTemplate=system_htmls.join('')
    fba_htmls=[
      base_html('shipment_state_change', 'STATE_CHANGE')
      base_html('shipment_receipt_not_receiving', 'NOT_RECEING')
      base_html('shipment_receiving_check', 'RECEIVING_CHECK')
    ]
    fbaTempalte=fba_htmls.join('')
    normal_htmls=[
      base_html('shipment_clearance', 'CLEARANCE')
      base_html('shipment_isdone', 'IS_DONE')
      base_html('more_offers', 'MORE_OFFERS')
      base_html('amazon_review_uk', 'REVIEW_UK')
      base_html('amazon_review_de', 'REVIEW_DE')
      base_html('amazon_review_us', 'REVIEW_US')
      base_html('feedback_warnning', 'FEEDBACK_WARN')
      base_html('review_warnning', 'REVIEW_WARN')
      base_html('fnsku_check_warn', 'FNSKU_CHECK')
    ]
    normalTemplate=normal_htmls.join('')
    return NORMAL: normalTemplate, FBA: fbaTempalte, SYSTEM: systemTemplate

  #获得所有的模板
  target_templates=templates_init()

  #初始化target
  $('#tmp_target').append(target_templates[$('[name=type]').val()])


  $('[name=type]').change(->
    $('#tmp_target').empty().append(target_templates[this.value])
  )

  #在target隐藏后取消选中的checkbox
  $('#tmp_target').on('hidden', ->
    $('[name=templates]').attr('checked', false)
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


  $('#search_btn').click (e) ->
    e.preventDefault()
    $.getJSON(this.getAttribute('url'), $('#params').formToArray(), (r) ->
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

  $('#search_btn').click()

