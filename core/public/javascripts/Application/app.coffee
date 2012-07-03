$ ->
  $('a[rel=tooltip]').tooltip({placement: 'right'})

  $('#cci').click ->
    $.post('/application/clearCache', {},
      (r) ->
        if r.flag then alert('清理首页缓存成功') else alert('清理失败.')
    )

  $('#change_passwd_btn').click ->
    $.varClosure.params = {}
    $('#change_passwd :input').map($.varClosure)
    if $.varClosure.params['u.password'] isnt $.varClosure.params['c_password']
      alert('两次密码不一致!')
      return false

    maskDiv = $('#change_passwd')
    maskDiv.mask("更新中...")
    $.post('/users/passwd', $.varClosure.params,
      (data) ->
        if data['username'] is $.varClosure.params['u.username']
          alert('更新成功')
        else
          alert('更新失败;\r\n' + JSON.stringify(data))
        maskDiv.unmask()
    )
    false


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

  pieAll = pieOpBuilder('cat_percent', '类别销售百分比', 0)
  pieUK = pieOpBuilder('cat_percent_uk', 'EasyAcc.U 类别销量百分比', 1)
  pieDE = pieOpBuilder('cat_percent_de', 'EasyAcc.D 类别销量百分比', 2)

  # category 百分比
  loadCategoryPercent = (pieOp, date = $.DateUtil.fmt2(new Date())) ->
    mask = $('#orders')
    mask.mask('加载中...')
    $.get('/application/categoryPercent', {date: date, aid: pieOp.aid},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          line = data: []
          line.data.push([o['_1'], o['_2']]) for o in r
          pieOp.clearLines()
          pieOp.series.push(line)
          new Highcharts.Chart(pieOp)
        mask.unmask()
    )
  loadCategoryPercent(pieAll)
  loadCategoryPercent(pieUK)
  loadCategoryPercent(pieDE)

  #为table 中的日期添加查看指定天的 类别百分比数据
  $('#orders td[date]').css('cursor', 'pointer').click ->
    loadCategoryPercent(pieAll, @getAttribute('date'))
    loadCategoryPercent(pieUK, @getAttribute('date'))
    loadCategoryPercent(pieDE, @getAttribute('date'))



