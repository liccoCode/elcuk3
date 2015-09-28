$ ->
  $sku = $("#inputsku")
  $sku.typeahead({
    source: (query, process) ->
      sku = $sku.val()
      $.get('/products/sameSku', {sku: sku})
      .done((c) ->
        process(c)
      )
    updater: (item) ->
      $("input[name='createtype']:checked").attr("checked", false)
      $("#amzDiv").fadeOut()
      $("#addDiv").fadeOut()
      $("#submitSaleBtn").hide()
      item
  })
  $("input[name='createtype']").click((e) ->
    if $("#inputsku").val()
      if $(@).val() == 'add'
        $.post('/products/findUPC', sku: $("#inputsku").val(),
          (r) ->
            $("#addDiv").fadeIn()
            $("#amzDiv").fadeOut()
            $("#submitSaleBtn").show()
            $("#submitSaleBtn").text("添加Selling")
            $("#addSellingSku").val($("#inputsku").val())
            $("#upc").val(r.upc)
        )
      else
        $("#amzDiv").load('/Sellings/saleAmazon', id: $("#inputsku").val(), (r) ->
          LoadMask.unmask()
          $("#amzDiv").fadeIn()
          $.getScript('../public/javascripts/editor/kindeditor-min.js', ->
            KindEditor.create('#productDesc', {
              resizeType: 1
              allowPreviewEmoticons: false
              allowImageUpload: false
              items: [
                'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold', 'italic', 'underline',
                'removeformat']
              afterChange: -> this.sync(); $("#productDesc").find('~ .help-inline').html((2000 - this.count()) + " bytes left")
            })
          )
          $.getScript('../public/javascripts/component/amazon.coffee')
        )
        $("#addDiv").fadeOut()
        $("#submitSaleBtn").text("AMZ上架")
        $("#submitSaleBtn").show()
    else
      noty({text: '请选择SKU', type: 'error'})
      $(@).prop("checked", false)
  )

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

