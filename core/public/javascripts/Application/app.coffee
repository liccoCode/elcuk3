$ ->
  $('#cci').click ->
    $.post('/application/clearCache', {},
      (r) ->
        if r.flag then alert('清理首页缓存成功') else alert('清理失败.')
    )

  $('#change_passwd_btn').click (e) ->
    params = $.formArrayToObj($('#change_passwd form').formToArray())
    if params['u.password'] isnt params['u.confirm']
      alert('两次密码不一致!')
      e.preventDefault()
      return false

    maskDiv = $('#change_passwd')
    maskDiv.mask("更新中...")
    $.post('/users/passwd', params,
      (data) ->
        if data['username'] is params['u.username']
          alert('更新成功')
        else
          alert('更新失败;\r\n' + JSON.stringify(data))
        maskDiv.unmask()
    )
    e.preventDefault()


  pieOpBuilder = (id, title, aid) ->
    chart:
      renderTo: id
      type: 'pie'
    title: {text: title}
    tooltip:
      formatter: ->
        "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%<br/>OrderItems: #{@y} / #{@total}"
    plotOptions:
      pie:
        cursor: 'point'
        dataLabels:
          enabled: true
          color: '#000'
          formatter: ->
            "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%"
    series: []
    aid: aid
    clearLines: () ->
      @series = []

  cat = '<span style="color:#F67300">销量</span>'
  sales = '<span style="color:#F67300">销售额</span>'
  pieAll = pieOpBuilder('cat_percent', "类别#{cat}百分比", 0)
  pieUK = pieOpBuilder('cat_percent_uk', "EasyAcc.U 类别#{cat}百分比", 1)
  pieDE = pieOpBuilder('cat_percent_de', "EasyAcc.D 类别#{cat}百分比", 2)

  salesALL = pieOpBuilder('sales_percent', "类别#{sales}百分比", 0)
  salesUK = pieOpBuilder('sales_percent_uk', "EasyAcc.U 类别#{sales}百分比", 0)
  salesDE = pieOpBuilder('sales_percent_de', "EasyAcc.D 类别#{sales}百分比", 0)

  # category 百分比
  loadCategoryAndSalePercent = (pieTuple, date = $.DateUtil.fmt2(new Date())) ->
    mask = $('#orders')
    mask.mask('加载中...')
    # aid 的值随便使用一个 pieTuple 即可, 因为 category 与 sales 这种计算是统一的
    $.get('/application/categoryPercent', {date: date, aid: pieTuple['o'].aid},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          tupleKey = o: '_2', s: '_3'
          for k, v of pieTuple
            line = data: []
            line.data.push([o['_1'], o[tupleKey[k]]]) for o in r
            v.clearLines()
            v.series.push(line)
            try
              new Highcharts.Chart(v)
            catch e #权限控制
              console.log(e)
        mask.unmask()
    )

  # 重新绘制所有的 Pie 图
  drawPies = (date) ->
    loadCategoryAndSalePercent({o: pieAll, s: salesALL}, date)
    loadCategoryAndSalePercent({o: pieUK, s: salesUK}, date)
    loadCategoryAndSalePercent({o: pieDE, s: salesDE}, date)

  lastDate = $("#orders tr:last td:eq(0)").attr('date')
  drawPies(lastDate)

  #为table 中的日期添加查看指定天的 类别百分比数据
  $('#orders td[date]').css('cursor', 'pointer').click ->
    drawPies(@getAttribute('date'))



