$ ->

# 切换供应商, 自行寻找价格
  $("select[name='unit.cooperator.id']").change (e) ->
    id = $(@).val()
    if id
      LoadMask.mask()
      $.get('/Cooperators/price', {id: id, sku: $('#unit_sku').val()}, 'json')
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          $("#unit_currency option:contains('#{r.currency}')").prop('selected', true)
          $("#unit_price").val(r.price)
          $("#unit_period").show()
          $("#unit_period").text('（生产周期：' + r.period + ' 天）')
        LoadMask.unmask()
      )
# 恢复默认
    else
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").text('')
      $("#unit_period").hide()

  $('#box_num').change (e) ->
    e.preventDefault()
    coperId = $("select[name='unit.cooperator.id']").val()
    if coperId
      $.post('/cooperators/boxSize', {size: $('#box_num').val(), coperId: coperId, sku: $('#unit_sku').val()})
      .done((r) ->
        if r.flag is false
          alert(r.message)
        else
          $("input[name='unit.attrs.planQty']").val(r['message'])
      )
    else
      alert('请先选择 供应商')

  # 计算时间到库日期与运输日期的差据
  $("[name='unit.attrs.planArrivDate']").change () ->
    planShipDate = $("[name='unit.attrs.planShipDate']")
    planArrivDate = $(@)
    if planArrivDate.val() and planShipDate.val()
      planArrivDate.next().text("#{(new Date(planArrivDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000)} 天")

  $("[name='unit.attrs.planShipDate']").change () ->
    shipType = $("[name='unit.shipType']:checked").val()
    if shipType != 'EXPRESS'
      return
    planShipDate = $("[name='unit.attrs.planShipDate']").val()
    warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {planShipDate: planShipDate, shipType: shipType, warehouseid})
    .done((r) ->
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate'])
    )

  $(document).ready ->
    $shipType = $("[name='unit.shipType']")
    $shipType.trigger('change') if $shipType.val() != undefined && $shipType.val() != 'EXPRESS'

  # Ajax 加载 Shipment
  $('#new_procureunit,#unitEditForm,#update_form').on('change', "[name='unit.shipType'],[name='unit.whouse.id']", ->
    whouseId = $("[name='unit.whouse.id']").val()
    shipType = $("[name='unit.shipType']:checked").val()
    shipment = $('#shipments')
    return unless (whouseId && shipType && shipment.size() > 0)

    if shipType == 'EXPRESS'
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.')
    else
      LoadMask.mask(shipment)
      $.get('/shipments/unitShipments', {whouseId: whouseId, shipType: shipType})
      .done((html) ->
        shipment.html(html)
        LoadMask.unmask()
      )

    if shipType != 'EXPRESS'
      return
    planShipDate = $("[name='unit.attrs.planShipDate']").val()
    if planShipDate == ''
      return
    warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {planShipDate: planShipDate, shipType: shipType, warehouseid})
    .done((r) ->
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate'])
    )
  )

  $('#shipments').on('change', '[name=shipmentId]', (e) ->
    LoadMask.mask()
    $.get("/shipment/#{@getAttribute('value')}/dates")
    .done((r) ->
      $("input[name='unit.attrs.planShipDate']").data('dateinput').setValue(r['begin'])
      $("input[name='unit.attrs.planArrivDate']").data('dateinput').setValue(r['end'])
      LoadMask.unmask()
    )
  )

  $('#new_procureunit').on('change', "[name='unit.product.sku']", ->
    $cooperators = $("select[name='unit.cooperator.id']")
    # 当输入的 sku 长度大于5才发起 Ajax 请求获取供应商列表
    if this.value.length > 5
      LoadMask.mask()
      # Ajax 加载供应商列表
      $.get('/products/cooperators', {sku: this.value})
      .done((r) ->
        $cooperators.empty()
        $cooperators.append("<option value=''>请选择</option>")
        r.forEach (value) ->
          $cooperators.append("<option value='#{value.id}'>#{value.name}</option>")
        LoadMask.unmask()
      )
  ).on('click', "#create_unit", (e) ->
    e.preventDefault()
    if !$("#planQty").val()
      noty({text: "请先填写采购数量！", type: 'error'})
      return false

    $.get('/procureunits/hasProcureUnitBySellings', {sellingId: $("#sellingId").val()})
    .done((r)->
      if r.flag
        totalFive = $("#totalFive").val()
        day = $("#day").val()
        planQty = $("#planQty").val()
        if day == null || day == '0'
          $("#new_procureunit").submit()
        else
          $.get('/procureunits/isNeedApprove', {total: totalFive + planQty, day: day})
          .done((e)->
            if e.flag
              if confirm(e.message)
                if $("#memo").val()
                  $("#isNeedApply").val("need")
                  $("#new_procureunit").submit()
                else
                  noty({text: "请先填写备注！", type: 'error'})
                  return false
            else
              $("#new_procureunit").submit()
          )
      else
        if confirm('该selling第一次创建采购计划需走采购计划审批流程，确定吗?')
          $("#isNeedApply").val("need")
          $("#new_procureunit").submit()
    )
  )

