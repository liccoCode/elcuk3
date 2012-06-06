$ ->
  addCatForm = $('#add_cat_form')

  # 创建 Category 方法
  catCreate = (params) ->
    $.post('/categorys/cc', params,
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          if r['categoryId'] is params['c.categoryId']
            alert('Category [' + r['categoryId'] + '] 添加成功')
          else
            alert('添加失败\r\n' + JSON.stringify(r))
    )

  # 添加 Category BTN
  $('#addCategoryBtn').click ->
    $.varClosure.params = {}
    addCatForm.find(':input').map($.varClosure)
    catCreate($.varClosure.params)

  # 绑定详细页面中的 更新 按钮
  bindBasicInfoUpdate = (cid) ->
    $('#btn_' + cid).click ->
      $.varClosure.params = {}
      $('#detail_' + cid + ' :input').map($.varClosure)
      basic = $('#detail_' + cid)
      basic.mask('更新中...')
      $.post('/categorys/cu', $.varClosure.params,
        (r) ->
          alert((if r.flag is true then '更新成功' else '更新失败') + '[' + r.message + ']')
          basic.unmask()
      )

  # 绑定详细页面中的 Bind 按钮
  bindBrandBindBtn = ->
    $('button[bind]').click ->
      o = $(@)
      $.varClosure.params = {}
      o.parent().parent().find(':input').map($.varClosure)
      if not('b.name' of $.varClosure.params)
        alert('请选择正确的 Brand.')
        return false
      bindMask = $('#brands_' + o.attr('cid'))
      bindMask.mask('绑定中...')
      $.post('/categorys/bBrand', $.varClosure.params,
        (r) ->
          if r.flag is true
            alert('绑定成功!')
            $('tr[cid=' + o.attr('cid') + ']').click()
          else
            alert(r.message)
          bindMask.unmask()
      )

  # 绑定详细页面中的 Unbind 按钮
  bindBrandUnBindBtn = ->
    $('button[unbind]').click ->
      o = $(@)
      unbindMask = $('#brands_' + o.attr('cid'))
      unbindMask.mask('解除绑定中...')
      $.post('/categorys/uBrand', 'c.categoryId': o.attr('cid'), 'b.name': o.attr('bid'),
        (r) ->
          if r.flag is true
            alert('解除绑定成功.')
            $('tr[cid=' + o.attr('cid') + ']').click()
          else
            alert(r.message)
          unbindMask.unmask()
      )

  # 绑定 Attr 的保存按钮
  bindAttrSaveBtn = ->
    $('#cat_atts_btn').click ->
      cat_atts = $('#cat_atts')
      checks = cat_atts.find(':input')
      checked = ($(aCheck).val() for aCheck in checks when $(aCheck).is(':checked'))
      params = 'c.categoryId': $(@).attr('cid')
      for aCheck, i in checked
        params['ats[' + i + '].name'] = aCheck
      cat_atts.mask('绑定中...')
      $.post('/categorys/bindAttrs', params,
        (r) ->
          if r.flag is true
            alert('绑定成功!')
          else
            alert(r.message)
          cat_atts.unmask()
      )
      false

  # 点击加载详细信息
  $('#cat_slider tr[cid]').click ->
    slider = $('#cat_slider')
    slider.mask('加载中...')
    $('#cat_detail').load('/categorys/detail', cid: $(@).attr('cid'), =>
        bindBasicInfoUpdate($(@).attr('cid'))
        bindBrandBindBtn()
        bindBrandUnBindBtn()
        bindAttrSaveBtn()
        slider.unmask()
    )
