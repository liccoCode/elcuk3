# timeline/timeline_js/timeline-api.js 中覆盖了 JQuery对象，所以重置
window.jQuery = window.$
$.extend $.fn.dataTableExt.oStdClasses,
  sWrapper: "dataTables_wrapper form-inline"

$ ->
  Highcharts.setOptions(global: {useUTC: false})
  $("#below_tabContent").on("click", ".sid,.sku", (e) ->
    $td = $(@)
    sidOrSku = $td.text().trim()
    $('#postVal').val(sidOrSku)
    $postType = $('#postType')

    # 绘制单个Selling的曲线图时,去掉Category条件
    $categoryNode = $('select[name|="p.categoryId"]')
    categoryId = $categoryNode.val()
    $categoryNode.val("")
    ajaxSaleUnitLinesForSku()
    $categoryNode.val(categoryId)

    # 转换率与 PageView
    if $postType.val() == "sid"
      ajaxSessionLine()
      ajaxTurnOverLine()
    else
      pageViewDefaultContent()

    #timeline
    paintProcureUnitInTimeline($postType.val(), sidOrSku)

    # 选中 效果
    $td.parents('table').find('tr').removeClass('selected')
    $td.parents('tr').addClass('selected')
  )

  # 当前选中的tab，调用相对应数据
  ajaxFreshAcitveTableTab = ->
    type = $("#below_tab li.active a").attr("href")
    $("#{type}").trigger("ajaxFresh")


  # 绑定 sid tab 中修改 ps 值
  $('#sid').on('change', 'input[ps]', () ->
    $line = $(@)
    LoadMask.mask($line)
    $.ajax("/analyzes/ps", {
      type: 'POST',
      data: {
        sid: $line.attr('sid'),
        ps: $line.val()
      },
      dataType: 'json'
    })
      .done((r) ->
      if r.flag is false
        noty({
          text: r.message,
          type: 'error',
          timeout: 3000
        })
      else
        noty({
          text: "修改成功！",
          type: 'success',
          timeout: 3000
        })
      LoadMask.unmask($line)
    )
  )

  #parameters：
  # headName ：标题名称   yName : Y轴名称   plotEvents ：曲线数据节点的事件   noDataDisplayMessage ：无数据时的提示文字
  $("#basic").on('ajaxFresh', '#a_units, #a_turn, #a_ss', (e, headName, yName, plotEvents, noDataDisplayMessage) ->
    $div = $(@)
    LoadMask.mask($div)
    $.ajax("/analyzes/#{$div.data("method")}", {
      type: 'GET',
      data: $('#click_param').serialize(),
      dataType: 'json'
    })
    .done((r) ->
      if r.flag == false
        noty({
          text: r.message.split("|F")[0],
          type: 'warning',
          timeout: 5000
        })
      else if r['series'].length != 0
        $div.highcharts('StockChart', {
          credits:
            text: 'EasyAcc'
            href: ''
          title:
            text: headName
          legend:
            enabled: true
          navigator:
            enabled: true
          scrollbar:
            enabled: false
          rangeSelector:
            enabled: true
            buttons: [{
              type: 'week',
              count: 1,
              text: '1w'
            }, {
              type: 'month',
              count: 1,
              text: '1m'
            }]
            selected: 1
          xAxis:
            type: 'datetime'
          yAxis: {min: 0}
          plotOptions:
            series: # 需要从服务器获取开始时间
              cursor: 'pointer',
              events: plotEvents
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
          series: r['series']
        })
      else
        $div.html(noDataDisplayMessage)
      LoadMask.unmask($div)
    )
    .fail((xhr, text, error) ->
      noty({
        text: "Load #{$div.attr('id')} #{error} because #{xhr.responseText}",
        type: 'error',
        timeout: 3000
      })
      LoadMask.unmask($div)
    )
  )
    
  $(".btn-xs").click((e) ->
    $btn = $(@)
    $div = $('#a_units')
    events = _.filter($('#a_units').highcharts().series, (serie) ->
      serie.name in ["#{$btn.text()}亚马逊 滑动平均", '滑动平均 汇总']
    )
    if events.length == 0
      LoadMask.mask($div)
      $market = $("[name='p.market']")
      $market.data('oldMarket', $market.val()).val($btn.data('market'))
      $.ajax('/analyzes/ajaxMovingAve', {
        type: 'GET',
        data: $('#click_param').serialize(),
        dataType: 'json'
      })
      .done((r) ->
        $market.val($market.data('oldMarket'))
        if r.flag == false
          noty({
            text: r.message.split("|F")[0],
            type: 'warning',
            timeout: 5000
          })
        else
          _.each(r['series'], (ele) ->
            $div.highcharts().addSeries({
              name: ele['name'],
              data: ele['data']
            })
          )
        LoadMask.unmask($div)
      )
    else
      _.each($('#a_units').highcharts().series, (serie) ->
        serie.remove() if serie.name in ["#{$btn.text()}亚马逊 滑动平均", '滑动平均 汇总']
      )
  )

  ajaxSaleUnitLinesForSku = ->
    $postVal = $('#postVal')
    displayStr = $postVal.val()
    head = "Selling [<span style='color:orange'>#{displayStr}</span> | " + $('#postType').val().toUpperCase() + "] Unit Order"
    $("#a_units").trigger("ajaxFresh", [head, "Units", {}, '没有数据, 无法绘制曲线...'])

  # 销售订单曲线
  ajaxSaleUnitLines = ->
    categoryId = $('select[name|="p.categoryId"]').val()
    $postVal = $('#postVal')
    if categoryId is ''
      displayStr = $postVal.val()
    else
      displayStr = 'Category:' + categoryId
      $postVal.val(categoryId)

    head = "Selling [<span style='color:orange'>#{displayStr}</span> | " + $('#postType').val().toUpperCase() + "] Unit Order"
    $("#a_units").trigger("ajaxFresh", [head, "Units", {}, '没有数据, 无法绘制曲线...'])

  # 转换率的曲线
  ajaxTurnOverLine = ->
