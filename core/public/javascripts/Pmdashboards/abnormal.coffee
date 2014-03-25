$ ->
  $("#below_tabContent").on("ajaxRefresh", "#review,#day1SalesAmount,#beforeSalesAmount,#beforeSalesProfit", () ->
    $div = $(@)
    $div.load("/Pmdashboards/#{$div.attr("id")}", (r) ->
    )
  )

  #  Tab 切换添加事件 bootstrap  shown 事件：点击后触发
  $('a[data-toggle=tab]').on('shown', (e) ->
    $abnormalInfo = $("#abnormalInfo")
    type = $("#below_tab li.active a").attr("href")
    if type == "#abnormalInfo"
      LoadMask.mask($abnormalInfo)
      $("#review").trigger("ajaxRefresh")
      $("#day1SalesAmount").trigger("ajaxRefresh")
      $("#beforeSalesAmount").trigger("ajaxRefresh")
      $("#beforeSalesProfit").trigger("ajaxRefresh")
      LoadMask.unmask($abnormalInfo)
  )


