$(() => {

  let $sku = $("#inputsku");
  $sku.typeahead({
    source: (query, process) => {
      let sku = $sku.val();
      $.get('/products/sameSku', {
        sku: sku
      }, function (c) {
        process(c);
      });
    },
    updater: (item) => {
      $("input[name='createtype']:checked").attr("checked", false);
      $("#amzDiv").fadeOut();
      $("#addDiv").fadeOut();
      $("#submitSaleBtn").hide();
      $("#showSellingDiv").hide();
      return item;
    }
  });

  $("input[name='createtype']").click(function () {
    if ($("#inputsku").val()) {
      if ($(this).val() === 'add') {
        $.post("/products/findUPC", {sku: $("#inputsku").val()}, function (r) {
          $("#addDiv").fadeIn();
          $("#amzDiv").html("");
          $("#submitSaleBtn").show();
          $("#submitSaleBtn").text("添加Selling");
          $("#addSellingSku").val($("#inputsku").val());
          $("#upc").val(r.upc);
          $("#upc_init").val(r.upc);
          $("#upc_jp").val(r.upcJP);
        });
        $("#showSellingList").load($("#showSellingList").data("url"), {sku: $("#inputsku").val()});
        $("#showSellingDiv").show();
      } else {
        $("#amzDiv").load('/Sellings/saleAmazon', {id: $("#inputsku").val()}, function (r) {
          LoadMask.unmask();
          $("#amzDiv").fadeIn();
          $.getScript('../public/javascripts/editor/kindeditor-min.js', function () {
            KindEditor.create('#productDesc', {
              resizeType: 1,
              allowPreviewEmoticons: false,
              allowImageUpload: false,
              newlineTag: 'br',
              afterChange: function () {
                let div, htmlCode;
                div = $('<div>').html($("<div>").html(this.html()).text());
                div.find('div').replaceWith(function () {
                  return $(this).contents();
                });
                div.find('span').replaceWith(function () {
                  return $(this).contents();
                });
                htmlCode = div.html();
                $('#productDesc').val(htmlCode);
                $("#productDesc").find('~ .help-inline').html((2000 - htmlCode.length) + " bytes left");
                return $('#previewDesc').html(htmlCode);
              },
              items: ['source', '|', '|', 'bold']
            });
          });
          $.getScript('../public/javascripts/component/amazon.coffee', function () {
            $("#feedProductType").trigger('adjust');
          });
          $("#addDiv").fadeOut();
          $("#submitSaleBtn").text("AMZ上架");
          $("#submitSaleBtn").show();
        });
      }
    } else {
      noty({
        text: '请选择SKU',
        type: 'error'
      });
      $(this).prop("checked", false);
    }
  });

});