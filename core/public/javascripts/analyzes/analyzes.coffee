$ ->
  MSKU = 'msku'
  SKU = 'sku'

  init = () ->
    $('#a_toolbar :input[type=date]').dateinput({format: 'mm/dd/yyyy'})
    $('a[rel=popover]').popover()

    preMonth = $.DateUtil.addDay(-30)
    now = new Date()
    $('#a_from').data('dateinput').setValue(preMonth)
    $('#a_to').data('dateinput').setValue(now)

    # 最下方的 Selling[MerchantSKU, SKU] 列表信息
    sellRankLoad(MSKU, 1)
    sellRankLoad(SKU, 1)
    # 销量线
    sales_line(from: $.DateUtil.fmt1(preMonth), to: $.DateUtil.fmt1(now), msku: 'All', type: 'msku')
    # 默认 PageView 线
    pageViewDefaultContent()


  # 用来构造给 HighChart 使用的默认 options 的方法
  lineOp = (container, yName) ->
    {
    chart: {renderTo: container}
    title: {text: 'Chart Title'}
    xAxis: {type: 'datetime', dateTimeLabelFormats: {day: '%y.%m.%d'}}
    yAxis: {title: {text: yName}, min: 0}
    plotOptions:
      {
      series:
        {
        cursor: 'pointer',
        point: {events: {}}
        }
      }
    tooltip: {}
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
    }

  # TODO 需要处理传入的函数中的 this 关键字的问题, 解决格式化输出 !
  sellOp = lineOp('a_units', 'Units').click(
    ->
      msku = localStorage.getItem('msku')
      window.open('/analyzes/pie?msku=' + msku + '&date=' + $.DateUtil.fmt1(new Date(@x)),
        msku,
        'width=520,height=620,location=yes,status=yes'
      )
  ).formatter(
    ->
      cur = new Date(@x)
      '<strong>' + @series.name + '</strong><br/>' +
      'Date: ' + ($.DateUtil.fmt1(cur)) + '<br/>' +
      'Sales: ' + @y
  )

  saleOp = lineOp('a_sales', 'Sales').click(
    ->
      alert(@series.name + ":::::" + @x + ":::" + @y)
  ).formatter(
    ->
      cur = new Date(@x)
      '<strong>' + @series.name + '</strong><br/>' +
      'Date: ' + ($.DateUtil.fmt1(cur)) + '<br/>' +
      'Sales: ' + @y
  )

  pvOp = lineOp('a_pv', 'PageView').click(
    ->
      alert('点击了这按钮')
  )

  ssOp = lineOp('a_ss', 'Session')


  pvSS_line = (params) ->
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
          pvOp.head('Selling[' + params['msku'] + '] PV')
          ssOp.head('Selling[' + params['msku'] + '] SS')
          pvOp.clearLines()
          ssOp.clearLines()
          for k,v of r
            lines[k].data.push([o['_1'], o['_2']]) for o in v
            pvOp.series.push(lines[k]) if k.indexOf('pv') >= 0
            ssOp.series.push(lines[k]) if k.indexOf('ss') >= 0
          new Highcharts.Chart(pvOp)
          new Highcharts.Chart(ssOp)
    )


  sales_line = (params) ->
    maskDiv = $('#myTabContent')
    maskDiv.mask('加载中...')
    $.getJSON('/analyzes/ajaxSells', params, (data)
      ->
        display_sku = params['msku']
        prefix = "Selling [<span style='color:orange'>" + display_sku + "</span> | " + params['type'].toUpperCase() + "]"
        sellOp.head(prefix + ' Sales')
        saleOp.head(prefix + ' Prices')
        sellOp.clearLines()
        saleOp.clearLines()

        # 处理一条一条的曲线
        dealLine = (lineName, defOp) ->
          return false if !data['series_' + lineName]
          line = name: lineName.toUpperCase(), data: []
          for d in [data['days']..1]
            line.data.push([
              $.DateUtil.addDay(-d + 1, $('#a_to').data('dateinput').getValue()).getTime(),
              data['series_' + lineName].shift()
            ])
          defOp.series.push(line)
          false

        dealLine('all', sellOp)
        dealLine('auk', sellOp)
        dealLine('ade', sellOp)
        dealLine('afr', sellOp)

        dealLine('allM', saleOp)
        dealLine('aukM', saleOp)
        dealLine('adeM', saleOp)
        dealLine('afrM', saleOp)

        localStorage.setItem('msku', params['msku'])
        new Highcharts.Chart(sellOp)
        new Highcharts.Chart(saleOp)
        maskDiv.unmask()
    )


  # Ajax Load 页面下方的 MSKU 与 SKU 两个 Tab 的数据.
  sellRankLoad = (type, page) ->
    if type isnt MSKU and type isnt SKU
      alert("只允许 msku 与 sku 两种类型!")
      return false

    tgt = $('#' + type)
    tgt.mask('加载中...')
    tgt.load('/analyzes/index_' + type, {'p.page': page, 'p.size': 10, "p.param": $('#a_param').val()},
      ->
        try
        # Selling 的 Ajax line 双击事件
          $('.msku,.sku').unbind().dblclick((e)
            ->
              o = $(e.target)
              $.varClosure.params = {type: o.attr('class')}
              # sku 类型不参加 sid 与 msku 的选择
              accId = o.attr('aid')
              $('#a_acc_id').val(accId)
              $('#a_msku').val(o.attr('title'))
              $('#dbcick_param :input').map($.varClosure)

              #绘制销量线
              #-----
              # PV & SS 线
              if $.varClosure.params['type'] is 'msku'
                pvSS_line($.varClosure.params)
              else
                pageViewDefaultContent()

              display =
                0: 'EasyAcc'
                1: 'EasyAcc.EU'
                2: 'EasyAcc.DE'
              $('#a_acc_id_label').html(display[accId])
          )
        finally
          tgt.unmask()
    )

  # 给 搜索 按钮添加事件
  $('#a_search').click(
    ->
      tab_type = $('#tab li.active a').attr('href').substring(1)
      page = $('#pagefooter_sku').val() - 1
      sellRankLoad(tab_type, if page <= 0 then 1 else page)
      false
  )

  $('#a_param').keyup(
    (e) =>
      $('#a_search').click() if e.keyCode is 13
      false
  )

  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center">请双击需要查看的 Selling 查看 PageView & Session</h3></div>'
    for id in ['a_pv', 'a_ss']
      $('#' + id).html(template)


  # 在最上面定义 init 方法,只能在最后调用 init 方法, 否则会报告方法未定义
  # 因为使用 Coffee Script 定义的 function 都是以定义变量的形式赋值 function, 与直接定义 function 不一样
  init()

