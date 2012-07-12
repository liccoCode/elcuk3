$ ->
  MSKU = 'msku'
  SKU = 'sku'
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
      msku = localStorage.getItem('msku')
      window.open('/analyzes/pie?msku=' + msku + '&date=' + $.DateUtil.fmt2(new Date(@x)),
        msku,
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
          ssOp.head('Selling[' + params['msku'] + '] SS')
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
          turnOp.head('Selling[' + params['msku'] + '] 转化率')
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
          display_sku = params['msku']
          unitOp.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['type']?.toUpperCase() + "] Unit Order")
          unitOp.clearLines()
          lines =
            unit_all: {name: 'Unit Order(all)', data: []}
            unit_uk: {name: 'Unit Order(uk)', data: []}
            unit_de: {name: 'Unit Order(de)', data: []}
            unit_fr: {name: 'Unit Order(fr)', data: []}
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            unitOp.series.push(lines[k])
          localStorage.setItem('msku', params['msku'])
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
          display_sku = params['msku']
          saleOp.head("Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['type']?.toUpperCase() + "] Sales(USD)")
          saleOp.clearLines()
          lines =
            sale_all: {name: 'Sales(all)', data: []}
            sale_uk: {name: 'Sales(uk)', data: []}
            sale_de: {name: 'Sales(de)', data: []}
            sale_fr: {name: 'Sales(fr)', data: []}
          for k, v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            saleOp.series.push(lines[k])
          localStorage.setItem('msku', params['msku'])
          $('#' + saleOp.id()).data('char', new Highcharts.Chart(saleOp))
        maskDiv.unmask()
    )

  # -------------------------------------------------------------------

  # 绑定 sid tab 中修改 ps 的事件
  bindSIDPsBtn = ->
    $('#msku input[ps]').unbind().change ->
      tableE = $(@).parents('table')
      tableE.mask('更新 PS 中...')
      $.post('/analyzes/ps', {sid: $(@).attr('sid'), ps: $(@).val()},
        (r) ->
          if r.flag is false
            alert(r.message)
          tableE.unmask()
      )

  # 绘制 ProcureUnit 的 timeline 中的数据
  paintProcureUnitInTimeline = (type, skuOrSid)->
    maskEl = belowTab().content
    maskEl.mask('加载中...')
    $.post('/analyzes/ajaxProcureUnitTimeline', {type: type, val: skuOrSid},
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
  sellRankLoad = (type, page) ->
    if type isnt MSKU and type isnt SKU
      alert("只允许 sid 与 sku 两种类型!")
      return false

    tgt = $('#' + type)
    tgt.mask('加载中...')
    params =
      'p.page': page
      'p.size': 15
      'p.param': $('#a_param').val()
    params['p.aid'] = $('#p_aid :checked').val() if type is 'msku'

    tgt.load('/analyzes/index_' + type, params,
      ->
        try
        # Selling 的 Ajax line 加载事件
          $('.msku,.sku').unbind().click ->
            o = $(@)
            # 处理样式
            o.parents('table').find('tr').removeClass('active')
            o.parent().addClass('active')

            # 收集参数
            $.varClosure.params = {type: o.attr('class')}
            ## sku 类型不参加 sid 与 msku 的选择
            accId = o.attr('aid')
            $('#a_acc_id').val(accId)
            $('#a_msku').val(o.attr('title'))
            $('#dbcick_param :input').map($.varClosure)

            # 绘制销量线
            unit_line($.varClosure.params)
            sale_line($.varClosure.params)
            #  绘制 Timeline 数据
            # 绘制 PV & SS 线
            if $.varClosure.params['type'] is 'msku'
              ss_line($.varClosure.params)
              turn_line($.varClosure.params)
              paintProcureUnitInTimeline($.varClosure.params['type'], o.find('~ .sid').attr('title'))
            else
              pageViewDefaultContent()
              paintProcureUnitInTimeline($.varClosure.params['type'], o.attr('title'))

            display =
              0: 'EasyAcc'
              1: 'EasyAcc.U'
              2: 'EasyAcc.D'
            $('#a_acc_id_label').html(display[accId])
            false

          # sid 中的 ps 修改事件
          bindSIDPsBtn()

          #页脚的翻页事件
          $('div.pagination a').click ->
            sellRankLoad(type, $(@).attr('page'))
            false
        finally
          tgt.unmask()
          window.$ui.init()
    )

  # 给 搜索 按钮添加事件
  $('#a_search').click ->
    tab_type = belowTab().tab.find('li.active a').attr('href').substring(1)
    page = new Number($('#curent_page').html())
    sellRankLoad(tab_type, if page <= 0 then 1 else page)
    false

  # 重新加载全部的销售线
  $('#all_search').click ->
    [from, to] = [$('#a_from').val(), $('#a_to').val()]
    unit_line(from: from, to: to, msku: 'All')
    sale_line(from: from, to: to, msku: 'All')
    false

  $('#a_param').keyup (e) ->
    $('#a_search').click() if e.keyCode is 13
    false

  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {a_ss: '双击查看 Selling 的 PageView & Session', a_turn: '请双击需要查看的 Selling 查看转化率'}
      $('#' + id).empty().append(template).find('h3').html(v)


  # 为页面下方的 Tab 切换添加事件
  bindBelowTabSwitchBtn = ->
    belowTab().tab.find('[data-toggle=tab]').on('shown',
      (e) ->
        tabId = $(e.target).attr('href').substr(1)
        if tabId is 'msku'
          $('#p_aid').removeAttr("disabled")
        else if tabId is 'sku'
          $('#p_aid').attr("disabled", '')
    )
    $("#p_aid")

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

  # 因为使用 Coffee Script 定义的 function 都是以定义变量的形式赋值 function, 与直接定义 function 不一样
  do ->
    $('#a_from').data('dateinput').setValue(defaultDate)
    $('#a_to').data('dateinput').setValue(now)

    # 最下方的 Selling[MerchantSKU, SKU] 列表信息
    sellRankLoad(MSKU, 1)
    sellRankLoad(SKU, 1)
    # 销量线
    unit_line(from: $.DateUtil.fmt2(defaultDate), to: $.DateUtil.fmt2(now), msku: 'All', type: 'msku')
    sale_line(from: $.DateUtil.fmt2(defaultDate), to: $.DateUtil.fmt2(now), msku: 'All', type: 'msku')
    # 默认 PageView 线
    pageViewDefaultContent()
    bindBelowTabSwitchBtn()
    bindTopTabSwitchBtn()
