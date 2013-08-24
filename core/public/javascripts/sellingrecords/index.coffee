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
  ).on('click', '.pagination a[page]',(e) ->
    $a = $(@)
    $('#post_page').val($a.attr('page'))
    ajaxFreshAcitveTableTab()
    false
  ).on('click', '.point', (e) ->
    $td = $(@)
    $('#post_val').val($td.text().trim())
    ajaxFreshLines()
    ajaxFreshColumns()
  ).on('click', 'td[orderby]', (e) ->
    $td = $(@)
    $('#post_orderby').val($td.attr('orderby'))
    ajaxFreshAcitveTableTab()
  )

  # lines # columns
  $("#chartsDiv").on('ajaxFresh', '#lines, #columns', () ->
    $div = $(@)
    LoadMask.mask()
    $.ajax("/sellingrecords/#{$div.attr('id')}", {type: 'GET', data: $('.search_form').serialize(), dataType: 'json'})
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
    ajaxFreshAcitveTableTab()
  )

  $('.search_form').on('change', '[name=p\\.market]', (e) ->
    # 1. trigger #sid and set page to 1
    $('#post_page').val(1)
    ajaxFreshAcitveTableTab()
    ajaxFreshLines()
    ajaxFreshColumns()
    false
  ).on('click', '.btn:contains(搜索)', (e) ->
    ajaxFreshAcitveTableTab()
    false
  ).on('click', '.reload', (e) ->
    ajaxFreshLines()
  )

  ajaxFreshAcitveTableTab = ->
    type = $('#divTabs li.active a').attr('href')
    $("#{type}").trigger('ajaxFresh')

  ajaxFreshLines = ->
    $('#lines').trigger('ajaxFresh')

  ajaxFreshColumns = ->
    $('#columns').trigger('ajaxFresh')


  $('a[data-toggle=tab]:contains(Selling)').tab('show')
  ajaxFreshLines()
  ajaxFreshColumns()
