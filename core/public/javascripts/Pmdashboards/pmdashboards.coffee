$ ->

  # 异常信息
  $("#below_tabContent").on("ajaxFetchAbnormals", "#abnormalInfo", () ->
    if $('input[name="abnormalInfovalue"]').val() isnt "1"
      divs = ["review", "salesQty", "salesAmount", "salesProfit"]
      _.each(divs, (value) ->
        $div = $("##{value}")
        $div.load("/Pmdashboards/#{$div.attr("id")}", (r)->
        )
      )
      $('input[name="abnormalInfovalue"]').val("1")
  )

  # 年份
  year = $('select[name="year"]').val()
  # team
  team = $('select[name="team"]').val()
  # 产品线 id
  cateid = $('select[name="cate"]').val()

  # Category 目标
  $("#below_tabContent").on("ajaxFetchCategorySaleTargets", "#category", () ->
    if $('input[name="categoryvalue"]').val() isnt "1"
      new PieChart("category_column").percent(null, year, team, cateid)
      new LineChart("category_line").percent(null, year, team, cateid)
      $('input[name="categoryvalue"]').val("1")
  )

  # 产品线状态
  $("#below_tabContent").on("ajaxFetchProductInfos", "#product", () ->
    if $('input[name="productvalue"]').val() isnt "1"
      new ProductInfoLineChart("salefee_line").percent('salefeeline', year, team)
      new ProductInfoLineChart("saleqty_line").percent('saleqtyline', year, team)
      $('input[name="productvalue"]').val("1")
  )

  # 年度目标
  $("#below_tabContent").on("ajaxFetchAnnualTargets", "#task", () ->
    if $('input[name="taskvalue"]').val() isnt "1"
      new PieChart("sale_column").percent('salecolumn', year, team)
      new PieChart("profitrate_line").percent('profitrateline', year, team)
      new PercentChart("sale_percent").percent('sale', year, team)
      new PercentChart("profit_percent").percent('profit', year, team)
      new PercentChart("teamsale_percent").percent('teamsale', year, team)
      new PercentChart("teamprofit_percent").percent('teamprofit', year, team)
      $('input[name="taskvalue"]').val("1")

  )

  #  Tab 切换添加事件 bootstrap  shown 事件：点击后触发
  $('a[data-toggle=tab]').on('shown', (e) ->
    triggerTabMethod()
  )

  class PieChart
    constructor: (@container) ->
    percent: (type = 'units', @year, @team, @cid, mask_selector = '#orders') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {cateid: @cid, type: type, year: @year, team: @team}, (p) ->
        title = if p.title == undefined then p['series'][0]['name'] else p.title
        $div.highcharts({
          title: { text: title },
          legend:
            enabled: true
          xAxis:
            type: 'category'
          yAxis: { min: 0 }
          tooltip:
            shared: true
          plotOptions:
            pie:
              dataLabels:
                enabled: true
          series: p['series']
        })
        LoadMask.unmask(mask_selector)
      )

  class LineChart
    constructor: (@container) ->
    percent: (type = 'units', @year, @team, @cid, mask_selector = '#orders') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {cateid: @cid, type: type, year: @year, team: @team}, (r) ->
        $div.highcharts({
          title: { text: r.title },
          legend:
            enabled: true
          xAxis:
            type: 'category'
          yAxis: { min: 0 }
          tooltip:
            shared: true
            crosshairs: true
            xDateFormat: '%Y-%m-%d'
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

  class ProductInfoLineChart
    constructor: (@container) ->

    # units/sales
    percent: (type = 'units', @year, @team, mask_selector = '#orders') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get('/pmdashboards/percent', {type: type, year: @year, team: @team}, (r) ->
        title = r['title']
        console.log(r['series'][0]['name'])
        $div.highcharts({
          title:{ text: title },
          legend:
            enabled: true
          xAxis:
            type: 'datetime'
          yAxis: { min: 0 }
          tooltip:
            shared: true
            crosshairs: true
            xDateFormat: '%Y-%m-%d'
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

  class PercentChart
    constructor: (@container) ->
    percent: (type = 'units', @year, @team, @cid, mask_selector = '#orders') =>
      self = @
      $div = $("##{self.container}")
      LoadMask.mask(mask_selector)
      $.get($div.data("url"), {cateid: @cid, type: type, year: @year, team: @team}, (p) ->
        title = if p.title == undefined then p['series'][0]['name'] else p.title
        $div.highcharts({
          title: { text: title },
          legend:
            enabled: true
          xAxis:
            type: 'category'
          yAxis: { min: 0 }
          tooltip:
            formatter: ->
              "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%<br/>#{@y} / #{@total}"
          plotOptions:
            pie:
              #cursor: 'point'
              dataLabels:
                enabled: true
                #color: '#000'
                formatter: ->
                  "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%"
          series: p['series']
        })
        LoadMask.unmask(mask_selector)
      )


   # 点击搜索按钮
  $('#orders button[name="search"]').click ->
    year = $('select[name="year"]').val()
    team = $('select[name="team"]').val()
    cateid = $('select[name="cate"]').val()
    $('input[name="productvalue"]').val("0")
    $('input[name="abnormalInfovalue"]').val("0")
    $('input[name="categoryvalue"]').val("0")
    $('input[name="taskvalue"]').val("0")
    triggerTabMethod()

  $("#abnormalInfo").on("click", "#salesQtySearch, #salesAmountSearch, #salesProfitSearch", () ->
    # 触发 Ajax 事件
    LoadMask.mask()
    $div = $("##{$(@).data("div")}")
    $div.load("/Pmdashboards/#{$div.attr("id")}",$("##{$(@).data("form")}").serialize(), (r)->
      LoadMask.unmask()
    )
  )

  # 触发当前选中的 tab 的事件
  triggerTabMethod = ->
    type = $("#below_tab li.active a").attr("href")
    $tab = $("#{type}")
    $tab.trigger($tab.data("method"))

  # 页面初始化默认触发 ajaxFetchProductInfos 事件去加载产品线信息图标
  $("#product").trigger("ajaxFetchProductInfos")