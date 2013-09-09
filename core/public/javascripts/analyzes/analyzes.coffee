$ ->
  Highcharts.setOptions(global:{useUTC: false})

  # table 数据列表
  $("#below_tabContent").on("ajaxFresh","#sid,#sku",() ->
      $div =$(@)
      $("#postType").val($div.attr("id"))
      LoadMask.mask()
      $div.load("/Analyzes/analyzes",$('.search_form').serialize(),(r) ->
        LoadMask.unmask()
      )

    #分页事件 bootstrap_pager.html
  ).on("click",".pagination a[page]",(e) ->
     e.preventDefault()
     $a = $(@)
     $('#postPage').val($a.attr('page'))
     ajaxFreshAcitveTableTab()

    #SKU | SID 项目的详细查看事件
  ).on("click",".sid,.sku",(e) ->
     $td = $(@)
     sidOrSku = $td.text().trim()
     $('#postVal').val(sidOrSku)
     $postType = $('#postType')

     #绘制单个Selling的曲线图时,去掉Category条件
     $categoryNode = $('select[name|="p.categoryId"]')
     categoryId = $categoryNode.val()
     $categoryNode.val("")
     ajaxSaleUnitLines()
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

   #列排序事件
  ).on('click', 'th[orderby]', (e) ->
      $td = $(@)
      $('#postOrderBy').val($td.attr('orderby'))
      ajaxFreshAcitveTableTab()
  )



  #当前选中的tab，调用相对应数据
  ajaxFreshAcitveTableTab = ->
     type = $("#below_tab li.active a").attr("href")
     $("#{type}").trigger("ajaxFresh")

  #  Tab 切换添加事件 bootstrap  shown 事件：点击后触发，ajaxFreshAcitveTableTab()不然会得到旧的TYPE
  $('a[data-toggle=tab]').on('shown', (e) ->
    $('#postPage').val(1)
    ajaxFreshAcitveTableTab()
  )

  # 绑定 sid tab 中修改 ps 值
  $('#sid').on('change', 'input[ps]', () ->
    LoadMask.mask()
    $.ajax("/analyzes/ps", {type: 'POST', data: {sid: $(@).attr('sid'), ps: $(@).val()}, dataType: 'json'})
      .done((r) ->
        if r.flag is false
          noty({text: r.message, type: 'error', timeout: 3000})
        else
          noty({text: "修改成功！", type: 'success', timeout: 3000})
        LoadMask.unmask()
      )
  )

  # Form 搜索功能
  $(".search_form").on("change","[name=p\\.market]",(e) ->
      ajaxFreshAcitveTableTab()

   #搜索按钮
  ).on("click",".btn:contains(搜索)",(e) ->
    e.preventDefault()
    if $('select[name|="p.categoryId"]').val() is ''
       $("#postVal").val('all')
    else
       ajaxSaleUnitLines()

    ajaxFreshAcitveTableTab()

   #重新加载全部的销售线
  ).on("click",".btn:contains(Reload)",(e) ->
    e.preventDefault()
    ajaxSaleUnitLines()
  )


  #parameters：
    # headName ：标题名称   yName : Y轴名称   plotEvents ：曲线数据节点的事件   noDataDisplayMessage ：无数据时的提示文字
  $("#basic").on('ajaxFresh', '#a_units, #a_turn, #a_ss', (e, headName, yName, plotEvents, noDataDisplayMessage) ->
    $div = $(@)
    LoadMask.mask()
    $.ajax("/analyzes/#{$div.data("method")}", {type: 'GET', data: $('.search_form').serialize(), dataType: 'json'})
      .done((r) ->
       if r['series'].length != 0
        $div.highcharts('StockChart', {
          credits:
             text:'EasyAcc'
             href:''
          title:
            text: headName
          legend:
            enabled: true
          navigator:
            enabled: false
          scrollbar:
            enabled: false
          rangeSelector:
            enabled: false
          xAxis:
            type: 'datetime'
          yAxis: [{},{labels: {format: '{value}'}, opposite: true},title:{text:yName}]
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
       LoadMask.unmask()
      )
      .fail((xhr, text, error) ->
        noty({text: "Load #{$div.attr('id')} #{error} because #{xhr.responseText}", type: 'error', timeout: 3000})
        LoadMask.unmask()
      )
  )

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
    $("#a_units").trigger("ajaxFresh",[head,"Units",{},'没有数据, 无法绘制曲线...'])

  # 转换率的曲线
  ajaxTurnOverLine = ->
    #无数据提示
    noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">请双击需要查看的 Selling 查看转化率</h3></div>'
    $("#a_turn").trigger("ajaxFresh",['Selling['+$('#postVal')+'] 转化率', "转化率", {}, noDataDisplay])

  # 绘制 Session 的曲线
  ajaxSessionLine = ->
    #无数据提示
    noDataDisplay = '<div class="alert alert-success"><h3 style="text-align:center">双击查看 Selling 的 PageView & Session</h3></div>'
    $("#a_ss").trigger("ajaxFresh",['Selling['+$('#postVal')+'] SS', "Session && PV", {}, noDataDisplay])

  # 清理 Session 转化率与 PageView
  pageViewDefaultContent = () ->
    template = '<div class="alert alert-success"><h3 style="text-align:center"></h3></div>'
    for id,v of {a_ss: '双击查看 Selling 的 PageView & Session', a_turn: '请双击需要查看的 Selling 查看转化率'}
      $('#' + id).empty().append(template).find('h3').html(v)

  # 绘制 ProcureUnit 的 timeline 中的数据
  paintProcureUnitInTimeline = (type, val)->
    LoadMask.mask()
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
        LoadMask.unmask()
    )


  # 页面 初始化数据
  ajaxFreshAcitveTableTab()
  ajaxSaleUnitLines()
  # 默认 PageView 线
  pageViewDefaultContent()