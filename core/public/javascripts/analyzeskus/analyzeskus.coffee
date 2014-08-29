# timeline/timeline_js/timeline-api.js 中覆盖了 JQuery对象，所以重置
window.jQuery = window.$
$.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"
$ ->
  Highcharts.setOptions(global:{useUTC: false})




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
    postval = $("#postVal").val()
    if postval==''
      alert '请添加SKU'
    else
      if $('select[name|="p.categoryId"]').val() is ''
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
    $.ajax("/analyzeskus/#{$div.data("method")}", {type: 'GET', data: $('.search_form').serialize(), dataType: 'json'})
      .done((r) ->
        if r.flag == false
          noty({text: r.message.split("|F")[0], type: 'warning', timeout: 5000})
        else if r['series'].length != 0
          $div.highcharts('StockChart', {
            credits:
              text:'EasyAcc'
              href:''
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
              buttons: [{type:'week', count: 1, text: '1w'}, {type:'month', count: 1, text: '1m'}]
              selected: 1
            xAxis:
              type: 'datetime'
            yAxis: { min: 0 }
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
  ).on('click', '.btn-toolbar > .btn-small', (e) ->
    $btn = $(@)
    events = _.filter($('#a_units').highcharts().series, (serie) ->
      serie.name in ["#{$btn.text()}亚马逊 滑动平均", '滑动平均 汇总']
    )
    if events.length == 0
      LoadMask.mask()
      $market = $("[name='p.market']")
      $market.data('oldMarket', $market.val()).val($btn.data('market'))
      $.ajax('/analyzes/ajaxMovingAve', {type: 'GET', data: $('.search_form').serialize(), dataType: 'json'})
        .done((r) ->
          $market.val($market.data('oldMarket'))
          if r.flag == false
            noty({text: r.message.split("|F")[0], type: 'warning', timeout: 5000})
          else
            _.each(r['series'], (ele) ->
              $('#a_units').highcharts().addSeries({name: ele['name'], data: ele['data']})
            )
          LoadMask.unmask()
        )
    else
      _.each($('#a_units').highcharts().series, (serie) ->
        serie.remove() if serie.name in ["#{$btn.text()}亚马逊 滑动平均", '滑动平均 汇总']
      )
  )

  # 销售订单曲线
  ajaxSaleUnitLines = ->
    categoryId = $('select[name|="p.categoryId"]').val()


    head = "SKU Unit Order"
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



  $('#skusearch').change (e) ->
    $input = $(@)
    postval = $("#postVal").val()
    if $input.data('products') is undefined
       $input.data('products', $input.data('source'))
    return false if !(@value in $input.data('products'))
    return false if postval.indexOf(@value) > 0

    trcount = $("#skutable tr").length

    gettr = document.getElementById("skutable").rows[trcount-1]
    gettr.innerHTML+="<td  colspan=1><a href='javascript:;' rel='tooltip'>"+@value+"</a> <a name='skudelete' copItemId='"+@value+"' class='btn btn-mini delelte'><i class='icon-remove'></i></a></td>"

    tdcount = gettr.getElementsByTagName("td").length
    if tdcount!=0 and tdcount % 6==0
      $("#skutable").append("<tr  class='table table-condensed table-bordered'></tr>")

    $("#postVal").val(postval+","+@value)



  $('#skutable').on('click', "[name='skudelete']", ->
    $sku = $(@)
    skuvalue = $sku.attr("copItemId")
    postval = $("#postVal").val()
    postval = postval.replace(","+skuvalue,'')
    $("#postVal").val(postval)


    $sku.parent("td").remove()

    )

  ajaxSaleUnitLines()