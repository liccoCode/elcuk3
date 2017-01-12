$(() => {
  //Ajax 加载 Shipment
  $("#splitUnitForm input[name='newUnit.shipType']").change(function() {
    getShipmentList();
  });

  $('#shipments').on('change', '[name=shipmentId]', function() {
    LoadMask.mask();
    let shipmentId = $(this).val();
    $.get("/shipment/" + shipmentId + "/dates", "", function(r) {
      $("input[name='newUnit.attrs.planShipDate']").data('dateinput').setValue(r['begin']);
      $("input[name='newUnit.attrs.planArrivDate']").data('dateinput').setValue(r['end']);
      LoadMask.unmask();
    });
  });

  let $sku = $("#select_sku");
  let cooperId = $("#cooperId").val();
  $sku.typeahead({
    source: (query, process) => {
      let sku = $sku.val();
      $.get('/products/sameSkuAndCooper', {
        sku: sku,
        cooperId: cooperId
      }, function(c) {
        process(c);
      });
    },
    updater: (item) => {
      getSelling(item);
      getProductNmae(item);
      return item;
    }
  });

  getSelling($sku.val());

  $("#warehouse_select").change(function() {
    if ($(this).val()) {
      let country = $("#warehouse_select :selected").text().split('_')[1];
      let sku = $("#select_sku").val();
      $.get("/sellings/findSellingBySkuAndMarket", {
        sku: sku,
        market: "AMAZON_" + country
      }, function(c) {
        $("#sellingId").val(c);
        if (c) {
          getShipmentList();
        } else {
          noty({
            text: "市场对应无Selling",
            type: 'error'
          });
          $("#warehouse_select").prop("value", "");
        }
      });
    } else {
      $("#sellingId").val("");
      $("input[name='newUnit.shipType']").each(function() {
        $(this).prop("checked", false);
      });
    }
  });

  //通过SKU 和 供应商获取相关信息
  function getProductNmae (sku) {
    let cooperId = $("#cooperId").val();
    $.get("/products/findProductName", {
      sku: sku,
      cooperId: cooperId
    }, function(r) {
      $("#productName").val(r.name);
      $("#price_input").val(r['price']);
      $("#unit_currency").prop("value", r['currency']);
      $("#unit_period").html("(生产周期：" + r['period'] + "天)");
      $("#size_of_box").val(r['boxSize']);
    });
  }

  function getSelling (sku) {
    if (sku && $("#warehouse_select").val()) {
      let country = $("#warehouse_select :selected").text().split('_')[1];
      $.get("/sellings/findSellingBySkuAndMarket", {
        sku: sku,
        market: "AMAZON_" + country
      }, function(r) {
        $("#sellingId").val(r);
        if (!$("#sellingId").val()) {
          noty({
            text: "市场对应无Selling",
            type: 'error'
          });
          $("#warehouse_select").prop("value", "");
        }
      });
    }
  }

  function getShipmentList () {
    let whouseId = $("[name='newUnit.whouse.id']").val();
    let shipType = $("[name='newUnit.shipType']:checked").val();
    let planDeliveryDate = $("[name='newUnit.attrs.planDeliveryDate']").val();
    let shipment = $("#shipments");
    if (planDeliveryDate && whouseId && shipType && shipment) {
      if (shipType == 'EXPRESS') {
        $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.');
      } else {
        shipment.mask();
        $.get('/shipments/unitShipments', {
          whouseId: whouseId,
          shipType: shipType,
          planDeliveryDate: planDeliveryDate
        }, function(html) {
          shipment.html(html);
          shipment.unmask();
        });
      }
    } else {
      noty({
        text: "请先填写【预计交货时间】【去往仓库】和【运输方式】！",
        type: 'error'
      });
    }
  }

  $('#box_num').change(function() {
    let boxSize = $("#size_of_box").val();
    let boxNum = $("#box_num").val();
    $("#planQty").val(boxSize * boxNum);
  });

  $("#planQty").change(function() {
    if ($(this).val() > $(this).data('max')) {
      noty({
        text: "分拆数量不能大于" + $(this).data('max'),
        type: 'error'
      });
      $(this).val(0);
    }
    if ($(this).val() < 0) {
      noty({
        text: "分拆数量不能小于0",
        type: 'error'
      });
      $(this).val(0);
    }
  });

});