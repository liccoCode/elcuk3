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


  pieOp =
    chart:
      renderTo: 'cat_percent'
      type: 'pie'
    title: {text: '类别销量百分比'}
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
    clearLines: () ->
      @series = []

  # category 百分比
  loadCategoryPercent = (date = $.DateUtil.fmt2(new Date())) ->
    mask = $('#orders')
    mask.mask('加载中...')
    $.post('/application/categoryPercent', date: date,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          line = name: '类别销量百分比', data: []
          line.data.push([o['_1'], o['_2']]) for o in r
          pieOp.clearLines()
          pieOp.series.push(line)
          new Highcharts.Chart(pieOp)
        mask.unmask()
    )
  loadCategoryPercent()

  #为table 中的日期添加查看指定天的 类别百分比数据
  $('#orders td[date]').css('cursor', 'pointer').click ->
    loadCategoryPercent(@getAttribute('date'))



