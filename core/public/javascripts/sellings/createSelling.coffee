$ ->
  $sku = $("#inputsku")
  $sku.typeahead({
    source: (query, process) ->
      sku = $sku.val()
      $.get('/products/sameSku', {sku: sku})
      .done((c) ->
        process(c)
      )
  })
  $("input[name='createtype']").click((e) ->
    if $("#inputsku").val()
      if $(@).val() == 'add'
        $("#addDiv").fadeIn()
        $("#amzDiv").fadeOut()
        $("#submitSaleBtn").text("添加Selling")
        $("#addSellingSku").val($("#inputsku").val())
      else
        $("#amzDiv").load('/Sellings/saleAmazon', id: $("#inputsku").val(), (r) ->
          LoadMask.unmask()
          $("#amzDiv").fadeIn()
          $.getScript('../public/javascripts/editor/kindeditor-min.js', ->
            KindEditor.create('#productDesc',{
              resizeType: 1
              allowPreviewEmoticons: false
              allowImageUpload: false
              afterChange: -> this.sync(); this.count();
              items: [
                'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold', 'italic', 'underline',
                'removeformat']
            })
          )
          $.getScript('../public/javascripts/component/amazon.coffee/')
          $("#saleAmazonForm").on('change', ".ke-content", (e) ->
            alert 1
          )

        )
        $("#addDiv").fadeOut()
        $("#submitSaleBtn").text("AMZ上架")
    else
      noty({text: '请选择SKU', type: 'error'})
      $(@).prop("checked", false)
  )

  $("#submitSaleBtn").click((e) ->
    e.preventDefault();
    if $("[name='createtype']:checked").length == 0
      noty({text: '请选择处理类型', type: 'error'})
      return false;
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

