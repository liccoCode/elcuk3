$ ->
  Highcharts.setOptions(global:
    {useUTC: false})
  SKU = 'sku'
  SID = 'sid'
  # 这里的 31 天是与 sku/sid 的分析数据中的 day30 所计算的数据保持一致
  defaultDate = $.DateUtil.addDay(-31)
  now = $.DateUtil.addDay(31, defaultDate)

  class Tabs
    constructor: (name) ->
      @tabs_id = "#{name}_tab"
      @content_id = "#{name}_tabContent"

    tabs: ->
      $("##{@tabs_id}")
    content: ->
      $("##{@content_id}")
    active: ->
      $("##{@tabs_id} [data-toggle=tab]").parent().filter('.active').find('a').attr('href')[1..-1]
    contentSelector: ->
      "#" + @content_id
    clearSessionStoreage: ->
      delete sessionStorage[@contentSelector() + "_times"]
      @


  # 获取下面的 Tab 元素组合
  BELOWTAB = new Tabs("below").clearSessionStoreage()

  # 获取上面的 Tab 元素组合
  TOPTAB = new Tabs("top").clearSessionStoreage()

  # 用来构造给 HighChart 使用的默认 options 的方法
  lineOp = (container, yName) ->
    chart:
      renderTo: container
    title:
      text: 'Chart Title'
    xAxis:
      type: 'datetime'
    yAxis:
      title:
        text: yName
      min: 0
    plotOptions:
      series: # 需要从服务器获取开始时间
        cursor: 'pointer'
        point:
          events:
            {}
    tooltip:
      shared: true
      formatter: ->
        s = "<b>#{Highcharts.dateFormat('%Y-%m-%d', @x)}</b><br>"
        @points.forEach((point) ->
          totalY = point.series.yData.reduce((a, b)->
            a + b
          )
          s += "<span style=\"color:#{point.series.color}\">#{point.series.name}</span>: <b>#{point.y} (#{totalY})</b><br/>"
        )
        s
      crosshairs: true
      xDateFormat: '%Y-%m-%d'
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
    id: ->
      container

  # 销售订单曲线
  newSaleUnitLines = ->
    lineOp('a_units', 'Units').click(->
      val = paramsObj()['p.val']
      window.open('/analyzes/pie?msku=' + val + '&date=' + $.DateUtil.fmt2(new Date(@x)),
      val,
      'width=520,height=620,location=yes,status=yes'
      )
    )

  # 销售销量曲线
  newSalesLines = ->
    lineOp('a_sales', 'Sales(USD)').click(->
      alert(@series.name + ":::::" + @x + ":::" + @y)
    )

  # 转换率的曲线
  turnOverLine = lineOp('a_turn', '转化率')

  # Session 数量曲线
  sessionLine = lineOp('a_ss', 'Session && PV')

  # 获取页面 Form 表单参数
  paramsObj = () ->
    $.formArrayToObj($('#click_param').formToArray())

  # 绘制 Session 的曲线
  ss_line = (params) ->
    LoadMask.mask(TOPTAB.contentSelector())
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
        LoadMask.unmask(TOPTAB.contentSelector())
    )

  # 绘制转换率曲线
  turn_line = (params) ->
    LoadMask.mask(TOPTAB.contentSelector())
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
        LoadMask.unmask(TOPTAB.contentSelector())
    )

  # 绘制销量曲线
  unit_line = (params) ->
    LoadMask.mask()

    if params['p.categoryId'] is ''
      displayStr = params['p.val']
    else
      displayStr = 'Category:' + params['p.categoryId']
      params['p.val'] = params['p.categoryId']

    $.getJSON('/analyzes/ajaxUnit', params, (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          unitLines = newSaleUnitLines()
          if r['series'].length == 0
            $('#' + unitLines.id()).html('没有数据, 无法绘制曲线...')
          else
            unitLines.head("Selling [<span style='color:orange'>" + displayStr + "</span> | " + params['p.type']?.toUpperCase() + "] Unit Order")
            names =
              unit_all: 'Unit Order(all)'
              unit_amazon_uk: 'Unit Order(uk)'
              unit_amazon_de: 'Unit Order(de)'
              unit_amazon_fr: 'Unit Order(fr)'
              unit_amazon_us: 'Unit Order(us)'
              unit_amazon_it: 'Unit Order(it)'
              unit_amazon_es: 'Unit Order(es)'
            # 将曲线的名字更换为可读性更强的
            r['series'].map((l) ->
              l.name = names[l.name])
            for line in r['series']
              unitLines.series.push(line)
            $('#' + unitLines.id()).data('char', new Highcharts.Chart(unitLines));
      finally
        LoadMask.unmask()
    )

  # 绘制销售额曲线
  sale_line = (params) ->
    LoadMask.mask()
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
            sale_amazon_uk: 'Sales(uk)'
            sale_amazon_de: 'Sales(de)'
            sale_amazon_fr: 'Sales(fr)'
            sale_amazon_us: 'Sales(us)'
            sale_amazon_es: 'Sales(es)'
            sale_amazon_it: 'Sales(it)'
          r['series'].map((l) ->
            l.name = names[l.name])
          for line in r['series']
            salesLines.series.push(line)
          #          salesLines.xStart(r['pointStart'])
          $('#' + salesLines.id()).data('char', new Highcharts.Chart(salesLines))
      finally
        LoadMask.unmask()
    )

  # 清理 Session 转化率与 PageView
  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {a_ss: '双击查看 Selling 的 PageView & Session', a_turn: '请双击需要查看的 Selling 查看转化率'}
      $('#' + id).empty().append(template).find('h3').html(v)

  # 绘制 ProcureUnit 的 timeline 中的数据
  paintProcureUnitInTimeline = (type, val)->
    LoadMask.mask(BELOWTAB.contentSelector())
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: val},
    (r) ->
      try
        if r.flag is false
          alert(r.message)
        else
          eventSource = $('#tl').data('source')
          eventSource.clear()
          eventSource.loadJSON(r, '/')
      finally
        LoadMask.unmask(BELOWTAB.contentSelector())
    )

  # 绑定 sid tab 中修改 ps 值
  $('#sid').on('change', 'input[ps]', () ->
    LoadMask.mask()
    $.post('/analyzes/ps', {sid: $(@).attr('sid'), ps: $(@).val()})
      .done((r) ->
        if r.flag is false
          alert(r.message)
        LoadMask.unmask()
      )
  )

  # Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据
  sellRankLoad = (params)->
    LoadMask.mask(BELOWTAB.contentSelector())
    $("##{params['p.type']}").load('/Analyzes/analyzes', $.param(params),
    LoadMask.unmask(BELOWTAB.contentSelector())
    )

  # SKU | SID 项目的详细查看事件
  $('#below_tabContent').on('click', '.sid,.sku', (e) ->
    $self = $(@)
    sidOrSku = $self.text().trim()
    params = paramsObj()
    params['p.val'] = sidOrSku

    #timeline
    paintProcureUnitInTimeline(params['p.type'], sidOrSku)

    # 销量图或者销售额图
    switch(TOPTAB.active())
      when 'root'
        sale_line(params)
      when 'basic'
      #绘制单个Selling的曲线图时,去掉Category条件
        params['p.categoryId'] = ''
        unit_line(params)
      else
        console.log('skip')

    # 转换率与 PageView
    if params['p.type'] == SID
      ss_line(params)
      turn_line(params)
    else
      pageViewDefaultContent()

    # 选中
    $self.parents('table').find('tr').removeClass('selected')
    $self.parents('tr').addClass('selected')
  )

  # 为 SKU | SID 项目添加排序事件
  $('#below_tabContent').on('click', 'th.sortable', (e) ->
    $('[name=p\\.desc]').val(-> return if $(@).val() == 'false' then true else false)
    $('[name=p\\.orderBy]').val($(@).attr('name'))
    sellRankLoad(paramsObj())
  )

  # 为 SKU | SID 添加分页事件
  $('#below_tabContent').on('click', 'div.pagination a', (e) ->
    $('[name=p\\.page]').val($(@).attr('page'))
    sellRankLoad(paramsObj())
  )

  # init
  $('#a_from').data('dateinput').setValue(defaultDate)
  $('#a_to').data('dateinput').setValue(now)

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
    params = paramsObj()
    if params['p.categoryId'] is ''
      params['p.val'] = 'all'
    else
      unit_line(params)
    #重新搜索数据、加载曲线图
    sellRankLoad(params)

  # 重新加载全部的销售线
  $('#all_search').click (e) ->
    e.preventDefault()
    switch(BELOWTAB.active())
      when 'root'
        sale_line(paramsObj())
      else
        unit_line(paramsObj())

  # 为页面下方的 Tab 切换添加事件
  BELOWTAB.tabs().on('shown', '[data-toggle=tab]',(e) ->
    tabId = $(e.target).attr('href').substr(1)
    params = paramsObj()
    params['p.type'] = tabId
    $('[name=p\\.type]').val(tabId)
    if $("##{tabId}").html() is 'wait'
      params['p.val'] = 'all' if $('[name=p\\.categoryId]').val() is ''
      sellRankLoad(params)
  ).find('a:first').trigger('shown')

  # 为页面上访的曲线 Tab 添加切换事件
  TOPTAB.tabs().find('[data-toggle=tab]').on('shown', (e) ->
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
  # 销量线
  unit_line(paramsObj())
  # 默认 PageView 线
  pageViewDefaultContent()
