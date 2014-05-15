$ ->
  $.extend $.fn.dataTableExt.oStdClasses,
    sWrapper: "dataTables_wrapper form-inline"

  $(document).ready ->
    oTable = $("#check_table").dataTable(
      sDom: "<'row-fluid'<'span10'><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )

  $(document).ready ->
    oTable = $("#checked_table").dataTable(
      sDom: "<'row-fluid'<'span10'><'span2'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      sPaginationType: "full_numbers"
      iDisplayLength: 50
      aaSorting: [[0, "desc"]]
    )

  # 待检任务
  $("#below_tabContent").on("ajaxCheckList", "#checkList", () ->
    $btn = $(@)

  )

  # 已检任务
  $("#below_tabContent").on("ajaxCheckedList", "#checkedList", () ->
    $btn = $(@)

  )

  # 触发当前选中的 tab 的事件
  triggerTabMethod = ->
    type = $("#below_tab li.active a").attr("href")
    $tab = $("#{type}")
    $tab.trigger($tab.data("method"))

  #  Tab 切换事件
  $('a[data-toggle=tab]').on('shown', (e) ->
    triggerTabMethod()
  )