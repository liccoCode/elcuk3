$ ->
  SHOW_COOPERATOR = $('#show_cooperator')
  NAV_BAR = $("#nav_bar")


  # 为导航栏添加点击加载事件
  $('#nav_bar li a').click ->
    NAV_BAR.mask('加载中...')
    SHOW_COOPERATOR.load('show', {id: @getAttribute('id'), full: false},
      (r) ->
        NAV_BAR.unmask()
    )

  # 访问首页以后, 首先利用 Ajax 加载第一个合作伙伴信息(在导航栏拥有 Click 事件的基础上)
  $('#nav_bar li.active a').click()
