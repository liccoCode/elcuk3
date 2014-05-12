$ ->
  class LineChart
    constructor: (@container) ->

      # units/sales
    line: (type = 'units', @sku, mask_selector = '#saleinfo') =>
      self = @
      LoadMask.mask(mask_selector)
      $.get('/products/linechart', {type: type, sku: @sku}, (r) ->
        title = r['title']
        console.log(r['series'][0]['name'])
        $("##{self.container}").highcharts({
        title:
          {
          text: title
          }
        legend:
          enabled: true
        xAxis:
          type: 'datetime'
        yAxis:
          { min: 0 }
        tooltip:
          shared: true
          crosshairs: true
          xDateFormat: '%Y-%m-%d'
        series: r['series']
        })
        LoadMask.unmask(mask_selector)
      )

  # 重新绘制所有的 Line 图
  linePies = (sku) ->
    new LineChart("skusalefee").line('skusalefee', sku,'#saleinfo')
    new LineChart("skusaleqty").line('skusaleqty', sku,'#saleinfo')
    new LineChart("skuprofit").line('skuprofit', sku,'#saleinfo')

  $('#skuSaleInfo').click ->
    if $('input[name="skuvalue"]').val() isnt "1"
      linePies($('input[name="pro.sku"]').val())
      $('input[name="skuvalue"]').val("1")

  # 重新绘制所有的 Line 图
  PricePies = (sku) ->
    new LineChart("skuprocureprice").line('skuprocureprice', sku,'#procureinfo')
    new LineChart("skuprocureqty").line('skuprocureqty', sku,'#procureinfo')

  $('#skuProcureInfo').click ->
    if $('input[name="skuprocurevalue"]').val() isnt "1"
      PricePies($('input[name="pro.sku"]').val())
      $('input[name="skuprocurevalue"]').val("1")