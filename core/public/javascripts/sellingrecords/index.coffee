$ ->
  Highcharts.setOptions(global: {useUTC: false})

  # tables
  $('#sellingSkuCategoryDivs').on('ajaxFresh', '#selling, #sku, #category',() ->
    # 1. 收集 Market 的参数
    # 2. 加载数据
    $div = $(@)
    $('#post_type').val($div.attr('id'))
    LoadMask.mask()
    $div.load('/sellingrecords/table', $('.search_form').serialize(), (r) ->
      LoadMask.unmask()
    )
  ).on('click', '.pagination a[page]', (e) ->
    $a = $(@)
    $('#post_page').val($a.attr('page'))
    $a.parents('table').parent().trigger('ajaxFresh')
    false
  ).on('click', '.point', (e) ->
    $td = $(@)
    $('#post_val').val($td.text().trim())
    $('#lines').trigger('ajaxFresh')
  )

  # lines
  $('#lines').on('ajaxFresh', () ->
    $div = $(@)
    LoadMask.mask()
    $.ajax('/sellingrecords/lines', {type: 'GET', data: $('.search_form').serialize(), dataType: 'json'})
      .done((r) ->
        $div.highcharts('StockChart', {
          title:
            text: "#{$('#post_val').val()} 曲线图"
          legend:
            enabled: true
          navigator:
            enabled: false
          scrollbar:
            enabled: false
          rangeSelector:
            enabled: false
          yAxis: [{},{labels: {format: '{value}'}, opposite: true}]
          series: r['series']
        })
        LoadMask.unmask()
      )
  )

  $('a[data-toggle=tab]').on('shown', (e) ->
    $('#post_page').val(1)
    $("#{$(e.target).attr('href')}").trigger('ajaxFresh')
  )

  $('.search_form [name=p\\.market]').change((e) ->
    # 1. trigger #sid and set page to 1
    $('#post_page').val(1)
    type = $('#divTabs li.active a').attr('href')[1..-1]
    $("##{type}").trigger('ajaxFresh')
    $('#lines').trigger('ajaxFresh')
    false
  )

  $('a[data-toggle=tab]:contains(Selling)').tab('show')
  $('#lines').trigger('ajaxFresh')
