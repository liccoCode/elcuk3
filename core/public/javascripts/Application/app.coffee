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

  class PieChart
    constructor: (@container, @market) ->

    # units/sales
    percent: (type = 'units', date = $.DateUtil.fmt2(new Date()), mask_selector = '#orders') =>
      self = @
      LoadMask.mask(mask_selector)
      $.get('/application/percent', {type: type, date: date, m: @market}, (r) ->
        title = r['series'][0]['name']
        console.log(r['series'][0]['name'])
        $("##{self.container}").highcharts({
          title: {
            text: title
          },
          tooltip:
            formatter: ->
              "<b>#{@point.name}</b>: #{@percentage.toFixed(2)}%<br/>OrderItems: #{@y} / #{@total}"
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

  # 重新绘制所有的 Pie 图
  drawPies = (date) ->
    new PieChart("cat_percent", 'amazon_de').percent('all', date)
    new PieChart("cat_percent_de", 'amazon_de').percent('units', date)
    new PieChart("cat_percent_us", 'amazon_us').percent('units', date)
    new PieChart("cat_percent_uk", 'amazon_uk').percent('units', date)
    new PieChart("cat_percent_es", 'amazon_es').percent('units', date)
    new PieChart("cat_percent_it", 'amazon_it').percent('units', date)
    new PieChart("cat_percent_fr", 'amazon_fr').percent('units', date)

  drawPies($("#orders tr:last td:eq(0)").attr('date'))

  #为table 中的日期添加查看指定天的 类别百分比数据
  $('#orders td[date]').css('cursor', 'pointer').click ->
    drawPies(@getAttribute('date'))
