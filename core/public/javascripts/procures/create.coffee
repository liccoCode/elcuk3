$ ->
  SUCCESS_TEMPLATE = '''<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">×</button><span></span></div>'''
  SID_INPUT = $('[name=p\\.sid]')

  # 创建 ProcureUnit
  $('#create_procure').click (e) ->
    $.post('/procures/save', $('#post_form').formSerialize(),
      (data) ->
        if data['flag'] is false
          alert(data['message'])
        else
          SID_INPUT.val('')
          alterEl = $(SUCCESS_TEMPLATE)
          alterEl.find('span').html("创建成功." + JSON.stringify(data))
          alterEl.appendTo('#alert')
    )
    e.preventDefault()

  # 清理缓存
  $('#selling_allsid').click (e) ->
    mask = $('#container')
    mask.mask('清理缓存中...')
    $.post('/c/selling.allsid',
      (r) ->
        if r.flag is false
          alert('清理缓存失败')
        else
          alert('清理缓存成功')
        mask.unmask()
    )

  # Selling ID 修改, 同时初始化 Product, 自动调整 Whouse
  SID_INPUT.change ->
    o = $(@)
    o.parents('td').mask('检查中...')
    $.getJSON('/procures/sidSetUp', sid: o.val(),
      (data) ->
        $('#asin a').attr('href', '/listings/listing?lid=' + data['_1'])
        $('#sid a').attr('href', '/sellings/selling?sid=' + data['_2'])
        $('[name=p\\.sku]').val(data['_3'])
        for opt in $('[name=p\\.whouse\\.id] option')
          _opt = $(opt).removeAttr('selected')
          _opt.prop('selected', true) if _opt.html().split('_')[1] is data['_4'].split('_')[1]
        o.parents('td').unmask()
    )

  $('[name=p\\.attrs\\.planQty] ~ .add-on').click ->
    o = $(@)
    planQty = $('[name=p\\.attrs\\.planQty]')
    if !$.isNumeric(planQty.val())
      alert("采购数量只允许为数字!")
      return false
    planQty.val(Number(planQty.val()) + Number(o.attr('val')))
