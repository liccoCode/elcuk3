$ ->
  UNIT_DETAIL = $('#unit_details')
  # ------------ Plan Tab 功能 -----------------------
  #procureUnit details 的事件绑定方法
  bindNewDeliveryBtn = () ->
    $('#delivery_btn').click ->
      $.post('/procures/createDeliveryMent', {},
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            window.location.reload()
      )

  # 将某一个 ProcureUnit 纳入到指定采购单
  bindToDeliveryBtn = () ->
    $('#todelivery_btn').click ->
      $.varClosure.params = {}
      $('#todelivery_info :input').map($.varClosure)
      if !$.varClosure.params['p.delivery.planDeliveryDate']
        alert("预计交货日期不能为空!")
        return false
      if !$.varClosure.params['dlmt.id']
        alert('没有指定采购单!')
        return false
      if !$.varClosure.params['p.id']
        alert('未知 ProcureUnit!')
        return false
      $.post('/procures/procureUnitToDeliveryMent', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert('绑定失败: ' + r.message)
          else
            window.location.reload()
      )

  # 加载 Plan Tab 以后的页面初始化
  afterLoadPlanTab = ->
    window.htmlIni()
    #绑定创建新采购单按钮
    bindNewDeliveryBtn()
    #绑定纳入采购单按钮
    bindToDeliveryBtn()
    # 绑定跳转 ProcureUnit 更新按钮
    bindProcureUnitEditBtn()
  # 重新初始化所有 popover


  # 更新 ProcureUnit 按钮事件
  bindProcureUnitEditBtn = ->
    $('#procureUnit_update_btn').attr('href', '/procures/edit?id=' + $('#procureUnit_id').val())


  # -------------- Delivery Tab 功能 --------------------------
  # planQty -> deliveryQty
  bindPlanQtyBtn = ->
    $('#planQty_btn').click ->
      $('[name=p\\.delivery\\.deliveryQty]').val($(@).html())

  # 更新采购信息
  bindUpdateDeliveryBtn = ->
    $('#update_delivery_info').click ->
      $.varClosure.params = {}
      $('#deliveryInfo :input').map($.varClosure)
      info = $('#deliveryInfo')
      info.mask("更新中...")
      $.post('/procures/procureUnitDeliveryInfoUpdate', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            alert('更新成功')
          info.unmask()
          activeDelivery = $('#procure tr[row][class=active]')
          activeDelivery.find('td:eq(3)').html(r['delivery']['deliveryQty'])
          activeDelivery.click()
      )

  # 指派 Shipment
  bindAssignShipment = ->
    $("#assainToShipment").click ->
      $.varClosure.params = {}
      $("#toShipment :input").map($.varClosure)
      toShipment = $("#toShipment")
      toShipment.mask("指派中...")
      $.post('/procures/assignToShipment', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            $(@).addClass('invisible').unbind('click')
            alert('3 s 后刷新')
            setTimeout(
              () ->
                window.location.reload()
              , 3000
            )
          toShipment.unmask()
      )

  # 加载 Delivery Tab 以后的初始化
  afterLoadDeliveryTab = ->
  # 实际交货数量全部转移
    bindPlanQtyBtn()
    bindUpdateDeliveryBtn()
    bindAssignShipment()
    window.htmlIni()


  # -------------- 通用功能 ------------------
  #三个 Tab 中的点击选中事件
  threeTabClickActive = (tab, o) ->
    $.tableRowClickActive('#' + tab + ' tr[row]', o)

  #初始化 Plan 点击事件
  planRowClick = ->
    $('#plan tr[row]').click ->
      o = $(@)
      threeTabClickActive('plan', o)
      UNIT_DETAIL.mask("加载中...")
      UNIT_DETAIL.load('/procures/planDetail', id: o.find('td:eq(0)').html(),
        ->
          UNIT_DETAIL.unmask()
          afterLoadPlanTab()
      )
      false

  #初始化 Delivery 点击事件
  deliveryRowClick = ->
    $('#delivery tr[row]').click ->
      o = $(@)
      threeTabClickActive('delivery', o)
      UNIT_DETAIL.mask('加载中...')
      UNIT_DETAIL.load('/procures/deliveryDetail', id: o.find('td:eq(0)').html(),
        (r) ->
          UNIT_DETAIL.unmask()
          afterLoadDeliveryTab()
      )
      false

  planRowClick()
  deliveryRowClick()

