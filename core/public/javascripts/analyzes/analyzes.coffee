$ ->
  Highcharts.setOptions(global:
    {useUTC: false})
  SKU = 'sku'
  SID = 'sid'
  # TODO: 需要还原成 -30 天
  defaultDate = $.DateUtil.addDay(-210)
  now = $.DateUtil.addDay(30, defaultDate)

  class Tabs
    constructor: (name) ->
      @tabs_id = "#{name}_tab"
      @content_id = "#{name}_tabContent"

    tabs: -> $("##{@tabs_id}")
    content: -> $("##{@content_id}")
    active: -> $("##{@tabs_id} [data-toggle=tab]").parent().filter('.active').find('a').attr('href')[1..-1]
    content_selector: -> "#" + @content_id


  # 获取下面的 Tab 元素组合
  BELOWTAb = new Tabs("below")

  # 获取上面的 Tab 元素组合
  TOPTAB = new Tabs("top")

  # 用来构造给 HighChart 使用的默认 options 的方法
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
        s = "<b>#{@x}</b><br>"
        @points.forEach((point) ->
          totalY = point.series.yData.reduce((a, b)-> a + b)
          s += "<span style=\"color:#{point.series.color}\">#{point.series.name}</span>: <b>#{point.y} (#{totalY})</b><br/>"
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

  # 销售订单曲线
  newSaleUnitLines = -> lineOp('a_units', 'Units').click(
    ->
      val = paramsObj()['p.val']
      window.open('/analyzes/pie?msku=' + val + '&date=' + $.DateUtil.fmt2(new Date(@x)),
      val,
      'width=520,height=620,location=yes,status=yes'
      )
  )

  # 销售销量曲线
  newSalesLines = -> lineOp('a_sales', 'Sales(USD)').click(
    ->
      alert(@series.name + ":::::" + @x + ":::" + @y)
  )

  # 转换率的曲线
  turnOverLine = lineOp('a_turn', '转化率')

  # Session 数量曲线
  sessionLine = lineOp('a_ss', 'Session && PV')

  paramsObj = () -> $.formArrayToObj($('#click_param').formToArray())
  # ------------------- 曲线绘制 ---------------------
  # 绘制 Session 的曲线
  ss_line = (params) ->
    LoadMask.mask(BELOWTAb.content_selector())
    $.getJSON('/analyzes/ajaxSellingRecord', params, (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          lines =
            pv_uk:
              {name: 'PageView(uk)', data: []}
            pv_de:
              {name: 'PageView(de)', data: []}
            pv_fr:
              {name: 'PageView(fr)', data: []}
            pv_us:
              {name: 'PageView(us)', data: []}
            ss_uk:
              {name: 'Session(uk)', data: []}
            ss_de:
              {name: 'Session(de)', data: []}
            ss_fr:
              {name: 'Session(fr)', data: []}
            ss_us:
              {name: 'Session(us)', data: []}
          sessionLine.head('Selling[' + params['p.val'] + '] SS')
          sessionLine.clearLines()
          for k,v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            sessionLine.series.push(lines[k])
          $('#' + sessionLine.id()).data('char', new Highcharts.Chart(sessionLine))
      finally
        LoadMask.unmask(BELOWTAb.content_selector())
    )

  # 绘制转换率曲线
  turn_line = (params) ->
    LoadMask.mask(BELOWTAb.content_selector())
    $.getJSON('/analyzes/ajaxSellingTurn', params, (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          lines =
            tn_uk:
              {name: 'TurnRatio(uk)', data: []}
            tn_de:
              {name: 'TurnRatio(de)', data: []}
            tn_fr:
              {name: 'TurnRatio(fr)', data: []}
            tn_us:
              {name: 'TurnRatio(us)', data: []}
          turnOverLine.head('Selling[' + params['p.val'] + '] 转化率')
          turnOverLine.clearLines()
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            # 填充完曲线数据
            turnOverLine.series.push(lines[k]) # 添加曲线
          $('#' + turnOverLine.id()).data('char', new Highcharts.Chart(turnOverLine))
      finally
        LoadMask.unmask(BELOWTAb.content_selector())
    )

  # 绘制销量曲线
  unit_line = (params) ->
    LoadMask.mask(BELOWTAb.content_selector())
    $.getJSON('/analyzes/ajaxUnit', params, (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          display_sku = params['p.val']
          unitLines = newSaleUnitLines()
          unitLines.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['p.type']?.toUpperCase() + "] Unit Order")
          names =
            unit_all: 'Unit Order(all)'
            unit_uk: 'Unit Order(uk)'
            unit_de: 'Unit Order(de)'
            unit_fr: 'Unit Order(fr)'
            unit_us: 'Unit Order(us)'
          # 将曲线的名字更换为可读性更强的
          r['series'].map((l) -> l.name = names[l.name])
          for line in r['series']
            unitLines.series.push(line)
          unitLines.xStart(r['pointStart'])
          console.log(unitLines)
          $('#' + unitLines.id()).data('char', new Highcharts.Chart(unitLines));
      finally
        LoadMask.unmask(BELOWTAb.content_selector())
    )

  # 绘制销售额曲线
  sale_line = (params) ->
    LoadMask.mask(BELOWTAb.content_selector())
    $.getJSON('/analyzes/ajaxSales', params,
    (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          display_sku = params['p.val']
          salesLines = newSalesLines()
          salesLines.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['p.type']?.toUpperCase() + "] Sales(USD)")
          names =
            sale_all: 'Sales(all)'
            sale_uk: 'Sales(uk)'
            sale_de: 'Sales(de)'
            sale_fr: 'Sales(fr)'
            sale_us: 'Sales(us)'
          r['series'].map((l) -> l.name = names[l.name])
          for line in r['series']
            salesLines.series.push(line)
          salesLines.xStart(r['pointStart'])
          $('#' + salesLines.id()).data('char', new Highcharts.Chart(salesLines))
      finally
        LoadMask.unmask(BELOWTAb.content_selector())
    )

  # -------------------------------------------------------------------

  # 绑定 sid tab 中修改 ps 的事件
  bindSIDPsBtn = ->
    $('#sid input[ps]').unbind().change ->
      tableE = $(@).parents('table')
      tableE.mask('更新 PS 中...')
      $.post('/analyzes/ps', {sid: $(@).attr('sid'), ps: $(@).val()},
      (r) ->
        if r.flag is false
          alert(r.message)
        tableE.unmask()
      )

  # 绘制 ProcureUnit 的 timeline 中的数据
  paintProcureUnitInTimeline = (type, val)->
    LoadMask.mask(BELOWTAb.content_selector())
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: val},
    (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          #eventSource.loadJSON(json, url)
          eventSource = $('#tl').data('source')
          eventSource.clear()
          eventSource.loadJSON(r, '/')
      finally
        LoadMask.unmask(BELOWTAb.content_selector())
      #$('#tl').data('timeline').paint()
    )

  # Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据.
  sellRankLoad = () ->
    LoadMask.mask();
    type = $("[name=p\\.type]").val()
    target = $("##{type}").load('/Analyzes/analyzes', $("#click_param").formSerialize(),
    ->

      #tooltip
      window.$ui.tooltip()

      # 翻页
      $("##{type} div.pagination a").click(
        (e) ->
          e.preventDefault()
          $('[name=p\\.page]').val($(@).attr('page'))
          sellRankLoad()
      )

      # 排序功能
      sortables = $("##{type} th.sortable")
      sortables.click (e) ->
        $('[name=p\\.orderBy]').val($(@).attr('name'))
        sellRankLoad()

      # 绑定 sku/sid 的点击事件[unit_line, sales_line, session, turnOver]
      $("##{type} .#{type}").click((e) ->
        text = $(@).text().trim()
        text = text.split('|')[0] if type == SID
        $('[name=p\\.val]').val(text)
        params = paramsObj()
        # timeline
        paintProcureUnitInTimeline(params['p.type'], $(@).text().trim())

        switch(TOPTAB.active())
          when 'root'
            sale_line(params)
          when 'basic'
            unit_line(params)
          else
            console.log('skip')


        if type == SID
          ss_line(params)
          turn_line(params)
      )

      if type == SID
        #绑定 ps 修改功能
        bindSIDPsBtn()
      else if type == SKU
        pageViewDefaultContent()

      LoadMask.unmask();
    )

  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {a_ss: '双击查看 Selling 的 PageView & Session', a_turn: '请双击需要查看的 Selling 查看转化率'}
      $('#' + id).empty().append(template).find('h3').html(v)


  # init
  $('#a_from').data('dateinput').setValue(defaultDate)
  $('#a_to').data('dateinput').setValue(now)

  # 下载一段时间内的 sku 销量数据
  $("#skus_csv").click (e) ->
    return false if !confirm("数据比较多, 需要点时间, 是否继续?")
    window.open("/analyzes/allSkuCsv?#{$('#click_param :input').fieldSerialize()}")
    e.preventDefault()

  # 清理缓存
  $('#clear_cache').click (e) ->
    return false if !confirm('确认需要清楚缓存?')
    $.post('/analyzes/clear', (r) ->
      if r.flag is true
        window.location.reload()
    )
    e.preventDefault()

  # 给 搜索 按钮添加事件
  $('#a_search').click (e) ->
    e.preventDefault()
    sellRankLoad()

  # 重新加载全部的销售线
  $('#all_search').click (e) ->
    e.preventDefault()
    unit_line(paramsObj())
    sale_line(paramsObj())

  # 为页面下方的 Tab 切换添加事件
  BELOWTAb.tabs().find('[data-toggle=tab]').on('shown', (e) ->
    tabId = $(e.target).attr('href').substr(1)
    $('[name=p\\.type]').val(tabId)
    sellRankLoad() if $("##{tabId}").html() == "wait"
  )

  # 为页面上访的曲线 Tab 添加切换事件
  TOPTAB.tabs().find('[data-toggle=tab]').on('shown',
  (e) ->
    tabId = $(e.target).attr('href').substr(1)
    if tabId is 'basic'
      # tab[basic] 中所存在的需要重新绘制的 div 元素 id
      for id in ['a_units', 'a_turn', 'a_ss', 'tl']
        div = $("##{id}")
        # timeline 的重新绘制
        if id is 'tl'
          div.data('timeline')?.layout()
        else
          if div.data('char')
            div.data('char')?.redraw()
          else
            unit_line(paramsObj()) if id == 'a_units'
    else if tabId is 'root'
      # tab[root] 中所存在的需要重新绘制的 div 元素 id
      for id in ['a_sales']
        div = $("##{id}")
        if div.data('char')
          div.data('char')?.redraw()
        else
          sale_line(paramsObj())
  )

  # ---------- 初始化加载页面数据 --------------
  # 最下方的 Selling[MerchantSKU, SKU] 列表信息
  sellRankLoad()
  # 销量线
  unit_line(paramsObj())
  # 默认 PageView 线
  pageViewDefaultContent()

