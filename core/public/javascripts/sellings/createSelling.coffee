$ ->

  $("#submitSaleBtn").click((e) ->
    e.preventDefault();
    if $("[name='createtype']:checked").length == 0
      noty({text: '请选择处理类型', type: 'error'})
      return false
    if previewBtn()
      return false
    if $("#inputsku").val()
      if $("[name='createtype']:checked").val() == 'amz'
        $form = $('#saleAmazonForm')
        LoadMask.mask()
        $.ajax($form.attr('action'), {data: $form.serialize(), type: 'POST'})
        .done((r) ->
          if r.flag
            noty({
              text: "成功创建 Selling #{r.message}, Amazon 与系统正在处理中, 请等待 5~10 分钟后再查看",
              layout: 'top',
              type: 'success',
              timeout: false
            })
            window.location.href = "/sellings/selling/" + r.message
          else
            noty({text: r.message, type: 'error'})
          LoadMask.unmask()
        )
        .fail((r) ->
          noty({text: r.responseText, type: 'error'})
          LoadMask.unmask()
        )
      else
        $from = $('#new_selling')
        $from.mask('创建 Selling')
        $.ajax($from.attr('action'), {type: 'POST', data: $from.serialize()})
        .done((r) ->
          if r.flag
            noty({text: "添加成功，SellingId：#{r.message} ", type: 'success', timeout: 5000})
            $("#amzradio").hide()
            $("#submitSaleBtn").hide()
          else
            noty({text: "添加失败： #{r.message}", type: 'error', timeout: 5000})
          $from.unmask()
        )
    else
      noty({text: '请选择SKU', type: 'error'})
      return false;
  )

  previewBtn = (e) ->
    invalidTag = false
    for tag in $('#previewDesc').html($('#productDesc').val()).find('*')
      switch tag.nodeName.toString().toLowerCase()
        when 'br','p','b','#text'
          break
        else
          invalidTag = true
          $(tag).css('background', 'yellow')
    noty({text: '使用了 Amazon 不允许使用的 Tag, 请查看预览中黄色高亮部分!', type: 'error', timeout: 3000}) if invalidTag is true
    invalidTag

  $('#market_add').change(->
    market = $(@).val()
    if market is 'AMAZON_JP'
      $("#upc").val($("#upc_jp").val())
    else
      $("#upc").val($("#upc_init").val())
  )