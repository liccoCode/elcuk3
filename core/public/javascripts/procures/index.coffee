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
      return false if !confirm("确认绑定到: " + $('[name=dlmt\\.id] option:checked').html())
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

  procureUnitId = ->
    id: $('#procureUnit_id').val()

  # 更新 ProcureUnit 按钮事件
  bindProcureUnitEditBtn = ->
    $('#procureUnit_update_btn').attr('href', '/procures/edit?id=' + procureUnitId()['id'])

  # 关闭 ProcureUnit 按钮事件
  bindProcureCloseBtn = ->
    $('#procureUnit_close_btn').click ->
      msg = prompt('却输入关闭的原因:')
      return false if msg is undefined or msg.trim() == ''
      UNIT_DETAIL.mask('关闭中...')
      $.post('/procures/close', {id: procureUnitId()['id'], msg: msg},
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            $('#plan tr[row][class=active]').remove()
            UNIT_DETAIL.html(r.message)
          UNIT_DETAIL.unmask()
      )

  # -------------- Delivery Tab 功能 --------------------------
  # planQty -> deliveryQty
  bindPlanQtyBtn = ->
    $('#planQty_btn').click ->
      $('[name=p\\.delivery\\.deliveryQty]').val($(@).html())

  # 更新采购信息
  bindUpdateDeliveryBtn = ->
    $('#update_delivery_info').click ->
      if $('#deliveryInfo form').valid() is false
        return false
      $.varClosure.params = {}
      $('#deliveryInfo :input').map($.varClosure)
      info = $('#deliveryInfo')
      info.mask("更新中...")
      $.post('/procures/procureUnitDone', $.varClosure.params,
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            alert('更新成功')
          info.unmask()
          $('#delivery tr[row][class=active]').remove()
      )
      false

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


  # 加载 Plan Tab 以后的页面初始化
  afterLoadPlanTab = ->
  # 重新初始化所有 popover
    window.$ui.htmlIni()
    #绑定创建新采购单按钮
    bindNewDeliveryBtn()
    #绑定纳入采购单按钮
    bindToDeliveryBtn()
    # 绑定跳转 ProcureUnit 更新按钮
    bindProcureUnitEditBtn()
    bindProcureCloseBtn()


  # 加载 Delivery Tab 以后的初始化
  afterLoadDeliveryTab = ->
  # 实际交货数量全部转移
    bindPlanQtyBtn()
    bindUpdateDeliveryBtn()
    bindAssignShipment()
    window.$ui.htmlIni()


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

