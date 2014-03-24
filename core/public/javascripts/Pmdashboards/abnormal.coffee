$ ->

  $("#below_tabContent").on("ajaxRefresh", "#review,#day1SalesAmount,#beforeSalesAmount,#beforeProfit", () ->
    $div = $(@)
    LoadMask.mask()
    $div.load("/Pmdashboards/#{$div.attr("id")}", (r) ->
      LoadMask.unmask()
    )
  )

  #  Tab 切换添加事件 bootstrap  shown 事件：点击后触发
  $('a[data-toggle=tab]').on('shown', (e) ->
    type = $("#below_tab li.active a").attr("href")
    if type == "abnormalInfo"
      $("#review").trigger("ajaxRefresh")
      $("#day1").trigger("ajaxRefresh")
      $("#before").trigger("ajaxRefresh")
  )