# 无数据提示
    noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">请双击需要查看的 Selling 查看转化率</h3></div>'
    $("#a_turn").trigger("ajaxFresh", ['Selling[' + $('#postVal').val() + '] 转化率', "转化率", {}, noDataDisplay])

  # 绘制 Session 的曲线
  ajaxSessionLine = ->
# 无数据提示
    noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">双击查看 Selling 的 PageView & Session</h3></div>'
    $("#a_ss").trigger("ajaxFresh", ['Selling[' + $('#postVal').val() + '] SS', "Session && PV", {}, noDataDisplay])

  # 清理 Session 转化率与 PageView
  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {
      a_ss: '双击查看 Selling 的 PageView & Session',
      a_turn: '请双击需要查看的 Selling 查看转化率'
    }
      $('#' + id).empty().append(template).find('h3').html(v)

  # 绘制 ProcureUnit 的 timeline 中的数据
  paintProcureUnitInTimeline = (type, val)->
    $div = $('#tl')
    LoadMask.mask($div)
    $.post('/analyzes/ajaxProcureUnitTimeline', {
      type: type,
      val: val
    },
      (r) ->
        try
          if r.flag is false
            alert(r.message)
          else
            eventSource = $div.data('source')
            eventSource.clear()
            eventSource.loadJSON(r, '/')
        finally
          LoadMask.unmask($div)
    )

  # Tab 切换添加事件 bootstrap  shown 事件：点击后触发，ajaxFreshAcitveTableTab()不然会得到旧的TYPE
  $('a[data-toggle=tab]').on('shown', (e) ->
    $('#postPage').val(1)
    ajaxFreshAcitveTableTab()
  )

  # 页面 初始化数据
  ajaxFreshAcitveTableTab()
  ajaxSaleUnitLines()
  # 默认 PageView 线
  pageViewDefaultContent()