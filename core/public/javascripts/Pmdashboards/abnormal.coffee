$ ->

  # table 数据列表
  $("#below_tabContent").on("ajaxRefresh", "#abnormalInfo", () ->
    $div = $(@)
    LoadMask.mask()
    $div.load("/Pmdashboards/abnormal", (r) ->
      LoadMask.unmask()
    )
  )

  #当前选中的tab，调用相对应数据
  ajaxFreshAcitveTableTab = ->
    type = $("#below_tab li.active a").attr("href")
    $("#{type}").trigger("ajaxRefresh")

  #  Tab 切换添加事件 bootstrap  shown 事件：点击后触发
  $('a[data-toggle=tab]').on('shown', (e) ->
    ajaxFreshAcitveTableTab()
  )


