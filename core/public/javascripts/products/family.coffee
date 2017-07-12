$ ->
  ALL_BRAND_LINKS = $('#slider a[level=brand]')
  FAMILYS = $("#fam_div")
  PRODUCTS = $("#pro_div")

  ALL_BRAND_LINKS.click ->
    ALL_BRAND_LINKS.parent().removeClass('active')
    $(@).parent().addClass('active')
    loadFamilys()

  # 加载 Family
  loadFamilys = ->
    FAMILYS.mask('加载中...')
    brand = $('li.active a[level=brand]')
    FAMILYS.load('/familys/famDiv', {'c.categoryId': $(brand).attr('cid'), 'b.name': $(brand).attr('bid')}, (r) ->
      FAMILYS.unmask()
    )
    loadProducts($(brand).attr('fid'))

  # 加载 Family 下的 Product 列表
  loadProducts = (family_id) ->
    PRODUCTS.mask('加载中...')
    PRODUCTS.load("/familys/proDiv", {'f.family': family_id}, (r) ->
      PRODUCTS.unmask()
    )

  FAMILYS.on('click', 'a[action=remove]',() ->
    return unless confirm('确认删除?')
    FAMILYS.mask()
    $btn = $(@)
    $.ajax($btn.data('url'))
    .done((r) ->
        type = if r.flag
          # 只删除最近的一个 tr 父元素
          $btn.parents('tr')[0].remove()
          'success'
        else
          'error'
        noty({text: r.message, type: type, timeout: 3000})
        FAMILYS.unmask()
      )
  ).on('click', 'td[action=products]',() ->
    loadProducts($(@).attr('fid'))
  ).on('click', '#add_family', () ->
    FAMILYS.mask()
    $.post('/familys/create', $(@).parents('table').find(":input").fieldSerialize())
    .done((r) ->
        if(r.flag is true)
          noty({text: r.message, type: 'success', timeout: 3000})
          loadFamilys()
        else
          noty({text: r.message, type: 'error', timeout: 3000})
        FAMILYS.unmask()
      )
  )


