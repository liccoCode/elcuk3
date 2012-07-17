$ ->
  SHOW_COOPERATOR = $('#show_cooperator')
  NAV_BAR = $("#nav_bar")


  # 为导航栏添加点击加载事件
  $('#nav_bar li a').click ->
    NAV_BAR.mask('加载中...')
    $('#nav_bar li').removeClass('active')
    li = @
    SHOW_COOPERATOR.load('show', {id: @getAttribute('id'), full: false},
      (r) ->
        li.parentNode.className += 'active'
        NAV_BAR.unmask()
    )

  # 访问首页以后, 首先利用 Ajax 加载一个合作伙伴信息(在导航栏拥有 Click 事件的基础上)
  # 从 url 的 #hash 中提取数据, 如果没有找到则直接访问第一个并设置 active
  activeCooperator = ->
    args = location.hash.substr(1).split('/')
    toBeClick = $("##{args[0]}")
    if toBeClick.size() is 0
      $('#nav_bar li a:eq(0)').click()
    else
      toBeClick.click()

  activeCooperator()
