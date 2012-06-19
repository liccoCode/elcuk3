$ ->
#df
  abc = ""

  #procureUnit details 的事件绑定方法
  bindDeliveryBtn = () ->
    $('#delivery_btn').click ->
      $.post('/procures/createDeliveryMent', {},
        (r) ->
          if r.flag is false
            alert(r.message)
          else
            $('#plan tr[row][class=active]').click()

      )

  # 将某一个 ProcureUnit 纳入到指定采购单
  bindToDeliveryBtn = () ->
    $('#todelivery_btn').click ->
      $.varClosure.params = {}
      $('#todelivery_info :input').map($.varClosure)
      if !$.varClosure.params['p.delivery.planDeliveryDate']
        alert("预计交货日期不能为空!")
        return false
      if !$.varClosure.params['p.deliveryment.id']
        alert('没有指定采购单!')
        return false
      if !$.varClosure.params['p.id']
        alert('未知 ProcureUnit!')
        return false
      $.post('/procures/procureUnitToDeliveryMent', $.varClosure.params, -> window.location.reload())

  #初始化 Plan 点击事件
  planRowClick = ->
    $('#plan tr[row]').css('cursor', 'pointer').click ->
      o = $(@)
      $('#plan tr[row]').removeClass('active')
      o.addClass('active')
      details = $('#unit_details')
      details.mask("加载中...")
      details.load('/procures/planDetail', id: o.find('td:eq(0)').html(),
        ->
          details.unmask()
          $('input[type=date]').dateinput(format: 'yyyy-mm-dd')
          bindDeliveryBtn()
          bindToDeliveryBtn()
      )

  planRowClick()
