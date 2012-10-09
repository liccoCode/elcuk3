$ ->
  SKU = 'sku'
  SID = 'sid'
  defaultDate = $.DateUtil.addDay(-30)
  now = new Date()

  # 获取下面的 Tab 元素组合
  belowTab = ->
    tab: $('#below_tab')
    content: $('#below_tabContent')

  # 获取上面的 Tab 元素组合
  topTab = ->
    tab: $('#top_tab')
    content: $('#top_tabContent')

  # 用来构造给 HighChart 使用的默认 options 的方法
  lineOp = (container, yName) ->
    chart: {renderTo: container}
    title: {text: 'Chart Title'}
    xAxis: {type: 'datetime', dateTimeLabelFormats: {day: '%y.%m.%d'}}
    yAxis: {title: {text: yName}, min: 0}
    plotOptions:
      series:
        cursor: 'pointer',
        point: events: {}
    tooltip:
      shared: true
      crosshairs: true
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
    mouseOut: (func) ->
      @plotOptions.series.point.events.mouseOut = func
      @
    formatter: (func) ->
      @tooltip.formatter = func
      @
    clearLines: () ->
      @series = []
    id: -> container

  # 销售订单曲线
  unitOp = lineOp('a_units', 'Units').click(
    ->
      val = paramsObj()['p.val']
      window.open('/analyzes/pie?msku=' + val + '&date=' + $.DateUtil.fmt2(new Date(@x)),
        val,
        'width=520,height=620,location=yes,status=yes'
      )
  )

  # 销售销量曲线
  saleOp = lineOp('a_sales', 'Sales(USD)').click(
    ->
      alert(@series.name + ":::::" + @x + ":::" + @y)
  )

  # 转换率的曲线
  turnOp = lineOp('a_turn', '转化率')

  # Session 数量曲线
  ssOp = lineOp('a_ss', 'Session && PV')

  paramsObj = () -> $.formArrayToObj($('#click_param').formToArray())
  # ------------------- 曲线绘制 ---------------------
  # 绘制 Session 的曲线
  ss_line = (params) ->
    $.getJSON('/analyzes/ajaxSellingRecord', params, (r) ->
        if r.flag is false
          alert(r.message)
        else
          lines =
            pv_uk: {name: 'PageView(uk)', data: []}
            pv_de: {name: 'PageView(de)', data: []}
            pv_fr: {name: 'PageView(fr)', data: []}
            ss_uk: {name: 'Session(uk)', data: []}
            ss_de: {name: 'Session(de)', data: []}
            ss_fr: {name: 'Session(fr)', data: []}
          ssOp.head('Selling[' + params['p.val'] + '] SS')
          ssOp.clearLines()
          for k,v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            ssOp.series.push(lines[k])
          $('#' + ssOp.id()).data('char', new Highcharts.Chart(ssOp))
    )

  # 绘制转换率曲线
  turn_line = (params) ->
    $.getJSON('/analyzes/ajaxSellingTurn', params, (r) ->
        if r.flag is false
          alert(r.message)
        else
          lines =
            tn_uk: {name: 'TurnRatio(uk)', data: []}
            tn_de: {name: 'TurnRatio(de)', data: []}
            tn_fr: {name: 'TurnRatio(fr)', data: []}
          turnOp.head('Selling[' + params['p.val'] + '] 转化率')
          turnOp.clearLines()
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v # 填充完曲线数据
            turnOp.series.push(lines[k]) # 添加曲线
          $('#' + turnOp.id()).data('char', new Highcharts.Chart(turnOp))
    )

  # 绘制销量曲线
  unit_line = (params) ->
    maskDiv = belowTab().content
    maskDiv.mask('加载中...')
    $.getJSON('/analyzes/ajaxUnit', params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          display_sku = params['p.val']
          unitOp.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['p.type']?.toUpperCase() + "] Unit Order")
          unitOp.clearLines()
          lines =
            unit_all: {name: 'Unit Order(all)', data: []}
            unit_uk: {name: 'Unit Order(uk)', data: []}
            unit_de: {name: 'Unit Order(de)', data: []}
            unit_fr: {name: 'Unit Order(fr)', data: []}
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            unitOp.series.push(lines[k])
          $('#' + unitOp.id()).data('char', new Highcharts.Chart(unitOp))
        maskDiv.unmask()
    )

  # 绘制销售额曲线
  sale_line = (params) ->
    maskDiv = belowTab().content
    maskDiv.mask('加载中...')
    $.getJSON('/analyzes/ajaxSales', params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          display_sku = params['p.val']
          saleOp.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['p.type']?.toUpperCase() + "] Sales(USD)")
          saleOp.clearLines()
          lines =
            sale_all: {name: 'Sales(all)', data: []}
            sale_uk: {name: 'Sales(uk)', data: []}
            sale_de: {name: 'Sales(de)', data: []}
            sale_fr: {name: 'Sales(fr)', data: []}
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            saleOp.series.push(lines[k])
          $('#' + saleOp.id()).data('char', new Highcharts.Chart(saleOp))
        maskDiv.unmask()
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
    maskEl = belowTab().content
    maskEl.mask('加载中...')
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: val},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
        #eventSource.loadJSON(json, url)
          eventSource = $('#tl').data('source')
          eventSource.clear()
          eventSource.loadJSON(r, '/')
        maskEl.unmask()
      #$('#tl').data('timeline').paint()
    )

  # Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据.
  sellRankLoad = () ->
    mask = $("#container")
    mask.mask("加载中...")
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
        $("##{type} .#{type}").click(
          (e) ->
            text = $(@).text().trim()
            text = text.split('|')[0] if type == SID
            $('[name=p\\.val]').val(text)

            params = paramsObj()

            # timeline
            paintProcureUnitInTimeline(params['p.type'], $(@).text().trim())

            unit_line(params)
            sale_line(params)

            if type == SID
              ss_line(params)
              turn_line(params)
        )

        if type == SID
          #绑定 ps 修改功能
          bindSIDPsBtn()
        else if type == SKU
          pageViewDefaultContent()


        mask.unmask()
    )

  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {a_ss: '双击查看 Selling 的 PageView & Session', a_turn: '请双击需要查看的 Selling 查看转化率'}
      $('#' + id).empty().append(template).find('h3').html(v)


  # 为页面上方的曲线的 Tab 切换添加事件
  bindTopTabSwitchBtn = ->
    topTab().tab.find('[data-toggle=tab]').on('shown',
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
              div.data('char')?.setSize(div.width(), div.height())
        else if tabId is 'root'
        # tab[root] 中所存在的需要重新绘制的 div 元素 id
          for id in ['a_sales']
            div = $("##{id}")
            div.data('char')?.setSize(div.width(), div.height())

    )

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

  # 为页面下方的 Tab 切换添加事件
  belowTab().tab.find('[data-toggle=tab]').on('shown',
    (e) ->
      tabId = $(e.target).attr('href').substr(1)
      $('[name=p\\.type]').val(tabId)
      sellRankLoad() if $("##{tabId}").html() == "wait"
  )

  # 最下方的 Selling[MerchantSKU, SKU] 列表信息
  sellRankLoad()

  # 销量线
  unit_line(paramsObj())
  sale_line(paramsObj())

  # 默认 PageView 线
  pageViewDefaultContent()
  bindTopTabSwitchBtn()


  # 给 搜索 按钮添加事件
  $('#a_search').click (e) ->
    e.preventDefault()
    sellRankLoad()

  # 重新加载全部的销售线
  $('#all_search').click (e) ->
    e.preventDefault()
    unit_line(paramsObj())
    sale_line(paramsObj())
